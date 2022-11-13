#include "SerialComs.h"
#include "SoftwareSerial.h"
#include <dht.h>
#include "TinyGPS++.h"
dht DHT;

#define DHT11_PIN 5
#define echoPin 2 // attach pin D2 Arduino to pin Echo of HC-SR04
#define trigPin 3 //attach pin D3 Arduino to pin Trig of HC-SR04
float h, t, distance, gpsLat,gpsLong;
const int RX_pin = 10;
const int TX_pin = 11;
String currentDate,combineData;
int counter = 0;
long duration; // variable for the duration of sound wave travel
int ignoreGPS = 0;
int runOne = 0;
unsigned long previousTime = 0;
const long eventTime = 15000;
boolean stopGPS = false;

SoftwareSerial softSerial(RX_pin, TX_pin);
TinyGPSPlus gps;// GPS object to process the NMEA data


SerialComs coms;
SoftwareSerial ss(7,6); //tx,rx 
void setup()
{
  previousTime = millis();
  Serial.begin(9600);
  ss.begin(9600); //Begin First serial communication
  readGPS();
  ss.end(); //End First serial communication

  // Sets the trigPin as an OUTPUT, echoPin as an INPUT
  pinMode(trigPin, OUTPUT); 
  pinMode(echoPin, INPUT); 
  SafeString::setOutput(Serial); 
  softSerial.begin(9600);//Begin Second serial communication
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
        delay(8000);

      readTempHumidity();
      readWaterLevel();
      printSensorData();
      sendData();
      counter++;  
  }
}

//GPS Sensor
void readGPS(){
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
}


//Temperature & Humidity sensor
void readTempHumidity(){
  int chk = DHT.read11(DHT11_PIN);
  if(DHT.temperature == 0 || DHT.humidity ==0){
     chk = DHT.read11(DHT11_PIN);
  }
  h=DHT.humidity;
  t=DHT.temperature;
}

// Ultrasonic Sensor
void readWaterLevel(){
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10); //10 milisecond
  digitalWrite(trigPin, LOW);
  duration = pulseIn(echoPin, HIGH);
  distance = (duration * 0.034 / 2)+4; // Calculate ultrasonic measurement and add height of styrofoam
  distance = ((float)((int)(distance * 10))) / 10; //Convert distance reading to 2 decimal place
  distance = 18 - distance; //total distance deduct ultrasonic measurement and height of styrofoam
}

void printSensorData(){
  Serial.println("\n------Sensor Data------");
  Serial.print("Longitude = ");
  Serial.println(gpsLong);
  Serial.print("Latitude  = ");
  Serial.println(gpsLat);
  Serial.print("Temperature = ");
  Serial.println(t);
  Serial.print("Humidity = ");
  Serial.println(h);
  Serial.print("Water Level = ");
  Serial.println(distance);
  Serial.println("-----------------------");
}

//Send Data to NodeMCU ESP8266
void sendData(){
  coms.textToSend.print(t); //Humidity
  coms.textToSend.print(" ");
  coms.textToSend.print(h); //Temperature
  coms.textToSend.print(" ");
  coms.textToSend.print(distance); //Distance
  coms.textToSend.print(" ");
  coms.textToSend.print(gpsLat); //Latitude
  coms.textToSend.print(" ");
  coms.textToSend.print(gpsLong); //Longtitude
  Serial.println("Sending Data to NodeMCU ESP8266");
}
