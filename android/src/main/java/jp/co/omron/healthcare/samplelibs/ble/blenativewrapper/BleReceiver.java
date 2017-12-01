//
//  BleReceiver.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.samplelibs.ble.blenativewrapper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

class BleReceiver {

    private static final int EVT_BASE = BlePrivateConstants.BLE_RECEIVER_EVT_BASE;
    public static final int EVT_BLUETOOTH_STATE_CHANGED = EVT_BASE + 0x0001;
    public static final int EVT_ACL_CONNECTED = EVT_BASE + 0x0002;
    public static final int EVT_ACL_DISCONNECTED = EVT_BASE + 0x0003;
    public static final int EVT_BOND_NONE = EVT_BASE + 0x0004;
    public static final int EVT_BONDING = EVT_BASE + 0x0005;
    public static final int EVT_BONDED = EVT_BASE + 0x0006;
    public static final int EVT_PAIRING_REQUEST = EVT_BASE + 0x0007;

    private final Context mContext;
    private final Handler mDstHandler;
    private boolean mReceiverRegistered;
    private String mAddressFilter;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(@NonNull Context context, @NonNull Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                sendBluetoothStateChangedMessage(intent);
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                sendAclConnectedMessage(intent);
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                sendAclDisconnectedMessage(intent);
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                sendBondStateChangedMessage(intent);
            } else if (BlePrivateConstants.ACTION_PAIRING_REQUEST.equals(action)) {
                sendPairingRequestMessage(intent);
            }
        }
    };

    public BleReceiver(@NonNull final Context context, @NonNull final Handler dstHandler) {
        mContext = context;
        mDstHandler = dstHandler;
        mAddressFilter = null;
    }

    public void setAddressFilter(String address) {
        mAddressFilter = address;
    }

    public void registerReceiver() {
        if (mReceiverRegistered) {
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BlePrivateConstants.ACTION_PAIRING_REQUEST);
        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
        mReceiverRegistered = true;
    }

    public void unregisterReceiver() {
        if (!mReceiverRegistered) {
            return;
        }
        mContext.unregisterReceiver(mBroadcastReceiver);
        mReceiverRegistered = false;
    }

    private void sendBluetoothStateChangedMessage(@NonNull final Intent intent) {
        int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
        BleLog.i("Received ACTION_STATE_CHANGED[" + bluetoothState + "].");
        mDstHandler.sendMessage(Message.obtain(mDstHandler, EVT_BLUETOOTH_STATE_CHANGED, bluetoothState, 0));
    }

    private void sendAclConnectedMessage(@NonNull final Intent intent) {
        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (null != mAddressFilter && !mAddressFilter.equals(bluetoothDevice.getAddress())) {
            BleLog.w("Ignore ACTION_ACL_CONNECTED. target:" + bluetoothDevice.getAddress());
            return;
        }
        BleLog.i("Received ACTION_ACL_CONNECTED.");
        mDstHandler.sendMessage(Message.obtain(mDstHandler, EVT_ACL_CONNECTED, bluetoothDevice));
    }

    private void sendAclDisconnectedMessage(@NonNull final Intent intent) {
        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (null != mAddressFilter && !mAddressFilter.equals(bluetoothDevice.getAddress())) {
            BleLog.w("Ignore ACTION_ACL_DISCONNECTED. target:" + bluetoothDevice.getAddress());
            return;
        }
        BleLog.i("Received ACTION_ACL_DISCONNECTED.");
        mDstHandler.sendMessage(Message.obtain(mDstHandler, EVT_ACL_DISCONNECTED, bluetoothDevice));
    }

    private void sendBondStateChangedMessage(@NonNull final Intent intent) {
        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (null != mAddressFilter && !mAddressFilter.equals(bluetoothDevice.getAddress())) {
            BleLog.w("Ignore ACTION_BOND_STATE_CHANGED. target:" + bluetoothDevice.getAddress());
            return;
        }
        int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE);
        int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
        BleLog.i("Received ACTION_BOND_STATE_CHANGED[" + previousBondState + " -> " + bondState + "].");
        int event;
        switch (bondState) {
            case BluetoothDevice.BOND_NONE:
                event = EVT_BOND_NONE;
                break;
            case BluetoothDevice.BOND_BONDING:
                event = EVT_BONDING;
                break;
            case BluetoothDevice.BOND_BONDED:
                event = EVT_BONDED;
                break;
            default:
                BleLog.e("Unknown state.");
                event = EVT_BOND_NONE;
                break;
        }
        mDstHandler.sendMessage(Message.obtain(mDstHandler, event, bluetoothDevice));
    }

    private void sendPairingRequestMessage(@NonNull final Intent intent) {
        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (null != mAddressFilter && !mAddressFilter.equals(bluetoothDevice.getAddress())) {
            BleLog.w("Ignore ACTION_PAIRING_REQUEST. target:" + bluetoothDevice.getAddress());
            return;
        }
        int variant = intent.getIntExtra(BlePrivateConstants.EXTRA_PAIRING_VARIANT, -1);
        BleLog.i("Received ACTION_PAIRING_REQUEST. variant:" + variant);
        mDstHandler.sendMessage(Message.obtain(mDstHandler, EVT_PAIRING_REQUEST, variant, -1, bluetoothDevice));
    }
}
