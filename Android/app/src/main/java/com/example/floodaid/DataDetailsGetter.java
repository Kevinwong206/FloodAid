package com.example.floodaid;

public class DataDetailsGetter {
    String Distance, WeatherDesc, Date, Time;

    public DataDetailsGetter() {
    }

    public DataDetailsGetter(String distance, String weatherDesc, String date, String time) {
        Distance = distance;
        WeatherDesc = weatherDesc;
        Date = date;
        Time = time;
    }

    public String getDistance() {
        return Distance;
    }

    public void setDistance(String distance) {
        Distance = distance;
    }

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
