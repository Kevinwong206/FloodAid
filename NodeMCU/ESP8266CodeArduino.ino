#include <ESP8266WebServer.h>
#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include "CTBot.h"
#include "SerialComs.h"
#include "SoftwareSerial.h"
#include <FirebaseArduino.h>    
#include <ArduinoJson.h>
#include <NTPClient.h>
#include <WiFiUdp.h>
#include "time.h"
#include <FirebaseHttpClient.h>
#include <ESP8266HTTPClient.h> // http web access library
//Initialisation variables for Transmit data from uno to NodeMCU, Wifi, Firebase, Date & Time, Telegram

const int RX_pin = 13; // PIN 13 is D7 on NodeMCU
const int TX_pin = 12; // PIN 12 is D6  on NodeMCU
String combineData, humidity, temperature, distance, gpsLat, gpsLong; // Sensor Data
SoftwareSerial softSerial(RX_pin, TX_pin); // Transmit data from Arduino UNO to NodeMCU
SerialComs coms;
#define FIREBASE_HOST "floodmonitoringsystem-d94ce-default-rtdb.firebaseio.com" //Firebase
#define FIREBASE_AUTH "A75IoK7uErtUi5QJcu4fGz98POVK95fLfgkHE08z"
#define WIFI_SSID "Bailey" // Wifi
#define WIFI_PASSWORD "dvqcr05567"
const long utcOffsetInSeconds = 28800; // Date and Time
char daysOfTheWeek[7][12] = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org", utcOffsetInSeconds);
int compareAddtionMin, compareBeforeMin, counter, tempMin, tempHour, compareAddtionHour, compareBeforeHour,hourDelay = 0;
unsigned long epochTime;
int minDelay = 2;
String newSeconds, newMinutes;
String API_Key = "520ca72125ec356abc3a03a92aac5094";
int tempDistance;
String tempWeather;

void setup() {
  Serial.begin(9600);

  //---------------------------------------------------CONNECT WIFI--------------------------------------------------
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to WIFI");
  while (WiFi.status() != WL_CONNECTED){
    delay(500);
  }
  Serial.println("Wifi Connected\n");

  //-----------------------------------------------DATE AND TIME----------------------------------------------
  timeClient.begin();

  //-----------------------------------------------CONNECT ARDUINO-------------------------------------------
  Serial.println("Connecting with Arduino");
  SafeString::setOutput(Serial); // enable error messages and debugging
  softSerial.begin(9600); // use previous rxPin, txPin and set 256 RX buffer
  coms.setAsController(); // Always choose the SoftwareSerial Side as the controller

  if (!coms.connect(softSerial)) {
    while (1) {
      Serial.println(F("Unable to connect with Arduino\n"));
    }
  }
  Serial.println(F("Arduino Connected\n"));
    
  //---------------------------------------------------FIREBASE-------------------------------------------
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);

}

void loop() {
  //-------------------------------------------RECEIVE DATA FROM ARDUINO---------------------------------
  coms.sendAndReceive(); 
  
  // If receive data from arduino
  if (!coms.textReceived.isEmpty()) { 
    String sensorData[5]; // Modify based on number of sensors
    int StringCount = 0;
    combineData = coms.textReceived.c_str();
    
    //Split string to array
    while (combineData.length() > 0){
      int index = combineData.indexOf(' ');
      
      if (index == -1){
        sensorData[StringCount++] = combineData;
        break;
      }
      
      else {
        sensorData[StringCount++] = combineData.substring(0, index);
        combineData = combineData.substring(index+1);
      }
    }
    
    temperature=String(sensorData[0]);
    Serial.println("Temperature = " + temperature);
    humidity=String(sensorData[1]);
    Serial.println("Humidity = " + humidity);
    distance=String(sensorData[2]);
    Serial.println("Distance = " + distance);
    gpsLat=String(sensorData[3]);
    Serial.println("Latitude = " + gpsLat);
    gpsLong=String(sensorData[4]);
    Serial.println("Longtitude = " + gpsLong);

    //If no GPS data, will use last updated location
    if(gpsLat== "0.00" && gpsLong== "0.00"){
      gpsLat = Firebase.getString("RealTimeData/data/Latitude");
      gpsLong = Firebase.getString("RealTimeData/data/Longtitude");
      Serial.println("New Latitude = " + gpsLat);
      Serial.println("New Longtitude = " + gpsLong);
    }

    //Get Weather data based on location
    if (WiFi.status() == WL_CONNECTED) //Check WiFi connection status
    {
      HTTPClient http; //Declare an object of class HTTPClient
      http.begin("http://api.openweathermap.org/data/2.5/weather?lat=" + gpsLat + "&lon=" + gpsLong + "&lon=" + "&appid=" + API_Key +"&units=metric"); // !!
      int httpCode = http.GET(); // send the request
       
      if (httpCode > 0) // check the returning code
      {
        String payload = http.getString(); //Get the request response payload
         
        DynamicJsonBuffer jsonBuffer(512);
         
        // Parse JSON object
        JsonObject& root = jsonBuffer.parseObject(payload);
        if (!root.success()) {
          Serial.println(F("Parsing failed!"));
          return;
        }
        JsonObject& weather = root["weather"][0];
        String weatherId = weather["id"];
        String weatherDesc = weather["description"]; 
        String cityName = root["name"]; 
        float wind_speed = root["wind"]["speed"]; // get wind speed in m/s
        int wind_degree = root["wind"]["deg"]; // get wind degree in °
        
        // print data
        Serial.println(weatherId);
        Serial.println(weatherDesc);
        Serial.println("city name = " + cityName);
        Serial.printf("Wind speed = %.1f m/s\r\n", wind_speed);
        Serial.printf("Wind degree = %d°\r\n\r\n", wind_degree);
        
        //REAL TIME
        Firebase.setString("RealTimeData/data/WeatherID",weatherId);
        Firebase.setString("RealTimeData/data/WeatherDesc",weatherDesc);
        Firebase.setString("RealTimeData/data/CityName",cityName);
        String temp_wind_speed = String(wind_speed);
        String temp_wind_degree = String(wind_degree);
        Firebase.setString("RealTimeData/data/WindSpeed",temp_wind_speed);
        Firebase.setString("RealTimeData/data/WindDegree",temp_wind_degree);

        tempWeather = weatherDesc;
      }
      http.end(); //Close connection
    }

    //REAL TIME
    Firebase.setString("RealTimeData/data/Humidity",humidity);
    Firebase.setString("RealTimeData/data/Temperature",temperature);
    Firebase.setString("RealTimeData/data/Distance",distance);
    Firebase.setString("RealTimeData/data/Latitude",gpsLat);
    Firebase.setString("RealTimeData/data/Longtitude",gpsLong);
    

    Serial.println();

    //-------------------------------------------FIRST TIME RUN-----------------------------------------
    if (counter == 0){
      //------------------------------------------------------------------UPDATE ADDITION (Calculate time after delay)
      timeClient.update();
      tempMin = timeClient.getMinutes();
      tempHour = timeClient.getHours();
      compareAddtionMin = tempMin+minDelay;
      compareAddtionHour = tempHour;
      
      if(compareAddtionMin>59){
        hourDelay=compareAddtionMin/60;
        compareAddtionMin == compareAddtionMin%60;
        compareAddtionHour=compareAddtionHour+hourDelay;
      }
      //Date
      unsigned long epochTime = timeClient.getEpochTime();
      struct tm *ptm = gmtime ((time_t *)&epochTime);
      int monthDay = ptm->tm_mday;
      int currentMonth = ptm->tm_mon+1;
      int currentYear = ptm->tm_year+1900;
      String currentDate = String(monthDay) + "-" + String(currentMonth) + "-" + String(currentYear);
      //Time
      int hour = timeClient.getHours();
      int minute = timeClient.getMinutes();
      if (minute<10)
        newMinutes = "0"+String(minute);
      else
        newMinutes = String(minute);
        
      int second = timeClient.getSeconds();
      if (second<10)
        newSeconds = "0"+String(second);
      else
        newSeconds = String(second);
      
      String currentTime = String(hour) + ":" + newMinutes + ":" + newSeconds;

    //-------------------------------------------FIREBASE BASED ON DATE AND TIME----------------------------------------------      
      Firebase.setString("SensorData/"+currentDate+"/"+currentTime+"/Distance",distance);
      Firebase.setString("SensorData/"+currentDate+"/"+currentTime+"/WeatherDesc",tempWeather);
      Firebase.setString("SensorData/"+currentDate+"/"+currentTime+"/Date",currentDate);
      Firebase.setString("SensorData/"+currentDate+"/"+currentTime+"/Time",currentTime);
      
      int tempNumData;
      
      Firebase.setString("DataHistory/"+currentDate+"/Date",currentDate);
      Firebase.setString("DataHistory/"+currentDate+"/LastUpdated",currentTime);

      String numDataString = Firebase.getString("DataHistory/"+currentDate+"/NumData");
      if(numDataString.equals("")){
        tempNumData = 1;
      }else{
        tempNumData = numDataString.toInt();
        tempNumData = tempNumData+1;
      }
      
      Firebase.setString("DataHistory/"+currentDate+"/NumData",String(tempNumData));


      int tempId;
      int compareId;
      tempDistance = distance.toInt();
      if(tempDistance>0 && tempDistance <=5){
        tempId = 4;
      }else if (tempDistance>5 && tempDistance <=10){
        tempId = 3;
      }else if (tempDistance>10 && tempDistance <=15){
        tempId = 2;
      }else{
        tempId = 1;
      }
      String idString = Firebase.getString("DataHistory/"+currentDate+"/SevereId");
      if(idString.equals("")){
        compareId = 0;
      }else{
        compareId = idString.toInt();
      }

      if(tempId>compareId){
        Firebase.setString("DataHistory/"+currentDate+"/SevereId",String(tempId));
      }
      
      Serial.println("Data uploaded to firebase");
    
      if (Firebase.failed()){
        Serial.println("error");
        Serial.println (Firebase.error());
        return;
      }
    }
    
    counter++;
    //--------------------------------------------------------------------------------UPDATE BEFORE (Current Time)
    timeClient.update();
    compareBeforeMin = timeClient.getMinutes();
    compareBeforeHour = timeClient.getHours();

    //----------------------------------------------------------------------------------------UPLOAD DATA AFTER DELAY
    if(compareBeforeMin == compareAddtionMin && compareBeforeHour == compareAddtionHour){
      //----------------------------------------------------------UPDATE ADDITION (Calculate next time after delay)
      tempMin = timeClient.getMinutes();
      tempHour = timeClient.getHours();
      compareAddtionMin = tempMin+minDelay;
      compareAddtionHour = tempHour;
      
      if(compareAddtionMin>59){
        hourDelay=compareAddtionMin/60;
        compareAddtionMin == compareAddtionMin%60;
        compareAddtionHour=compareAddtionHour+hourDelay;
      }

      //Date
      unsigned long epochTime = timeClient.getEpochTime();
      struct tm *ptm = gmtime ((time_t *)&epochTime);
      int monthDay = ptm->tm_mday;
      int currentMonth = ptm->tm_mon+1;
      int currentYear = ptm->tm_year+1900;
      String currentDate = String(monthDay) + "-" + String(currentMonth) + "-" + String(currentYear);
      //Time
      int hour = timeClient.getHours();
      int minute = timeClient.getMinutes();
      if (minute<10)
        newMinutes = "0"+String(minute);
      else
        newMinutes = String(minute);
        
      int second = timeClient.getSeconds();
      if (second<10)
        newSeconds = "0"+String(second);
      else
        newSeconds = String(second);
      
      String currentTime = String(hour) + ":" + newMinutes + ":" + newSeconds;

    //-------------------------------------------FIREBASE BASED ON DATE AND TIME AFTER DELAY----------------------------------------------
      Firebase.setString("SensorData/"+currentDate+"/"+currentTime+"/Distance",distance);
      Firebase.setString("SensorData/"+currentDate+"/"+currentTime+"/WeatherDesc",tempWeather);
      Firebase.setString("SensorData/"+currentDate+"/"+currentTime+"/Date",currentDate);
      Firebase.setString("SensorData/"+currentDate+"/"+currentTime+"/Time",currentTime);
      
      Firebase.setString("DataHistory/"+currentDate+"/LastUpdated",currentTime);
      String numDataString = Firebase.getString("DataHistory/"+currentDate+"/NumData");
      int tempNumData;
      if(numDataString.equals("")){
        tempNumData = 1;
      }else{
        tempNumData = numDataString.toInt();
        tempNumData = tempNumData+1;
      }
      Firebase.setString("DataHistory/"+currentDate+"/NumData",String(tempNumData));


      int tempId;
      int compareId;
      tempDistance = distance.toInt();
      if(tempDistance>0 && tempDistance <=5){
        tempId = 4;//Dangerous
      }else if (tempDistance>5 && tempDistance <=10){
        tempId = 3; //Warning
      }else if (tempDistance>10 && tempDistance <=15){
        tempId = 2; //Alert
      }else{
        tempId = 1; //Normal
      }
      String idString = Firebase.getString("DataHistory/"+currentDate+"/SevereId");
      if(idString.equals("")){
        compareId = 0;
      }else{
        compareId = idString.toInt();
      }

      if(tempId>compareId){
        Firebase.setString("DataHistory/"+currentDate+"/SevereId",String(tempId));
      }
      Serial.println("Data uploaded to firebase");
    
      if (Firebase.failed()){
        Serial.println("error");
        Serial.println (Firebase.error());
        return;
      }
    }
  }
}
