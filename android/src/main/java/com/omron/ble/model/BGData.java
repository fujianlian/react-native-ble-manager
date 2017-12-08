package com.omron.ble.model;

/**
 * Created by Administrator on 2017/12/8 0008.
 */


import com.omron.ble.model.Meal;
import com.omron.ble.model.Unit;
import java.util.Calendar;

public class BGData {
    private int sequenceNumber;
    private Calendar time;
    private float glucoseConcentration;
    private Unit unit;
    private Meal meal;

    public BGData() {
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("{");

        sb.append("unit:"+unit);
        sb.append(",");

        sb.append("meal:"+meal);
        sb.append(",");

        sb.append("glucoseConcentration:"+glucoseConcentration);

        sb.append("}");
        return sb.toString();
    }

    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Calendar getTime() {
        return this.time;
    }

    public void setTime(Calendar time) {
        this.time = time;
    }

    public float getGlucoseConcentration() {
        return this.glucoseConcentration;
    }

    public void setGlucoseConcentration(float glucoseConcentration) {
        this.glucoseConcentration = glucoseConcentration;
    }

    public Unit getUnit() {
        return this.unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Meal getMeal() {
        return this.meal;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }
}