package com.example.floodaid;

public class DonationGetter {
    String pickupAddress, condition, donatorName, donatorPhone, itemUID, productTitle, quantity,imageURL;

    public DonationGetter() {
    }

    public DonationGetter(String pickupAddress, String condition, String donatorName, String donatorPhone, String itemUID, String productTitle, String quantity, String imageURL) {

        this.pickupAddress = pickupAddress;
        this.condition = condition;
        this.donatorName = donatorName;
        this.donatorPhone = donatorPhone;
        this.itemUID = itemUID;
        this.productTitle = productTitle;
        this.quantity = quantity;
        this.imageURL = imageURL;
    }

    public String getPickupAddress() { return pickupAddress; }

    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getItemUID() { return itemUID; }

    public void setItemUID(String itemUID) { this.itemUID = itemUID; }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDonatorName() {
        return donatorName;
    }

    public void setDonatorName(String donatorName) {
        this.donatorName = donatorName;
    }

    public String getDonatorPhone() {
        return donatorPhone;
    }

    public void setDonatorPhone(String donatorPhone) {
        this.donatorPhone = donatorPhone;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
