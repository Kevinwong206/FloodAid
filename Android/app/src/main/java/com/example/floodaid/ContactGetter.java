package com.example.floodaid;

public class ContactGetter {
    String address, contactType, officeName, phoneNum, state;

    public ContactGetter() {
    }

    public ContactGetter(String address, String contactType, String officeName, String phoneNum, String state) {
        this.address = address;
        this.contactType = contactType;
        this.officeName = officeName;
        this.phoneNum = phoneNum;
        this.state = state;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
