package com.example.floodaid;

public class DataDetailsGetter {
    String WaterLevel, WeatherDesc, Date, Time;

    public DataDetailsGetter() {
    }

    public DataDetailsGetter(String waterLevel, String weatherDesc, String date, String time) {
        WaterLevel = waterLevel;
        WeatherDesc = weatherDesc;
        Date = date;
        Time = time;
    }

    public String getWaterLevel() { return WaterLevel; }

    public void setWaterLevel(String waterLevel) { WaterLevel = waterLevel; }

    public String getWeatherDesc() {
        return WeatherDesc;
    }

    public void setWeatherDesc(String weatherDesc) {
        WeatherDesc = weatherDesc;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }
}
