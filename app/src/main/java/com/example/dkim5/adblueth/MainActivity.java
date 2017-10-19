package com.example.dkim5.adblueth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final String TAG = "BluetoothService";
    private BluetoothAdapter btAdapt = null;
    private BluetoothSocket btSocekt = null;
    private StringBuilder sBuilder = new StringBuilder();
    Handler h;
    TextView temptxt, htxt,colortxt;
    private static String rec;
    final int RECIEVE_MESSAGE = 1;

    private ConnectedThread connectedthread;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String address = "98:D3:32:10:F5:AA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temptxt=(TextView)findViewById(R.id.TV);
        htxt=(TextView)findViewById(R.id.HTV);
        colortxt=(TextView)findViewById(R.id.ColorTV);


        btAdapt = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

    }

    private void Communication()
    {
        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                rec="";

                switch (msg.what) {
                    case RECIEVE_MESSAGE:
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);
                        sBuilder.append(strIncom);
                        int endOfLineIndex = sBuilder.indexOf("\r\n");
                        if (endOfLineIndex > 0) {

                            rec = sBuilder.substring(0, endOfLineIndex);
                            sBuilder.delete(0, sBuilder.length());
                        }

                        break;
                }
            }

        };
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord",
                        new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }



    @Override
    public void onResume() {
        super.onResume();

        BluetoothDevice device = btAdapt.getRemoteDevice(address);

        try {
            btSocekt = createBluetoothSocket(device);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }
        btAdapt.cancelDiscovery();
        Log.d(TAG, "...Connecting...");
        try {
            btSocekt.connect();
            Log.d(TAG, "....Connection ok...");
        } catch (IOException e) {
            try {
                btSocekt.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        connectedthread = new ConnectedThread(btSocekt);
        connectedthread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try {
            btSocekt.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapt == null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapt.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.Rbtn:
                connectedthread.write("1");
                Communication();
                colortxt.setText(rec);

                break;
            case R.id.Bbtn:
                connectedthread.write("2");
                Communication();
                colortxt.setText(rec);

                break;
            case R.id.Gbtn:
                connectedthread.write("3");
                Communication();
                colortxt.setText(rec);

                break;
            case R.id.Tbtn:
                connectedthread.write("4");
                Communication();
                String str="Current Temp:"+rec;
                temptxt.setText(str);

                break;
            case R.id.Hbtn:
                connectedthread.write("5");
                Communication();
                String str2="Current Humidity:"+rec;
                htxt.setText(str2);

                break;
        }

    }


    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }
    }
}
