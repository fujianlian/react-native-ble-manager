//
//  CharacteristicProperty.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.samplelibs.ble.blenativewrapper;

import android.bluetooth.BluetoothGattCharacteristic;

public enum CharacteristicProperty {
    Broadcast(BluetoothGattCharacteristic.PROPERTY_BROADCAST),
    Read(BluetoothGattCharacteristic.PERMISSION_READ),
    WriteTypeNoResponse(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE),
    Write(BluetoothGattCharacteristic.PROPERTY_WRITE),
    Notify(BluetoothGattCharacteristic.PROPERTY_NOTIFY),
    Indicate(BluetoothGattCharacteristic.PROPERTY_INDICATE),
    SignedWrite(BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE),
    ExtendedProps(BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS),
    NotifyEncryptionRequired(0x0100),
    IndicateEncryptionRequired(0x0200),;
    private int mValue;

    CharacteristicProperty(int value) {
        mValue = value;
    }

    static boolean contains(int source, CharacteristicProperty property) {
        return (property.value() == (source & property.value()));
    }

    public static CharacteristicProperty valueOf(int value) {
        for (CharacteristicProperty type : values()) {
            if (type.value() == value) {
                return type;
            }
        }
        return null;
    }

    public int value() {
        return mValue;
    }
}
