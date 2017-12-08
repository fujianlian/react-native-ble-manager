package com.omron.ble.model;

/**
 * Created by Administrator on 2017/12/8 0008.
 */


public enum Meal {
    NOT_PRESENT("未设定"),
    BEFORE_MEAL("餐前"),
    AFTER_MEAL("餐后");

    private String description;

    private Meal(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
