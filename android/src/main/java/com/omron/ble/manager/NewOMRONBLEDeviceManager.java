package com.omron.ble.manager;

/**
 * Created by Administrator on 2018/2/1 0001.
 */
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.omron.ble.common.OMRONBLECallbackBase;
import com.omron.ble.common.OMRONBLEErrMsg;
import com.omron.ble.device.DeviceType;
import com.omron.ble.device.OMRONBLEBGMDevice;
import com.omron.ble.device.OMRONBLEDevice;
//import com.omron.ble.manager.OMRONBLEDeviceManager;

import java.util.Calendar;
import java.util.UUID;

public class NewOMRONBLEDeviceManager  {


    private static NewOMRONBLEDeviceManager bleDeviceManager;
    private static Context mContext;
    private Handler mHandler;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private static final int DEFAULT_SCAN_TIMEOUT = 10000;
    private static final int DEFAULT_SCAN_RETRY_TIMES = 3;
    private int scanTimeout = 10000;
    private int scanRetryTimes = 3;
    private int currentTimes = 0;
    private DeviceType[] deviceTypes;
    private NewOMRONBLEDeviceManager.OMRONBLEDeviceScanCB scanCallback;
    private static final String TAG = NewOMRONBLEDeviceManager.class.getSimpleName();
    private DeviceType targetDeviceType;
    private boolean mScanSingle = true;
    private boolean syncTime = true;
    private BluetoothGattServer mGattServer;
    private static final UUID CTS_SERVICE = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
    private static final UUID CHAR_CURRENT_TIME = UUID.fromString("00002A2B-0000-1000-8000-00805f9b34fb");
    private BluetoothGattService ctsService;
    private Runnable autoStopScanByPeriod = new Runnable() {
        public void run() {
//            NewOMRONBLEDeviceManager.access$008(NewOMRONBLEDeviceManager.this);
//            NewOMRONBLEDeviceManager.this.mScanning = false;
            if(NewOMRONBLEDeviceManager.this.currentTimes < NewOMRONBLEDeviceManager.this.scanRetryTimes) {
                NewOMRONBLEDeviceManager.this.scanLeDevice(true);
                Log.i(NewOMRONBLEDeviceManager.TAG, "currentTimes :" + NewOMRONBLEDeviceManager.this.currentTimes + ",timestamp:" + System.currentTimeMillis());
            } else {
                NewOMRONBLEDeviceManager.this.mScanning = false;
                NewOMRONBLEDeviceManager.this.currentTimes = 0;
                NewOMRONBLEDeviceManager.this.mBluetoothAdapter.stopLeScan(NewOMRONBLEDeviceManager.this.mLeScanCallback);
                Log.i(NewOMRONBLEDeviceManager.TAG, "Time out stop ");
                if(NewOMRONBLEDeviceManager.this.scanCallback != null) {
                    NewOMRONBLEDeviceManager.this.scanCallback.onFailure(OMRONBLEErrMsg.OMRON_BLE_ERROR_DEVICE_SCAN_TIME_OUT);
                }

                NewOMRONBLEDeviceManager.this.printError(OMRONBLEErrMsg.OMRON_BLE_ERROR_DEVICE_SCAN_TIME_OUT);
            }

        }
    };
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.i(NewOMRONBLEDeviceManager.TAG, "device name:" + device.getName());
            if(!TextUtils.isEmpty(device.getName()) && (NewOMRONBLEDeviceManager.this.targetDeviceType = NewOMRONBLEDeviceManager.this.filterDevice(device.getName())) != null) {
                Log.i(NewOMRONBLEDeviceManager.TAG, "get target device:" + device.getName());
                Log.i(NewOMRONBLEDeviceManager.TAG, "bone state:" + device.getBondState());
                switch(NewOMRONBLEDeviceManager.this.targetDeviceType.getType()) {
                    case 1:
                        OMRONBLEBGMDevice bgDevice = new OMRONBLEBGMDevice(device, NewOMRONBLEDeviceManager.mContext);
                        if(NewOMRONBLEDeviceManager.this.scanCallback != null) {
                            NewOMRONBLEDeviceManager.this.scanCallback.onScanComplete(bgDevice);
                        }
                    case 2:
                    default:
                        if(NewOMRONBLEDeviceManager.this.mScanSingle) {
                            NewOMRONBLEDeviceManager.this.scanLeDevice(false);
                        }
                }
            } else {
                Log.i(NewOMRONBLEDeviceManager.TAG, "TextUtils.isEmpty(device.getName()");
            }

        }
    };
    private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            Log.d(NewOMRONBLEDeviceManager.TAG, "Our gatt server connection state changed, new state ");
            Log.d(NewOMRONBLEDeviceManager.TAG, Integer.toString(newState));
            super.onConnectionStateChange(device, status, newState);
        }

        public void onServiceAdded(int status, BluetoothGattService service) {
            Log.d(NewOMRONBLEDeviceManager.TAG, "Our gatt server service was added.");
            super.onServiceAdded(status, service);
        }

        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            Log.d(NewOMRONBLEDeviceManager.TAG, "Our gatt characteristic was read.");
            if(characteristic.getUuid().equals(NewOMRONBLEDeviceManager.CHAR_CURRENT_TIME)) {
                Log.d(NewOMRONBLEDeviceManager.TAG, "Our gatt characteristic was read.  begin");
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(1);
                int month = calendar.get(2) + 1;
                int day = calendar.get(5);
                int hour = calendar.get(11);
                int min = calendar.get(12);
                int sec = calendar.get(13);
                int week = calendar.get(7) - 1;
                if(week == 0) {
                    week = 7;
                }

                characteristic.setValue(new byte[10]);
                byte var13 = 0;
                characteristic.setValue(year, 18, var13);
                offset = var13 + 2;
                characteristic.setValue(month, 17, offset);
                ++offset;
                characteristic.setValue(day, 17, offset);
                ++offset;
                characteristic.setValue(hour, 17, offset);
                ++offset;
                characteristic.setValue(min, 17, offset);
                ++offset;
                characteristic.setValue(sec, 17, offset);
                ++offset;
                characteristic.setValue(week, 17, offset);
                ++offset;
                characteristic.setValue(0, 17, offset);
                ++offset;
                characteristic.setValue(0, 17, offset);
                Log.d(NewOMRONBLEDeviceManager.TAG, "Our gatt characteristic was read.  end");
            }

            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            NewOMRONBLEDeviceManager.this.mGattServer.sendResponse(device, requestId, 0, offset, characteristic.getValue());
        }

        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            Log.d(NewOMRONBLEDeviceManager.TAG, "We have received a write request for one of our hosted characteristics");
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
        }

        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            Log.d(NewOMRONBLEDeviceManager.TAG, "Our gatt server descriptor was read.");
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        }

        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            Log.d(NewOMRONBLEDeviceManager.TAG, "Our gatt server descriptor was written.");
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        }

        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            Log.d(NewOMRONBLEDeviceManager.TAG, "Our gatt server on execute write.");
            super.onExecuteWrite(device, requestId, execute);
        }
    };

    private NewOMRONBLEDeviceManager() {
    }

    public static NewOMRONBLEDeviceManager getBLEDevManager(Context context) {
        if(bleDeviceManager == null) {
            Class var1 = NewOMRONBLEDeviceManager.class;
            synchronized(NewOMRONBLEDeviceManager.class) {
                if(bleDeviceManager == null) {
                    bleDeviceManager = new NewOMRONBLEDeviceManager();
                    mContext = context.getApplicationContext();
                }
            }
        }

        return bleDeviceManager;
    }

    public void scan(NewOMRONBLEDeviceManager.OMRONBLEDeviceScanCB cb) {
        this.scanCallback = cb;
        this.deviceTypes = DeviceType.values();
        this.mScanSingle = true;
        this.scan();
    }

    public void removeOMRONBLEDeviceScanCB() {
        this.scanCallback = null;
    }

    public void scan(NewOMRONBLEDeviceManager.OMRONBLEDeviceScanCB cb, DeviceType... deviceTypes) {
        this.scanCallback = cb;
        this.deviceTypes = deviceTypes;
        this.mScanSingle = true;
        this.scan();
    }

    private void scan() {
        if(!mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            if(this.scanCallback != null) {
                this.scanCallback.onFailure(OMRONBLEErrMsg.OMRON_BLE_ERROR_NOT_SUPPORT_BLE);
            }

            this.printError(OMRONBLEErrMsg.OMRON_BLE_ERROR_NOT_SUPPORT_BLE);
        } else {
            if(this.mHandler == null) {
                this.mHandler = new Handler(Looper.getMainLooper());
            }

            if(this.mBluetoothManager == null) {
                this.mBluetoothManager = (BluetoothManager)mContext.getSystemService("bluetooth");
            }

            if(this.mBluetoothAdapter == null) {
                this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
            }

            if(this.mBluetoothAdapter == null) {
                Log.i(TAG, "mBluetoothAdapter == null");
                if(this.scanCallback != null) {
                    this.scanCallback.onFailure(OMRONBLEErrMsg.OMRON_BLE_ERROR_NOT_SUPPORT_BLE);
                }

                this.printError(OMRONBLEErrMsg.OMRON_BLE_ERROR_NOT_SUPPORT_BLE);
            } else if(!this.mBluetoothAdapter.isEnabled()) {
                Log.i(TAG, "mBluetoothAdapter disable ");
                if(this.scanCallback != null) {
                    this.scanCallback.onFailure(OMRONBLEErrMsg.OMRON_BLE_ERROR_BT_IS_CLOSED);
                }

                this.printError(OMRONBLEErrMsg.OMRON_BLE_ERROR_BT_IS_CLOSED);
            } else {
                if(this.syncTime) {
                    Log.i(TAG, "need to sync time");
                    if(this.mGattServer == null) {
                        this.mGattServer = this.mBluetoothManager.openGattServer(mContext, this.mGattServerCallback);
                    }

                    this.setService();
                } else {
                    Log.i(TAG, "do not need to sync time");
                    if(null != this.mGattServer) {
                        this.mGattServer.removeService(this.ctsService);
                        Log.i(TAG, "do not need to sync time  and null != mGattServer");
                    } else {
                        Log.i(TAG, "do not need to sync time  and null == mGattServer");
                    }
                }

                if(!this.mScanning) {
                    this.scanLeDevice(true);
                }

            }
        }
    }

    private void printError(OMRONBLEErrMsg mOMRONBLEErrMsg) {
        Log.i(TAG, "error code:" + mOMRONBLEErrMsg.getErrCode() + ",error msg:" + mOMRONBLEErrMsg.getErrMsg());
    }

    private void scanLeDevice(boolean enable) {
        if(enable) {
            this.mHandler.postDelayed(this.autoStopScanByPeriod, (long)this.scanTimeout);
            if(!this.mScanning) {
                this.mBluetoothAdapter.startLeScan(this.mLeScanCallback);
            }

            this.mScanning = true;
            Log.i(TAG, "start scan le device");
        } else if(this.mScanning) {
            this.mScanning = false;
            this.currentTimes = 0;
            this.mBluetoothAdapter.stopLeScan(this.mLeScanCallback);
            this.mHandler.removeCallbacks(this.autoStopScanByPeriod);
            Log.i(TAG, "stop  scan le device and remove autoStopScanByPeriod callback");
        }

    }

    private DeviceType filterDevice(String deviceName) {
        for(int i = 0; i < this.deviceTypes.length; ++i) {
            if(deviceName.startsWith(this.deviceTypes[i].getPrefix())) {
                return this.deviceTypes[i];
            }
        }

        return null;
    }

    public void setScanTimeOut(int timeout) {
        this.scanTimeout = timeout * 1000;
    }

    public void setScanRetryCnt(int cnt) {
        if(cnt > 0) {
            this.scanRetryTimes = cnt;
        }

    }

    public void stopScan() {
        this.scanLeDevice(false);
    }

    public void setSyncTime(boolean syncTime) {
        this.syncTime = syncTime;
    }

    private void setService() {
        if(this.mGattServer == null) {
            Log.i(TAG, "setService mGattServer == null ");
        } else {
            Log.i(TAG, "mGattServer != null");
            BluetoothGattService previousService = this.mGattServer.getService(CTS_SERVICE);
            if(null != previousService) {
                this.mGattServer.removeService(previousService);
            }

            BluetoothGattCharacteristic currentTimeCharacteristic = new BluetoothGattCharacteristic(CHAR_CURRENT_TIME, 2, 1);
            this.ctsService = new BluetoothGattService(CTS_SERVICE, 0);
            this.ctsService.addCharacteristic(currentTimeCharacteristic);
            this.mGattServer.addService(this.ctsService);
        }
    }

    public interface OMRONBLEDeviceScanCB extends OMRONBLECallbackBase {
        void onScanProgress(int var1);

        void onScanComplete(OMRONBLEDevice var1);
    }

}
