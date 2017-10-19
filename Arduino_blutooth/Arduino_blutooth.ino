#include <SoftwareSerial.h>
#include <dht.h>
dht DHT;

SoftwareSerial BT(10, 11);
int RED=13;
int BLUE=12;
int GREEN=8;
int Temp=7;
 
void setup()  
{
  // set digital pin to control as an output
  // set the data rate for the SoftwareSerial port
    BT.begin(9600);
    
    pinMode(RED,OUTPUT);
    pinMode(BLUE,OUTPUT);
    pinMode(GREEN,OUTPUT);
    
    

}
char a; // stores incoming character from other device
void loop() 
{
  int tem=DHT.read11(Temp);
  if (BT.available())
  // if text arrived in from BT serial...
  {
    a=(BT.read());
    switch(a)
    {
      case '1':
            BT.println("RED");
            digitalWrite(RED, HIGH);
            digitalWrite(BLUE,LOW);
            digitalWrite(GREEN,LOW);
            
            
      break;
      case '2':
            BT.println("BLUE");
            digitalWrite(RED, LOW);
            digitalWrite(BLUE,HIGH);
            digitalWrite(GREEN,LOW);
            
      break;
      case '3':
            BT.println("GREEN");
            digitalWrite(RED, LOW);
            digitalWrite(BLUE,LOW);
            digitalWrite(GREEN,HIGH);
            
            break;    
      case '4':
            BT.println(DHT.temperature);
            digitalWrite(RED,HIGH);
            digitalWrite(BLUE,HIGH);
            digitalWrite(GREEN,HIGH);   
                
            break;
      case '5':
            BT.println(DHT.humidity);
            digitalWrite(RED,LOW);
            digitalWrite(BLUE,LOW);
            digitalWrite(GREEN,LOW); 
            
            break;
  
     }
  }
}



