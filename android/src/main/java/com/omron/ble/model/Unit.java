package com.omron.ble.model;

/**
 * Created by Administrator on 2017/12/8 0008.
 */

public enum Unit {
    UNIT_MGPL("mg/L"),
    UNIT_MMOLPL("mmol/L");

    private String description;

    private Unit(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}