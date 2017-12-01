//
//  BlePrivateConstants.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.samplelibs.ble.blenativewrapper;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.os.Build;

class BlePrivateConstants {

    public static final int BLE_PERIPHERAL_EVT_BASE = 0x10000000;
    public static final int BLE_RECEIVER_EVT_BASE = 0x20000000;
    public static final int LOCAL_EVT_BASE = 0xf0000000;

    // Reference
    //   /frameworks/base/core/java/android/bluetooth/BluetoothDevice.java
    public static final int PAIRING_VARIANT_PIN = 0;
    public static final int PAIRING_VARIANT_PASSKEY = 1;
    public static final int PAIRING_VARIANT_PASSKEY_CONFIRMATION = 2;
    public static final int PAIRING_VARIANT_CONSENT = 3;
    public static final int PAIRING_VARIANT_DISPLAY_PASSKEY = 4;
    public static final int PAIRING_VARIANT_DISPLAY_PIN = 5;
    public static final int PAIRING_VARIANT_OOB_CONSENT = 6;
    public static final int PAIRING_VARIANT_PIN_16_DIGITS = 7;

    public static final String ACTION_PAIRING_REQUEST;
    public static final String EXTRA_PAIRING_VARIANT;

    static {
        if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
            ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
        } else {
            ACTION_PAIRING_REQUEST = actionPairingRequestStringFromKitkat();
        }
    }

    static {
        if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
            EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
        } else {
            EXTRA_PAIRING_VARIANT = extraPairingVariantStringFromKitkat();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String actionPairingRequestStringFromKitkat() {
        return BluetoothDevice.ACTION_PAIRING_REQUEST;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String extraPairingVariantStringFromKitkat() {
        return BluetoothDevice.EXTRA_PAIRING_VARIANT;
    }
}
