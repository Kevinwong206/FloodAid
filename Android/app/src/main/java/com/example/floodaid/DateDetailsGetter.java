package com.example.floodaid;

public class DateDetailsGetter {
    String LastDate, LastTime, NumData, DangerousStage;

    public DateDetailsGetter() {
    }

    public DateDetailsGetter(String lastDate, String lastTime, String numData, String dangerousStage) {
        LastDate = lastDate;
        LastTime = lastTime;
        NumData = numData;
        DangerousStage = dangerousStage;
    }

    public String getLastDate() {
        return LastDate;
    }

    public void setLastDate(String lastDate) {
        LastDate = lastDate;
    }

    public String getLastTime() {
        return LastTime;
    }

    public void setLastTime(String lastTime) {
        LastTime = lastTime;
    }

    public String getNumData() {
        return NumData;
    }

    public void setNumData(String numData) {
        NumData = numData;
    }

    public String getDangerousStage() {
        return DangerousStage;
    }

    public void setDangerousStage(String dangerousStage) {
        DangerousStage = dangerousStage;
    }
}
