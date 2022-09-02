#include "SerialComs.h"
#include "SoftwareSerial.h"
#include <dht.h>
#include "TinyGPS++.h"
dht DHT;

//-------------------------------------------SENSOR PINS---------------------------------
#define DHT11_PIN 5
#define echoPin 2 // attach pin D2 Arduino to pin Echo of HC-SR04
#define trigPin 3 //attach pin D3 Arduino to pin Trig of HC-SR04
float h, t;
const int RX_pin = 10;
const int TX_pin = 11;
String currentDate,combineData;
int counter = 0;
long duration; // variable for the duration of sound wave travel
int distance; // variable for the distance measurement
float gpsLat,gpsLong ;
int ignoreGPS = 0;
int runOne = 0;
unsigned long previousTime = 0;
const long eventTime = 15000;
boolean stopGPS = false;

SoftwareSerial softSerial(RX_pin, TX_pin);
SerialComs coms;
SoftwareSerial ss(7,6); //tx,rx 
TinyGPSPlus gps;// GPS object to process the NMEA data

void setup()
{
  previousTime = millis();
  Serial.begin(9600);
  ss.begin(9600); 
  delay(10000);

  //Loop until stopGPS variable is true
  do{
    //While there are incoming characters  from the GPS
    while(ss.available()> 0)              
    {
      gps.encode(ss.read());           
    }

    //When GPS location updated
    if(gps.location.isUpdated())          
    {
      Serial.print("Latitude:");
      Serial.println(gps.location.lat(), 6);
      gpsLat = gps.location.lat(), 6;
      
      Serial.print("Longitude:");
      Serial.println(gps.location.lng(), 6);
      gpsLong = gps.location.lng(), 6;
      Serial.println("");
      stopGPS=true;
    }

    //If arduino running for more than 15 seconds with no GPS signal, will set stopGPS to true
    unsigned long currentTime = millis();
    if(currentTime - previousTime >= eventTime){
        ignoreGPS = 1;
        stopGPS=true;
        gpsLat = 0;
        gpsLong = 0;
    }
  }while(stopGPS == false);
  ss.end();

  // Sets the trigPin as an OUTPUT, echoPin as an INPUT
  pinMode(trigPin, OUTPUT); 
  pinMode(echoPin, INPUT); 
  SafeString::setOutput(Serial); 
  softSerial.begin(9600);

  if (!coms.connect(softSerial)) {
    while (1) {
      Serial.println(F("Out of memory"));
      delay(3000);
    }
  }
  Serial.println(F("Uno Setup finished."));
}

void loop()
{
  coms.sendAndReceive();
  if (coms.textToSend.isEmpty()) { 

      //Delay for firebase to send data
      if(counter>0)
        delay(3500);

      //DHT11 sensor
      int chk = DHT.read11(DHT11_PIN);
      if(DHT.temperature == 0 || DHT.humidity ==0){
         chk = DHT.read11(DHT11_PIN);
      }

      Serial.print("Temperature = ");
      Serial.println(DHT.temperature);
      Serial.print("Humidity = ");
      Serial.println(DHT.humidity);
      h=DHT.humidity;
      t=DHT.temperature;
      
      //Ultrasonic Sensor
      digitalWrite(trigPin, LOW);
      delayMicroseconds(2);
      digitalWrite(trigPin, HIGH);
      delayMicroseconds(10); //10 milisecond
      digitalWrite(trigPin, LOW);
      duration = pulseIn(echoPin, HIGH);
      distance = duration * 0.034 / 2; // Speed of sound wave divided by 2 (go and back)
      Serial.print("Distance = ");
      Serial.print(distance);
      Serial.println("\n");

      coms.textToSend.print(t); //Humidity
      coms.textToSend.print(" ");
      coms.textToSend.print(h); //Temperature
      coms.textToSend.print(" ");
      coms.textToSend.print(distance); //Distance
      coms.textToSend.print(" ");
      //if(gpsLong!=0.00 && gpsLat!=0.00){
      coms.textToSend.print(gpsLat); //Latitude
      coms.textToSend.print(" ");
      coms.textToSend.print(gpsLong); //Longtitude
      //}
      
      counter++;  
  }
}
