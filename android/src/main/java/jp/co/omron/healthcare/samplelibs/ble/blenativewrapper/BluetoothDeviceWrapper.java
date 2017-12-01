//
//  BluetoothDeviceWrapper.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.samplelibs.ble.blenativewrapper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

class BluetoothDeviceWrapper {
    @NonNull
    private final Context mContext;
    @NonNull
    private final BluetoothDevice mBluetoothDevice;
    @Nullable
    private BluetoothGatt mBluetoothGatt;
    @Nullable
    private BluetoothGattCallbackWrapper mBluetoothGattCallbackWrapper;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BleLog.i(String.format(Locale.US, "status=%d(0x%02x)", status, status) + " " +
                    String.format(Locale.US, "newState=%d(0x%02x)", newState, newState));
            if (null != mBluetoothGattCallbackWrapper) {
                mBluetoothGattCallbackWrapper.onConnectionStateChange(status, newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BleLog.i(String.format(Locale.US, "status=%d(0x%02x)", status, status));
            if (null != mBluetoothGattCallbackWrapper) {
                mBluetoothGattCallbackWrapper.onServicesDiscovered(status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            BleLog.i(GattUUID.Characteristic.valueOf(characteristic.getUuid()).name() + " " +
                    String.format(Locale.US, "status=%d(0x%02x)", status, status));
            if (null != mBluetoothGattCallbackWrapper) {
                mBluetoothGattCallbackWrapper.onCharacteristicRead(characteristic, status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            BleLog.i(GattUUID.Characteristic.valueOf(characteristic.getUuid()).name() + " " +
                    String.format(Locale.US, "status=%d(0x%02x)", status, status));
            if (null != mBluetoothGattCallbackWrapper) {
                mBluetoothGattCallbackWrapper.onCharacteristicWrite(characteristic, status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            BleLog.i(GattUUID.Characteristic.valueOf(characteristic.getUuid()).name());
            if (null != mBluetoothGattCallbackWrapper) {
                mBluetoothGattCallbackWrapper.onCharacteristicChanged(characteristic);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BleLog.i(GattUUID.Characteristic.valueOf(descriptor.getCharacteristic().getUuid()).name() + " " +
                    GattUUID.Descriptor.valueOf(descriptor.getUuid()).name() + " " +
                    String.format(Locale.US, "status=%d(0x%02x)", status, status));
            if (null != mBluetoothGattCallbackWrapper) {
                mBluetoothGattCallbackWrapper.onDescriptorRead(descriptor, status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BleLog.i(GattUUID.Characteristic.valueOf(descriptor.getCharacteristic().getUuid()).name() + " " +
                    GattUUID.Descriptor.valueOf(descriptor.getUuid()).name() + " " +
                    String.format(Locale.US, "status=%d(0x%02x)", status, status));
            if (null != mBluetoothGattCallbackWrapper) {
                mBluetoothGattCallbackWrapper.onDescriptorWrite(descriptor, status);
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            BleLog.i(String.format(Locale.US, "status=%d(0x%02x)", status, status));
            if (null != mBluetoothGattCallbackWrapper) {
                mBluetoothGattCallbackWrapper.onReliableWriteCompleted(status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            BleLog.e("rssi=" + rssi + " " + String.format(Locale.US, "status=%d(0x%02x) ", status, status));
            if (null != mBluetoothGattCallbackWrapper) {
                mBluetoothGattCallbackWrapper.onReadRemoteRssi(rssi, status);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            BleLog.e("mtu=" + mtu + " " + String.format(Locale.US, "status=%d(0x%02x) ", status, status));
            if (null != mBluetoothGattCallbackWrapper) {
                mBluetoothGattCallbackWrapper.onMtuChanged(mtu, status);
            }
        }
    };

    BluetoothDeviceWrapper(@NonNull Context context, @NonNull final BluetoothDevice bluetoothDevice) {
        mContext = context;
        mBluetoothDevice = bluetoothDevice;
        mBluetoothGatt = null;
        mBluetoothGattCallbackWrapper = null;
    }

    private static Object invokeMethod(Object target, String methodName, Class<?>[] parameterClasses, Object[] paramterValues)
            throws IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Class<?> clazz = target.getClass();
        Method method = clazz.getDeclaredMethod(methodName, parameterClasses);
        return method.invoke(target, paramterValues);
    }

    public String getAddress() {
        return mBluetoothDevice.getAddress();
    }

    public String getLocalName() {
        return mBluetoothDevice.getName();
    }

    public int getBondState() {
        return mBluetoothDevice.getBondState();
    }

    @SuppressLint("NewApi")
    public boolean createBond() {
        boolean ret = false;
        BleLog.i("createBond() call.");
        if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
            try {
                ret = (Boolean) invokeMethod(mBluetoothDevice, "createBond", null, null);
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else {
            ret = mBluetoothDevice.createBond();
        }
        if (ret) {
            BleLog.d("createBond() called. ret=true");
        } else {
            BleLog.e("createBond() called. ret=false");
        }
        return ret;
    }

    public boolean cancelBondProcess() {
        boolean ret = false;
        BleLog.i("cancelBondProcess() call.");
        try {
            ret = (Boolean) invokeMethod(mBluetoothDevice, "cancelBondProcess", null, null);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (ret) {
            BleLog.d("cancelBondProcess() called. ret=true");
        } else {
            BleLog.e("cancelBondProcess() called. ret=false");
        }
        return ret;
    }

    public boolean removeBond() {
        boolean ret = false;
        BleLog.i("removeBond() call.");
        try {
            ret = (Boolean) invokeMethod(mBluetoothDevice, "removeBond", null, null);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (ret) {
            BleLog.d("removeBond() called. ret=true");
        } else {
            BleLog.e("removeBond() called. ret=false");
        }
        return ret;
    }

    public boolean isBonded() {
        return BluetoothDevice.BOND_BONDED == mBluetoothDevice.getBondState();
    }

    @SuppressLint("NewApi")
    public boolean setPairingConfirmation(boolean enable) {
        boolean ret = false;
        BleLog.i("setPairingConfirmation(" + enable + ") call.");
        if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
            try {
                ret = (Boolean) invokeMethod(
                        mBluetoothDevice,
                        "setPairingConfirmation",
                        new Class<?>[]{boolean.class},
                        new Object[]{enable}
                );
            } catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            ret = mBluetoothDevice.setPairingConfirmation(enable);
        }
        if (ret) {
            BleLog.d("setPairingConfirmation() called. ret=true");
        } else {
            BleLog.e("setPairingConfirmation() called. ret=false");
        }
        return ret;
    }

    @SuppressLint("NewApi")
    public boolean setPin(String pinCode) {
        boolean ret = false;
        byte[] pin = convertPinToBytes(pinCode);
        if (null == pin) {
            BleLog.e("null == pin");
            return false;
        }

        BleLog.i("setPin(" + pinCode + ") call.");
        if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
            try {
                ret = (Boolean) invokeMethod(
                        mBluetoothDevice,
                        "setPin",
                        new Class<?>[]{byte[].class},
                        new Object[]{pin}
                );
            } catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            ret = mBluetoothDevice.setPin(pin);
        }
        if (ret) {
            BleLog.d("setPin() called. ret=true");
        } else {
            BleLog.e("setPin() called. ret=false");
        }
        return ret;
    }

    public boolean setPasskey(String pinCode) {
        boolean ret = false;
        BleLog.i("setPasskey(" + pinCode + ") call.");
        try {
            ByteBuffer converter = ByteBuffer.allocate(4);
            converter.order(ByteOrder.nativeOrder());
            converter.putInt(Integer.parseInt(pinCode));
            byte[] pin = converter.array();
            ret = (Boolean) invokeMethod(
                    invokeMethod(BluetoothDevice.class, "getService", null, null),
                    "setPasskey",
                    new Class<?>[]{BluetoothDevice.class, boolean.class, int.class, byte[].class},
                    new Object[]{mBluetoothDevice, true, pin.length, pin}
            );
        } catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        if (ret) {
            BleLog.d("setPasskey() called. ret=true");
        } else {
            BleLog.e("setPasskey() called. ret=false");
        }
        return ret;
    }

    public byte[] convertPinToBytes(String pin) {
        byte[] ret = null;
        try {
            Class<?>[] types = {
                    String.class
            };
            Object[] args = {
                    pin
            };
            ret = (byte[]) invokeMethod(mBluetoothDevice, "convertPinToBytes", types, args);
        } catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public boolean hasGatt() {
        return null != mBluetoothGatt;
    }

    public boolean connectGatt(@NonNull Context context, BluetoothGattCallbackWrapper bluetoothGattCallbackWrapper) {
        boolean ret = false;
        if (null == bluetoothGattCallbackWrapper) {
            BleLog.e("null == bluetoothGattCallbackWrapper");
            return false;
        }

        mBluetoothGattCallbackWrapper = bluetoothGattCallbackWrapper;
        if (null != mBluetoothGatt) {
            BleLog.i("connect() call.");
            ret = mBluetoothGatt.connect();
            if (ret) {
                BleLog.d("connect() called. ret=true");
            } else {
                BleLog.e("connect() called. ret=false");
            }
        } else {
            BleLog.i("connectGatt() call.");
            mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, mGattCallback);
            if (null != mBluetoothGatt) {
                ret = true;
                BleLog.d("connectGatt() called. ret=Not Null");
            } else {
                BleLog.e("connectGatt() called. ret=Null");
            }
        }
        return ret;
    }

    public boolean disconnectGatt() {
        if (null == mBluetoothGatt) {
            BleLog.e("null == mBluetoothGatt");
            return false;
        }
        BleLog.i("disconnect() call.");
        mBluetoothGatt.disconnect();
        BleLog.d("disconnect() called.");
        return true;
    }

    /**
     * @return BluetoothProfile.STATE_DISCONNECTED
     * BluetoothProfile.STATE_CONNECTING
     * BluetoothProfile.STATE_CONNECTED
     * BluetoothProfile.STATE_DISCONNECTING
     */
    public int getGattState() {
        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        return bluetoothManager.getConnectionState(mBluetoothDevice, BluetoothProfile.GATT);
    }

    public boolean discoverServices() {
        if (null == mBluetoothGatt) {
            BleLog.e("null == mBluetoothGatt");
            return false;
        }
        BleLog.i("discoverServices() call.");
        boolean ret = mBluetoothGatt.discoverServices();
        if (ret) {
            BleLog.d("discoverServices() called. ret=true");
        } else {
            BleLog.e("discoverServices() called. ret=false");
        }
        return ret;
    }

    public boolean refreshGatt() {
        if (null == mBluetoothGatt) {
            BleLog.e("null == mBluetoothGatt");
            return false;
        }
        boolean ret = false;
        BleLog.i("refresh() call.");
        try {
            ret = (Boolean) invokeMethod(mBluetoothGatt, "refresh", null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ret) {
            BleLog.d("refresh() called. ret=true");
        } else {
            BleLog.e("refresh() called. ret=false");
        }
        return ret;
    }

    public boolean closeGatt() {
        if (null == mBluetoothGatt) {
            BleLog.e("null == mBluetoothGatt");
            return false;
        }
        BleLog.i("close() call.");
        mBluetoothGatt.close();
        BleLog.d("close() called.");
        mBluetoothGatt = null;
        mBluetoothGattCallbackWrapper = null;
        return true;
    }

    @Nullable
    public BluetoothGattService getService(@NonNull final UUID uuid) {
        if (null == mBluetoothGatt) {
            BleLog.e("null == mBluetoothGatt");
            return null;
        }
        BleLog.i("getService(" + uuid.toString() + ") call.");
        BluetoothGattService ret = mBluetoothGatt.getService(uuid);
        if (null != ret) {
            BleLog.d("getService() called. ret=Not Null");
        } else {
            BleLog.e("getService() called. ret=Null");
        }
        return mBluetoothGatt.getService(uuid);
    }

    @Nullable
    public List<BluetoothGattService> getServices() {
        if (null == mBluetoothGatt) {
            BleLog.e("null == mBluetoothGatt");
            return null;
        }
        BleLog.i("getServices() call.");
        List<BluetoothGattService> ret = mBluetoothGatt.getServices();
        if (null != ret) {
            if (0 == ret.size()) {
                BleLog.d("getServices() called. ret.size=0");
            } else {
                BleLog.d("getServices() called. ret=Not Null");
            }
        } else {
            BleLog.e("getServices() called. ret=Null");
        }
        return ret;
    }

    public boolean setCharacteristicNotification(@NonNull final BluetoothGattCharacteristic characteristic, boolean enable) {
        if (null == mBluetoothGatt) {
            BleLog.e("null == mBluetoothGatt");
            return false;
        }
        BleLog.i("setCharacteristicNotification(" + GattUUID.Characteristic.valueOf(characteristic.getUuid()).name() + ", " + enable + ") call.");
        boolean ret = mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
        if (ret) {
            BleLog.d("setCharacteristicNotification() called. ret=true");
        } else {
            BleLog.e("setCharacteristicNotification() called. ret=false");
        }
        return ret;
    }

    public boolean readCharacteristic(@NonNull final BluetoothGattCharacteristic characteristic) {
        if (null == mBluetoothGatt) {
            BleLog.e("null == mBluetoothGatt");
            return false;
        }
        BleLog.i("readCharacteristic(" + GattUUID.Characteristic.valueOf(characteristic.getUuid()).name() + ") call.");
        boolean ret = mBluetoothGatt.readCharacteristic(characteristic);
        if (ret) {
            BleLog.d("readCharacteristic() called. ret=true");
        } else {
            BleLog.e("readCharacteristic() called. ret=false");
        }
        return ret;
    }

    public boolean writeCharacteristic(@NonNull final BluetoothGattCharacteristic characteristic) {
        if (null == mBluetoothGatt) {
            BleLog.e("null == mBluetoothGatt");
            return false;
        }
        BleLog.i("writeCharacteristic(" + GattUUID.Characteristic.valueOf(characteristic.getUuid()).name() + ") call.");
        boolean ret = mBluetoothGatt.writeCharacteristic(characteristic);
        if (ret) {
            BleLog.d("writeCharacteristic() called. ret=true");
        } else {
            BleLog.e("writeCharacteristic() called. ret=false");
        }
        return ret;
    }

    public boolean readDescriptor(@NonNull final BluetoothGattDescriptor descriptor) {
        if (null == mBluetoothGatt) {
            BleLog.e("null == mBluetoothGatt");
            return false;
        }
        BleLog.i("readDescriptor(" + GattUUID.Characteristic.valueOf(descriptor.getCharacteristic().getUuid()).name() + ", " +
                GattUUID.Descriptor.valueOf(descriptor.getUuid()).name() + ") call.");
        boolean ret = mBluetoothGatt.readDescriptor(descriptor);
        if (ret) {
            BleLog.d("readDescriptor() called. ret=true");
        } else {
            BleLog.e("readDescriptor() called. ret=false");
        }
        return ret;
    }

    public boolean writeDescriptor(@NonNull final BluetoothGattDescriptor descriptor) {
        if (null == mBluetoothGatt) {
            BleLog.e("null == mBluetoothGatt");
            return false;
        }
        BleLog.i("writeDescriptor(" + GattUUID.Characteristic.valueOf(descriptor.getCharacteristic().getUuid()).name() + ", " +
                GattUUID.Descriptor.valueOf(descriptor.getUuid()).name() + ") call.");
        boolean ret = mBluetoothGatt.writeDescriptor(descriptor);
        if (ret) {
            BleLog.d("writeDescriptor() called. ret=true");
        } else {
            BleLog.e("writeDescriptor() called. ret=false");
        }
        return ret;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean requestMtu(int mtu) {
        if (null == mBluetoothGatt) {
            BleLog.e("null == mBluetoothGatt");
            return false;
        }
        if (Build.VERSION_CODES.LOLLIPOP > Build.VERSION.SDK_INT) {
            BleLog.e("VERSION_CODES.LOLLIPOP > VERSION.SDK_INT");
            return false;
        }
        BleLog.i("requestMtu(" + mtu + ") call.");
        boolean ret = mBluetoothGatt.requestMtu(mtu);
        if (ret) {
            BleLog.d("requestMtu() called. ret=true");
        } else {
            BleLog.e("requestMtu() called. ret=false");
        }
        return ret;
    }
}
