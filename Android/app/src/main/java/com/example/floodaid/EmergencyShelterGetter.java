package com.example.floodaid;

public class EmergencyShelterGetter {
    String shelterAddress, shelterName, name, phone, maxCapacity, currentCapacity, distance;

    public EmergencyShelterGetter() {
    }

    public EmergencyShelterGetter(String shelterAddress, String shelterName, String name, String phone, String maxCapacity, String currentCapacity, String distance) {
        this.shelterAddress = shelterAddress;
        this.shelterName = shelterName;
        this.name = name;
        this.phone = phone;
        this.maxCapacity = maxCapacity;
        this.currentCapacity = currentCapacity;
        this.distance = distance;
    }

    public String getShelterAddress() {
        return shelterAddress;
    }

    public void setShelterAddress(String shelterAddress) {
        this.shelterAddress = shelterAddress;
    }

    public String getShelterName() {
        return shelterName;
    }

    public void setShelterName(String shelterName) {
        this.shelterName = shelterName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(String maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public String getCurrentCapacity() {
        return currentCapacity;
    }

    public void setCurrentCapacity(String currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
