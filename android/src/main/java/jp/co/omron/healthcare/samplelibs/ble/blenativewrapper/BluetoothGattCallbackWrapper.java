//
//  BluetoothGattCallbackWrapper.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.samplelibs.ble.blenativewrapper;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

public abstract class BluetoothGattCallbackWrapper {

    public void onConnectionStateChange(int status, int newState) {
    }

    public void onServicesDiscovered(int status) {
    }

    public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
    }

    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
    }

    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
    }

    public void onDescriptorRead(BluetoothGattDescriptor descriptor, int status) {
    }

    public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
    }

    public void onReliableWriteCompleted(int status) {
    }

    public void onReadRemoteRssi(int rssi, int status) {
    }

    public void onMtuChanged(int mtu, int status) {
    }
}
