package com.otcengineering.white_app.serialization.pojo;

public class CustomKeySet {

    private String key;
    private String value;

    public CustomKeySet(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
