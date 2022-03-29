package com.app.mysantinis.module;

public class RestaurantData {
    private String name;
    private String address;
    private String state;
    private String postCode;
    private String consumerKey;
    private String consumerSecret;
    private String status;
    private int id;

    public RestaurantData(int id, String name, String address, String state, String postCode, String consumerKey, String consumerSecret, String status) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.state = state;
        this.postCode = postCode;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public String getStatus() {
        return status;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getState() {
        return state;
    }

    public int getId() {
        return id;
    }
}
