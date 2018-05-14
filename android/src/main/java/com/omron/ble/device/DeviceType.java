
package com.omron.ble.device;

public enum DeviceType {
    HGM_124T("BLEsmart_00090006", 1),
    HGM_124T_A("HGM-124T", 1),
    HGM_125T("BLEsmart_00090007", 1),
    HGM_125T_A("HGM-125T", 1),
    HGM_126T("BLEsmart_00090008", 1),
    BLOOD_XXX("BLEsmart_00090008", 2);

    private String prefix;
    private int type;

    private DeviceType(String prefix, int type) {
        this.prefix = prefix;
        this.type = type;



    }

    public String getPrefix() {
        return this.prefix;
    }

    public int getType() {
        return this.type;
    }

    public static DeviceType[] getHGM124T(){
        return (new DeviceType[] { DeviceType.HGM_124T, DeviceType.HGM_124T_A });
    }

    public static DeviceType[] getHGM125T(){
        return (new DeviceType[] { DeviceType.HGM_125T, DeviceType.HGM_125T_A });
    }
}