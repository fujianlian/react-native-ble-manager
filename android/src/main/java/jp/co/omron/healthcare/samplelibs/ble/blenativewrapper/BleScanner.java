//
//  BleScanner.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.samplelibs.ble.blenativewrapper;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class BleScanner {

    @NonNull
    private final ScanHandler mHandler;

    public BleScanner(@NonNull Context context) {
        this(context, null);
    }

    public BleScanner(@NonNull Context context, @Nullable Looper looper) {
        if (null == looper) {
            HandlerThread thread = new HandlerThread("BleScannerThread");
            thread.start();
            looper = thread.getLooper();
        }
        mHandler = new ScanHandler(looper, context);
    }

    public void destroy() {
        mHandler.destroy();
    }

    public void startScan(@Nullable final UUID[] serviceUuidFilteringList, final int timeout, @NonNull final ScanListener scanListener) {
        final Object[] objects = {serviceUuidFilteringList, timeout, scanListener};
        mHandler.sendMessage(Message.obtain(mHandler, ScanHandler.Event.StartScan.ordinal(), objects));
    }

    public void stopScan() {
        mHandler.sendMessage(Message.obtain(mHandler, ScanHandler.Event.StopScan.ordinal(), Reason.StopRequest));
    }

    public boolean isScanning() {
        return mHandler.isScanning();
    }

    @NonNull
    public List<DiscoverPeripheral> getScanResults() {
        return mHandler.getScanResults();
    }

    public enum Reason {
        BluetoothOff,
        AlreadyScanning,
        OSNativeError,
        StopRequest,
        Timeout,
        Destroy,
        Unknown
    }

    public interface ScanListener {
        void onScanStarted();

        /**
         * @see Reason#BluetoothOff
         * @see Reason#AlreadyScanning
         * @see Reason#OSNativeError
         */
        void onScanStartFailure(Reason reason);

        /**
         * @see Reason#BluetoothOff
         * @see Reason#StopRequest
         * @see Reason#Timeout
         */
        void onScanStopped(@NonNull Reason reason);

        void onScan(@NonNull DiscoverPeripheral discoverPeripheral);
    }

    private static class ScanHandler extends Handler {

        private final Context mContext;
        private final BluetoothAdapter mBluetoothAdapter;
        private final AtomicBoolean mIsScanning = new AtomicBoolean(false);
        private final ConcurrentHashMap<String, DiscoverPeripheral> mLastDiscoverPeripherals = new ConcurrentHashMap<>();
        private final BroadcastReceiver mBluetoothStateChangedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(@NonNull Context context, @NonNull Intent intent) {
                int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                if (BluetoothAdapter.STATE_TURNING_OFF == bluetoothState || BluetoothAdapter.STATE_OFF == bluetoothState) {
                    sendMessage(Message.obtain(ScanHandler.this, Event.StopScan.ordinal(), Reason.BluetoothOff));
                }
            }
        };
        private BluetoothAdapter.LeScanCallback mLeScanCallback;
        private ScanCallback mScanCallback;
        private ScanListener mScanListener;

        public ScanHandler(@NonNull Looper looper, @NonNull Context context) {
            super(looper);
            mContext = context;
            BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            mContext.registerReceiver(mBluetoothStateChangedBroadcastReceiver,
                    new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        }

        public void destroy() {
            mContext.unregisterReceiver(mBluetoothStateChangedBroadcastReceiver);
            sendMessage(Message.obtain(this, ScanHandler.Event.StopScan.ordinal(), Reason.Destroy));
        }

        public boolean isScanning() {
            return mIsScanning.get();
        }

        @NonNull
        public List<DiscoverPeripheral> getScanResults() {
            final List<DiscoverPeripheral> list;
            synchronized (this) {
                list = new ArrayList<>(mLastDiscoverPeripherals.values());
            }
            return list;
        }

        @Override
        public void handleMessage(Message msg) {
            Event what = Event.valueOf(msg.what);
            switch (what) {
                case StartScan: {
                    final Object[] objects = (Object[]) msg.obj;
                    final UUID[] serviceUuidFilteringList = (UUID[]) objects[0];
                    final int timeout = (int) objects[1];
                    final ScanListener scanListener = (ScanListener) objects[2];
                    if (BluetoothAdapter.STATE_ON != mBluetoothAdapter.getState()) {
                        scanListener.onScanStartFailure(Reason.BluetoothOff);
                        break;
                    }
                    if (mIsScanning.get()) {
                        scanListener.onScanStartFailure(Reason.AlreadyScanning);
                        break;
                    }
                    boolean scanStarted = startScan(serviceUuidFilteringList, new BluetoothAdapter.LeScanCallback() {
                        @Override
                        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
                            Object[] objects = {bluetoothDevice, rssi, scanRecord};
                            sendMessage(Message.obtain(ScanHandler.this, Event.OnScan.ordinal(), objects));
                        }
                    });
                    if (!scanStarted) {
                        scanListener.onScanStartFailure(Reason.OSNativeError);
                        break;
                    }
                    mLastDiscoverPeripherals.clear();
                    mIsScanning.set(true);
                    mScanListener = scanListener;
                    mScanListener.onScanStarted();
                    if (0 < timeout) {
                        sendMessageDelayed(Message.obtain(this, ScanHandler.Event.ScanTimeout.ordinal()), timeout);
                    }
                    break;
                }
                case StopScan: {
                    final Reason reason = (Reason) msg.obj;
                    if (!mIsScanning.get()) {
                        return;
                    }
                    mIsScanning.set(false);
                    stopScan();
                    mScanListener.onScanStopped(reason);
                    mScanListener = null;
                    removeMessages(ScanHandler.Event.ScanTimeout.ordinal());
                    break;
                }
                case OnScan: {
                    final Object[] objects = (Object[]) msg.obj;
                    final BluetoothDevice bluetoothDevice = (BluetoothDevice) objects[0];
                    final int rssi = (int) objects[1];
                    final byte[] scanRecord = (byte[]) objects[2];
                    final DiscoverPeripheral discoverPeripheral;
                    synchronized (this) {
                        if (mLastDiscoverPeripherals.containsKey(bluetoothDevice.getAddress())) {
                            discoverPeripheral = mLastDiscoverPeripherals.get(bluetoothDevice.getAddress());
                            discoverPeripheral.update(rssi, scanRecord);
                        } else {
                            discoverPeripheral = new DiscoverPeripheral(bluetoothDevice, rssi, scanRecord);
                            mLastDiscoverPeripherals.put(discoverPeripheral.getAddress(), discoverPeripheral);
                        }
                    }
                    if (null != mScanListener) {
                        mScanListener.onScan(discoverPeripheral);
                    }
                    break;
                }
                case ScanTimeout:
                    sendMessage(Message.obtain(this, ScanHandler.Event.StopScan.ordinal(), Reason.Timeout));
                    break;
                default:
                    break;
            }
        }

        public boolean startScan(@Nullable UUID[] serviceUuidFilteringList, @NonNull BluetoothAdapter.LeScanCallback leScanCallback) {
            if (null != mLeScanCallback) {
                BleLog.e("null != mLeScanCallback");
                return false;
            }
            boolean ret;
            if (Build.VERSION_CODES.LOLLIPOP > Build.VERSION.SDK_INT) {
                ret = startOldScan(serviceUuidFilteringList, leScanCallback);
            } else {
                ret = startNewScan(serviceUuidFilteringList);
            }
            if (ret) {
                mLeScanCallback = leScanCallback;
            }
            return ret;
        }

        public void stopScan() {
            if (null == mLeScanCallback) {
                BleLog.e("null == mLeScanCallback");
                return;
            }
            if (Build.VERSION_CODES.LOLLIPOP > Build.VERSION.SDK_INT) {
                stopOldScan(mLeScanCallback);
            } else {
                stopNewScan();
            }
            mLeScanCallback = null;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @SuppressWarnings("deprecation")
        private boolean startOldScan(@Nullable UUID[] serviceUuidFilteringList, @NonNull BluetoothAdapter.LeScanCallback leScanCallback) {
            boolean ret;
            BleLog.i("startLeScan() call.");
            if (serviceUuidFilteringList == null) {
                ret = mBluetoothAdapter.startLeScan(leScanCallback);
            } else {
                ret = mBluetoothAdapter.startLeScan(serviceUuidFilteringList, leScanCallback);
            }
            if (ret) {
                BleLog.d("startLeScan() called. ret=true");
            } else {
                BleLog.e("startLeScan() called. ret=false");
            }
            return ret;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @SuppressWarnings("deprecation")
        private void stopOldScan(@NonNull BluetoothAdapter.LeScanCallback leScanCallback) {
            BleLog.i("stopLeScan() call.");
            mBluetoothAdapter.stopLeScan(leScanCallback);
            BleLog.d("stopLeScan() called.");
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private boolean startNewScan(@Nullable UUID[] serviceUuidFilteringList) {
            if (null != mScanCallback) {
                BleLog.e("null != mScanCallback");
                return false;
            }
            if (null == mBluetoothAdapter.getBluetoothLeScanner()) {
                BleLog.e("null == mBluetoothAdapter.getBluetoothLeScanner()");
                return false;
            }
            boolean ret;
            List<ScanFilter> filters = null;
            if (null != serviceUuidFilteringList) {
                filters = new ArrayList<>();
                for (UUID serviceUuid : serviceUuidFilteringList) {
                    ParcelUuid parcelUuid = new ParcelUuid(serviceUuid);
                    ScanFilter filter = new ScanFilter.Builder()
                            .setServiceUuid(parcelUuid)
                            .build();
                    filters.add(filter);
                }
            }
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            ScanCallback scanCallback = new ScanCallback() {
                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (null != mLeScanCallback) {
                        if (null == result.getScanRecord()) {
                            mLeScanCallback.onLeScan(result.getDevice(), result.getRssi(), null);
                        } else {
                            mLeScanCallback.onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                        }
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                }
            };
            BleLog.i("startScan() call.");
            try {
                mBluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
                BleLog.d("startScan() called. ret=true");
                mScanCallback = scanCallback;
                ret = true;
            } catch (Exception e) {
                BleLog.e(e.getMessage());
                BleLog.e("startScan() called. ret=false");
                ret = false;
            }
            return ret;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private void stopNewScan() {
            if (null == mScanCallback) {
                BleLog.e("null == mScanCallback");
                return;
            }
            if (null != mBluetoothAdapter.getBluetoothLeScanner()) {
                BleLog.i("stopScan() call.");
                mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                BleLog.d("stopScan() called.");
            } else {
                BleLog.e("null == mBluetoothAdapter.getBluetoothLeScanner()");
            }
            mScanCallback = null;
        }

        public enum Event {
            StartScan,
            StopScan,
            OnScan,
            ScanTimeout,
            Unknown;

            static Event valueOf(int ordinal) {
                return Event.values()[ordinal];
            }
        }
    }
}
