package com.example.floodaid;

public class DataHistoryDateGetter {
    String Date, LastUpdated, NumData, SevereId;

    public DataHistoryDateGetter() {
    }

    public DataHistoryDateGetter(String date, String lastUpdated, String numData, String severeId) {
        Date = date;
        LastUpdated = lastUpdated;
        NumData = numData;
        SevereId = severeId;
    }

    public void setDate(String date) {
        Date = date;
    }

    public void setLastUpdated(String lastUpdated) {
        LastUpdated = lastUpdated;
    }

    public void setNumData(String numData) {
        NumData = numData;
    }

    public void setSevereId(String severeId) {
        SevereId = severeId;
    }

    public String getDate() {
        return Date;
    }

    public String getLastUpdated() {
        return LastUpdated;
    }

    public String getNumData() {
        return NumData;
    }

    public String getSevereId() {
        return SevereId;
    }


}
