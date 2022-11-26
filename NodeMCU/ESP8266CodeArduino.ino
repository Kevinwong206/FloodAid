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
int minDelay = 5;
String newSeconds, newMinutes;
String API_Key = "520ca72125ec356abc3a03a92aac5094";
float tempDistance;
String sensorData[5];

String weatherId ;
String weatherDesc ;
String cityName ;
float wind_speed ;
int wind_degree ;

void setup() {
  Serial.begin(9600);
  //-----------------------------------------------CONNECT WIFI----------------------------------------------
  connectWifi();
  //-----------------------------------------------CONNECT TIME----------------------------------------------
  timeClient.begin();
  //-----------------------------------------------CONNECT ARDUINO-------------------------------------------
  Serial.println("Connecting with Arduino");
  SafeString::setOutput(Serial); // enable error messages and debugging
  softSerial.begin(9600); 
  coms.setAsController(); 
  if (!coms.connect(softSerial)) {
    while (1) {
      Serial.println(F("Unable to connect with Arduino\n"));
    }
  }
  Serial.println(F("Arduino Connected\n"));
  //---------------------------------------------CONNECT FIREBASE-------------------------------------------
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
}

void loop() {
  //-------------------------------------------RECEIVE DATA FROM ARDUINO---------------------------------
  coms.sendAndReceive(); 
  // If receive data from arduino
  if (!coms.textReceived.isEmpty()) { 

    receiveData();
    checkGPSCoordinates();
    getWeather();
    uploadRealTime();
    Serial.println();

    //-------------------------------------------FIRST TIME RUN-----------------------------------------
    if (counter == 0){
      timeClient.update();
      IncreaseTime();
      //Date
      String currentDate = getCurrentDate();
      //Time
      String currentTime = getCurrentTime();

      uploadScheduledData(currentDate,currentTime);
    }

    //-------------------------------------------UPDATE TIME-----------------------------------------
    counter++;
    timeClient.update();
    compareBeforeMin = timeClient.getMinutes();
    compareBeforeHour = timeClient.getHours();

    //----------------------------------------UPLOAD AFTER DELAY-----------------------------------------
    if(compareBeforeMin == compareAddtionMin && compareBeforeHour == compareAddtionHour){
      IncreaseTime();

      //Date
      String currentDate = getCurrentDate();
      //Time
      String currentTime = getCurrentTime();
      
      uploadScheduledData(currentDate,currentTime);
    }
  }
}

void connectWifi(){
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to WIFI");
  while (WiFi.status() != WL_CONNECTED){
    Serial.println("Not Connected to Wifi\n");
    delay(500);
  }
  if (WiFi.status() == WL_CONNECTED)
    Serial.println("Wifi Connected\n");
}

void receiveData(){
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
    //Serial.println("Temperature = " + temperature);
    humidity=String(sensorData[1]);
    //Serial.println("Humidity = " + humidity);
    distance=String(sensorData[2]);
    //Serial.println("Distance = " + distance);
    gpsLat=String(sensorData[3]);
    //Serial.println("Latitude = " + gpsLat);
    gpsLong=String(sensorData[4]);
    //Serial.println("Longtitude = " + gpsLong);
}

void checkGPSCoordinates(){
  //If no GPS data, will use last updated location
  if(gpsLat== "0.00" && gpsLong== "0.00"){
    gpsLat = Firebase.getString("RealTimeData/Latitude");
    gpsLong = Firebase.getString("RealTimeData/Longtitude");
  }
}

void getWeather(){
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
        String temp_weatherId = weather["id"];
        String temp_weatherDesc = weather["description"]; 
        String temp_cityName = root["name"]; 
        float temp_wind_speed = root["wind"]["speed"]; // get wind speed in m/s
        int temp_wind_degree = root["wind"]["deg"]; // get wind degree in °

        weatherId = temp_weatherId;
        weatherDesc = temp_weatherDesc; 
        cityName = temp_cityName;
        wind_speed = temp_wind_speed;
        wind_degree = temp_wind_degree;
      }
      http.end(); //Close connection
    }
}

void uploadRealTime(){
  Firebase.setString("RealTimeData/WeatherID",weatherId);
    Firebase.setString("RealTimeData/WeatherDesc",weatherDesc);
    Firebase.setString("RealTimeData/CityName",cityName);
    String convert_wind_speed = String(wind_speed);
    String convert_wind_degree = String(wind_degree);
    Firebase.setString("RealTimeData/WindSpeed",convert_wind_speed);
    Firebase.setString("RealTimeData/WindDegree",convert_wind_degree);
    Firebase.setString("RealTimeData/Humidity",humidity);
    Firebase.setString("RealTimeData/Temperature",temperature);
    Firebase.setString("RealTimeData/WaterLevel",distance);
    Firebase.setString("RealTimeData/Latitude",gpsLat);
    Firebase.setString("RealTimeData/Longtitude",gpsLong);

    Serial.println("----Uploaded real-time Sensor Data----");
    
   if (Firebase.failed()){
    Serial.println("error uploading to realtime database");
    Serial.println (Firebase.error());
    return;
  }
}


String getCurrentDate(){
  unsigned long epochTime = timeClient.getEpochTime();
  struct tm *ptm = gmtime ((time_t *)&epochTime);
  int monthDay = ptm->tm_mday;
  int currentMonth = ptm->tm_mon+1;
  int currentYear = ptm->tm_year+1900;
  String testDate = String(monthDay) + "-" + String(currentMonth) + "-" + String(currentYear);
  return testDate;
}

String getCurrentTime(){
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
  return currentTime;
}

void IncreaseTime(){
  tempMin = timeClient.getMinutes();
  tempHour = timeClient.getHours();
  compareAddtionMin = tempMin+minDelay;
  compareAddtionHour = tempHour;
  
  if(compareAddtionMin>59){
    compareAddtionMin = compareAddtionMin%60;
    compareAddtionHour = compareAddtionHour+1;
  }
}

void uploadScheduledData(String currentDate, String currentTime){
  Firebase.setString("SensorData/"+currentDate+"/"+currentTime+"/WaterLevel",distance);
  Firebase.setString("SensorData/"+currentDate+"/"+currentTime+"/WeatherDesc",weatherDesc);
  Firebase.setString("SensorData/"+currentDate+"/"+currentTime+"/Date",currentDate);
  Firebase.setString("SensorData/"+currentDate+"/"+currentTime+"/Time",currentTime);
  
 
  Firebase.setString("DateDetails/"+currentDate+"/LastDate",currentDate);
  Firebase.setString("DateDetails/"+currentDate+"/LastTime",currentTime);

  int numData = getNumData(currentDate);
  Firebase.setString("DateDetails/"+currentDate+"/NumData",String(numData));

  int dangerousID = getDangerousID(distance, currentDate);
  Firebase.setString("DateDetails/"+currentDate+"/DangerousStage",String(dangerousID));

  Serial.println("----Uploaded Scheduled Sensor Data----\n");

  if (Firebase.failed()){
    Serial.println("error uploading to firestore");
    Serial.println (Firebase.error());
    return;
  }
}

int getNumData(String currentDate){
  int tempNumData;
  String numDataString = Firebase.getString("DateDetails/"+currentDate+"/NumData");
  if(numDataString.equals("")){
    tempNumData = 1;
  }else{
    tempNumData = numDataString.toInt();
    tempNumData = tempNumData+1;
  }

  return tempNumData;
}


int getDangerousID(String distance, String currentDate){
  int tempId;
  int compareId;
  //Get current
  tempDistance = distance.toFloat();
  if(tempDistance>=17){
    tempId = 4;//Danger
  }else if (tempDistance>=16 && tempDistance <17){
    tempId = 3; //Warning
  }else if (tempDistance>=14 && tempDistance <16){
    tempId = 2; //Alert
  }else{
    tempId = 1; //Normal
  }

  //Get last 
  String idString = Firebase.getString("DateDetails/"+currentDate+"/DangerousStage");
  if(idString.equals("")){
    compareId = 0;
  }else{
    compareId = idString.toInt();
  }

  //compare
  if(tempId>compareId){
    return tempId;
  }
  else{
    return compareId;
  }
}
