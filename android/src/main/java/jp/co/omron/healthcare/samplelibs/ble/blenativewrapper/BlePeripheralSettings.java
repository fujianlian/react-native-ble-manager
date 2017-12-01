//
//  BlePeripheralSettings.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.samplelibs.ble.blenativewrapper;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlePeripheralSettings {

    public static final boolean USE_CREATE_BOND_DEFAULT = true;
    public static final boolean USE_REMOVE_BOND_DEFAULT = false;
    public static final boolean USE_REFRESH_GATT_DEFAULT = false;
    public static final boolean ASSIST_PAIRING_DIALOG_DEFAULT = false;
    public static final boolean ENABLE_AUTO_PAIRING_DEFAULT = false;
    public static final String AUTO_PAIRING_PIN_CODE_DEFAULT = "000000";
    public static final boolean ENABLE_CONNECT_RETRY_DEFAULT = true;
    public static final int CONNECT_RETRY_COUNT_DEFAULT = 5;
    public static final long DISCOVER_SERVICE_DELAY_TIME_DEFAULT = 0;

    // These parameter are for following cases.
    // 1. For notification from BONDED to BONDING immediately after GATT_CONNCTED
    // 2. For notification of GATT_DISCONNECTED(status=133) immediately after GATT_CONNCTED
    // Change next state after wait defined time for stable connection.
    // The wait time is defined as 1500ms because Nexus 6 (Android 5.1) needs about 1200ms for stable connection in case 2.
    public static final boolean STABLE_CONNECTION_DEFAULT = true;
    public static final int STABLE_CONNECTION_WAIT_TIME_DEFAULT = 1500;

    @NonNull
    private final String mDeviceId;
    boolean UseCreateBond = USE_CREATE_BOND_DEFAULT;
    boolean UseRemoveBond = USE_REMOVE_BOND_DEFAULT;
    boolean UseRefreshGatt = USE_REFRESH_GATT_DEFAULT;
    boolean AssistPairingDialog = ASSIST_PAIRING_DIALOG_DEFAULT;
    boolean EnableAutoPairing = ENABLE_AUTO_PAIRING_DEFAULT;
    String AutoPairingPinCode = AUTO_PAIRING_PIN_CODE_DEFAULT;
    boolean EnableConnectRetry = ENABLE_CONNECT_RETRY_DEFAULT;
    int ConnectRetryCount = CONNECT_RETRY_COUNT_DEFAULT;
    long DiscoverServiceDelayTime = DISCOVER_SERVICE_DELAY_TIME_DEFAULT;
    boolean StableConnection = STABLE_CONNECTION_DEFAULT;
    long StableConnectionWaitTime = STABLE_CONNECTION_WAIT_TIME_DEFAULT;

    BlePeripheralSettings(@NonNull String deviceId) {
        mDeviceId = deviceId;
    }

    public void setParameter(@NonNull final Bundle bundle) {
        BleLog.dMethodIn("DeviceId:" + mDeviceId);

        BleLog.d("bundle:" + bundle);

        if (bundle.containsKey(Key.UseCreateBond.name())) {
            UseCreateBond = bundle.getBoolean(Key.UseCreateBond.name());
        }

        if (bundle.containsKey(Key.UseRemoveBond.name())) {
            UseRemoveBond = bundle.getBoolean(Key.UseRemoveBond.name());
        }

        if (bundle.containsKey(Key.UseRefreshGatt.name())) {
            UseRefreshGatt = bundle.getBoolean(Key.UseRefreshGatt.name());
        }

        if (bundle.containsKey(Key.AssistPairingDialog.name())) {
            AssistPairingDialog = bundle.getBoolean(Key.AssistPairingDialog.name());
        }

        if (bundle.containsKey(Key.EnableAutoPairing.name())) {
            EnableAutoPairing = bundle.getBoolean(Key.EnableAutoPairing.name());
        }

        if (bundle.containsKey(Key.AutoPairingPinCode.name())) {
            AutoPairingPinCode = bundle.getString(Key.AutoPairingPinCode.name());
        }

        if (bundle.containsKey(Key.EnableConnectRetry.name())) {
            EnableConnectRetry = bundle.getBoolean(Key.EnableConnectRetry.name());
        }

        if (bundle.containsKey(Key.ConnectRetryCount.name())) {
            ConnectRetryCount = bundle.getInt(Key.ConnectRetryCount.name());
        }

        if (bundle.containsKey(Key.DiscoverServiceDelayTime.name())) {
            DiscoverServiceDelayTime = bundle.getLong(Key.DiscoverServiceDelayTime.name());
        }

        if (bundle.containsKey(Key.StableConnection.name())) {
            StableConnection = bundle.getBoolean(Key.StableConnection.name());
        }

        if (bundle.containsKey(Key.StableConnectionWaitTime.name())) {
            StableConnectionWaitTime = bundle.getLong(Key.StableConnectionWaitTime.name());
        }
    }

    @NonNull
    public Bundle getParameter(@Nullable List<String> keys) {
        BleLog.dMethodIn("DeviceId:" + mDeviceId);

        final Bundle bundle = new Bundle();
        if (null == keys) {
            BleLog.d("get all parameters.");
            final ArrayList<String> allKeys = new ArrayList<>();
            allKeys.add(Key.UseCreateBond.name());
            allKeys.add(Key.UseRemoveBond.name());
            allKeys.add(Key.UseRefreshGatt.name());
            allKeys.add(Key.AssistPairingDialog.name());
            allKeys.add(Key.EnableAutoPairing.name());
            allKeys.add(Key.AutoPairingPinCode.name());
            allKeys.add(Key.EnableConnectRetry.name());
            allKeys.add(Key.ConnectRetryCount.name());
            allKeys.add(Key.DiscoverServiceDelayTime.name());
            allKeys.add(Key.StableConnection.name());
            allKeys.add(Key.StableConnectionWaitTime.name());
            keys = allKeys;
        }
        for (String key : keys) {
            if (Key.UseCreateBond.name().equals(key)) {
                bundle.putBoolean(Key.UseCreateBond.name(), UseCreateBond);
            }
            if (Key.UseRemoveBond.name().equals(key)) {
                bundle.putBoolean(Key.UseRemoveBond.name(), UseRemoveBond);
            }
            if (Key.UseRefreshGatt.name().equals(key)) {
                bundle.putBoolean(Key.UseRefreshGatt.name(), UseRefreshGatt);
            }
            if (Key.AssistPairingDialog.name().equals(key)) {
                bundle.putBoolean(Key.AssistPairingDialog.name(), AssistPairingDialog);
            }
            if (Key.EnableAutoPairing.name().equals(key)) {
                bundle.putBoolean(Key.EnableAutoPairing.name(), EnableAutoPairing);
            }
            if (Key.AutoPairingPinCode.name().equals(key)) {
                bundle.putString(Key.AutoPairingPinCode.name(), AutoPairingPinCode);
            }
            if (Key.EnableConnectRetry.name().equals(key)) {
                bundle.putBoolean(Key.EnableConnectRetry.name(), EnableConnectRetry);
            }
            if (Key.ConnectRetryCount.name().equals(key)) {
                bundle.putInt(Key.ConnectRetryCount.name(), ConnectRetryCount);
            }
            if (Key.DiscoverServiceDelayTime.name().equals(key)) {
                bundle.putLong(Key.DiscoverServiceDelayTime.name(), DiscoverServiceDelayTime);
            }
            if (Key.StableConnection.name().equals(key)) {
                bundle.putBoolean(Key.StableConnection.name(), StableConnection);
            }
            if (Key.StableConnectionWaitTime.name().equals(key)) {
                bundle.putLong(Key.StableConnectionWaitTime.name(), StableConnectionWaitTime);
            }
        }

        BleLog.d("bundle:" + bundle);

        return bundle;
    }

    public enum Key {
        UseCreateBond,
        UseRemoveBond,
        UseRefreshGatt,
        AssistPairingDialog,
        EnableAutoPairing,
        AutoPairingPinCode,
        EnableConnectRetry,
        ConnectRetryCount,
        DiscoverServiceDelayTime,
        StableConnection,
        StableConnectionWaitTime
    }
}
