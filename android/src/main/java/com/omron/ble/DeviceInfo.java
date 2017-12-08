package com.omron.ble;

import android.content.ContentValues;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

//import com.zhizhong.mmcassistant.exception.DbAbNormalException;

import org.litepal.crud.DataSupport;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by 123 on 2016/6/17.
 */
public class DeviceInfo extends DataSupport {

    public static final int NOT_PRINT = 0;
    public static final int PRINT = 1;

    @IntDef({NOT_PRINT, PRINT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PrintStatus {
    }

    public static final int NOT_ADD = 0;
    public static final int ADD = 1;

    @IntDef({NOT_ADD, ADD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DeviceMeasureStatus {
    }

    public static final String HEM_7081_IT = "hem_7081_it";
    public static final String HGM_126T = "hgm_126T";

    @StringDef({HEM_7081_IT, HGM_126T})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BondDeviceType {
    }

    /* 标识id */
    private int id;

    /* 是否打印报告信息,0:不打印 1：打印 */
    @PrintStatus
    private short isPrintReport;

    /* 是否添加血压测量流程,0：不添加 1：添加 */
    @DeviceMeasureStatus
    private short isMeasureBp;

    /* 是否添加血糖测量流程,0：不添加 1：添加 */
    @DeviceMeasureStatus
    private short isMeasureBg;

    /* 血压设备绑定信息 */
    @BondDeviceType
    private String bpBondInfo;

    /* 血糖设备绑定信息 */
    @BondDeviceType
    private String bgBondInfo;

    /* 打印机设备绑定信息 */
    @BondDeviceType
    private String printBondInfo;

    /* 血糖绑定pincode信息 */
    private String bgPincode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @PrintStatus
    public short getIsPrintReport() {
        return isPrintReport;
    }

    @PrintStatus
    public void setIsPrintReport(short isPrintReport) {
        this.isPrintReport = isPrintReport;
    }

    @DeviceMeasureStatus
    public short getIsMeasureBp() {
        return isMeasureBp;
    }

    @DeviceMeasureStatus
    public void setIsMeasureBp(short isMeasureBp) {
        this.isMeasureBp = isMeasureBp;
    }

    @DeviceMeasureStatus
    public short getIsMeasureBg() {
        return isMeasureBg;
    }

    @DeviceMeasureStatus
    public void setIsMeasureBg(short isMeasureBg) {
        this.isMeasureBg = isMeasureBg;
    }

    @BondDeviceType
    public String getBpBondInfo() {
        return bpBondInfo;
    }

    @BondDeviceType
    public void setBpBondInfo(String bpBondInfo) {
        this.bpBondInfo = bpBondInfo;
    }

    @BondDeviceType
    public String getBgBondInfo() {
        return bgBondInfo;
    }

    @BondDeviceType
    public void setBgBondInfo(String bgBondInfo) {
        this.bgBondInfo = bgBondInfo;
    }

    @BondDeviceType
    public String getPrintBondInfo() {
        return printBondInfo;
    }

    @BondDeviceType
    public void setPrintBondInfo(String printBondInfo) {
        this.printBondInfo = printBondInfo;
    }

    public String getBgPincode() {
        return bgPincode;
    }

    public void setBgPincode(String bgPincode) {
        this.bgPincode = bgPincode;
    }

    public static void savePincode(String pincode) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setBgPincode(pincode);
        deviceInfo.save();
    }

    public static void updateDeviceInfo(String bgPincode) {
        ContentValues values = new ContentValues();
        values.put("bgpincode", bgPincode);
        DataSupport.updateAll(DeviceInfo.class, values);
    }

    public static List<DeviceInfo> getDeviceInfo() {
        List<DeviceInfo> infoList =  DataSupport.findAll(DeviceInfo.class);
        if (infoList != null && infoList.size() > 1) {
            throw new DbAbNormalException("It should be only one device info in database. " +
                    "But there is more than one");
        }
        return infoList;
    }

    public static boolean isDeviceInfoEmpty() {
        List<DeviceInfo> infoList = getDeviceInfo();
        if (infoList.isEmpty()) {
            return true;
        }
        return false;
    }

    public static void saveDefaultSetting() {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setIsPrintReport((short) DeviceInfo.PRINT);
        deviceInfo.setIsMeasureBg((short) DeviceInfo.ADD);
        deviceInfo.setIsMeasureBp((short) DeviceInfo.ADD);
        deviceInfo.save();
    }
}
