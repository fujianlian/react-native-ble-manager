//
//  DiscoverPeripheral.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.samplelibs.ble.blenativewrapper;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiscoverPeripheral implements Parcelable {

    /**
     * Implement the Parcelable interface
     */
    public static final Creator<DiscoverPeripheral> CREATOR =
            new Creator<DiscoverPeripheral>() {
                public DiscoverPeripheral createFromParcel(Parcel in) {
                    return new DiscoverPeripheral(in);
                }

                public DiscoverPeripheral[] newArray(int size) {
                    return new DiscoverPeripheral[size];
                }
            };
    private static final String PARCEL_EXTRA_BLUETOOTH_DEVICE = "bluetooth_device";
    private static final String PARCEL_EXTRA_CURRENT_RSSI = "current_rssi";
    private static final String PARCEL_EXTRA_SCAN_RECORD = "device_scan_record";
    private static final String PARCEL_EXTRA_PREVIOUS_TIME = "previous_time";
    private static final String PARCEL_EXTRA_UPDATE_TIME = "update_time";
    @NonNull
    private BluetoothDevice mBluetoothDevice;
    private int mCurrentRssi;
    private byte[] mScanRecord;
    private long mPreviousTime;
    private long mUpdateTime;
    private List<BleAdvertiseData> mAdvertiseDataList;

    public DiscoverPeripheral(@NonNull final BluetoothDevice bluetoothDevice, final int rssi, final byte[] scanRecord) {
        mBluetoothDevice = bluetoothDevice;
        mCurrentRssi = rssi;
        mScanRecord = scanRecord;
        mPreviousTime = System.currentTimeMillis();
        mUpdateTime = System.currentTimeMillis();
        mAdvertiseDataList = parseAdvertiseData(scanRecord);
    }

    protected DiscoverPeripheral(Parcel in) {
        final Bundle b = in.readBundle(getClass().getClassLoader());
        mBluetoothDevice = b.getParcelable(PARCEL_EXTRA_BLUETOOTH_DEVICE);
        mCurrentRssi = b.getInt(PARCEL_EXTRA_CURRENT_RSSI, 0);
        mScanRecord = b.getByteArray(PARCEL_EXTRA_SCAN_RECORD);
        mPreviousTime = b.getLong(PARCEL_EXTRA_PREVIOUS_TIME, 0);
        mUpdateTime = b.getLong(PARCEL_EXTRA_UPDATE_TIME, 0);
        mAdvertiseDataList = parseAdvertiseData(mScanRecord);
    }

    void update(final int rssi, final byte[] scanRecord) {
        mCurrentRssi = rssi;
        mScanRecord = scanRecord;
        mPreviousTime = mUpdateTime;
        mUpdateTime = System.currentTimeMillis();
        mAdvertiseDataList = parseAdvertiseData(scanRecord);

    }

    @NonNull
    public BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }

    public long getAdvertisingInterval() {
        return mUpdateTime - mPreviousTime;
    }

    @NonNull
    public String getAddress() {
        return mBluetoothDevice.getAddress();
    }

    @NonNull
    public String getLocalName() {
        return mBluetoothDevice.getName();
    }

    public int getBondState() {
        return mBluetoothDevice.getBondState();
    }

    public int getRssi() {
        return mCurrentRssi;
    }

    public byte[] getScanRecord() {
        return mScanRecord.clone();
    }

    public List<BleAdvertiseData> getAdvertiseDataList() {
        return mAdvertiseDataList;
    }

    private static List<BleAdvertiseData> parseAdvertiseData(byte[] payload) {
        if (payload == null) {
            return null;
        }

        List<BleAdvertiseData> list = new ArrayList<>();

        for (int i = 0; i < payload.length; ) {
            int len = payload[i] & 0xFF;

            if (len == 0) {
                break;
            }

            if ((payload.length - i - 1) < len) {
                break;
            }

            int type = payload[i + 1] & 0xFF;
            byte[] data = Arrays.copyOfRange(payload, i + 2, i + len + 1);

            BleAdvertiseData ads = new BleAdvertiseData(len, type, data);
            list.add(ads);

            i += 1 + len;
        }

        return list;
    }

    /**
     * Implement the Parcelable interface
     */
    public int describeContents() {
        return 0;
    }

    /**
     * Implement the Parcelable interface
     */
    public void writeToParcel(Parcel dest, int flags) {
        final Bundle b = new Bundle(getClass().getClassLoader());
        b.putParcelable(PARCEL_EXTRA_BLUETOOTH_DEVICE, mBluetoothDevice);
        b.putInt(PARCEL_EXTRA_CURRENT_RSSI, mCurrentRssi);
        b.putByteArray(PARCEL_EXTRA_SCAN_RECORD, mScanRecord);
        b.putLong(PARCEL_EXTRA_PREVIOUS_TIME, mPreviousTime);
        b.putLong(PARCEL_EXTRA_UPDATE_TIME, mUpdateTime);
        dest.writeBundle(b);
    }
}
