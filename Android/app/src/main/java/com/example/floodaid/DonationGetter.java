package com.example.floodaid;

public class DonationGetter {
    String address, condition, donatorName, donatorPhone, itemId, productTitle, quantity,imageURL;

    public DonationGetter() {
    }

    public DonationGetter(String address, String condition, String donatorName, String donatorPhone, String itemId, String productTitle, String quantity, String imageURL) {
        this.address = address;
        this.condition = condition;
        this.donatorName = donatorName;
        this.donatorPhone = donatorPhone;
        this.itemId = itemId;
        this.productTitle = productTitle;
        this.quantity = quantity;
        this.imageURL = imageURL;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

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

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
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
