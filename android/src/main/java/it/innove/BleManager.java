package it.innove;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.facebook.react.bridge.*;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePalApplication;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.*;

import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.BleLog;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.BlePeripheral;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.BlePeripheralSettings;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.BleScanner;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.DiscoverPeripheral;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.ErrorCode;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.GattStatusCode;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.GattUUID;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.StateInfo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.omron.ble.DeviceInfo;
import com.omron.ble.common.OMRONBLEErrMsg;
import com.omron.ble.device.OMRONBLEBGMDevice;
import com.omron.ble.device.OMRONBLEDevice;
import com.omron.ble.device.OMRONBLEDeviceState;
import com.omron.ble.manager.OMRONBLEDeviceManager;

import com.omron.ble.model.BGData;

import static android.app.Activity.RESULT_OK;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;


public class BleManager extends ReactContextBaseJavaModule implements ActivityEventListener {

	public static final String LOG_TAG = "logs";
	private static final int ENABLE_REQUEST = 539;

	private class BondRequest {
		private String uuid;
		private Callback callback;

		BondRequest(String _uuid, Callback _callback) {
			uuid = _uuid;
			callback = _callback;
		}
	}

	private BluetoothAdapter bluetoothAdapter;
	private Context context;
	private ReactApplicationContext reactContext;
	private Callback enableBluetoothCallback;
	private ScanManager scanManager;
	private BondRequest bondRequest;

	// key is the MAC Address
	public Map<String, Peripheral> peripherals = new LinkedHashMap<>();
	// scan session id


	public BleManager(ReactApplicationContext reactContext) {
		super(reactContext);
		context = reactContext;
		this.reactContext = reactContext;
		reactContext.addActivityEventListener(this);
		Log.d(LOG_TAG, "BleManager created");

	}

	@Override
	public String getName() {
		return "BleManager";
	}

	private BluetoothAdapter getBluetoothAdapter() {
		if (bluetoothAdapter == null) {
			BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
			bluetoothAdapter = manager.getAdapter();
		}
		return bluetoothAdapter;
	}

	public void sendEvent(String eventName,
						  @Nullable WritableMap params) {
		getReactApplicationContext()
				.getJSModule(RCTNativeAppEventEmitter.class)
				.emit(eventName, params);
	}

	@ReactMethod
	public void start(ReadableMap options, Callback callback) {
		Log.d(LOG_TAG, "start");
		if (getBluetoothAdapter() == null) {
			Log.d(LOG_TAG, "No bluetooth support");
			callback.invoke("No bluetooth support");
			return;
		}
		boolean forceLegacy = false;
		if (options.hasKey("forceLegacy")) {
			forceLegacy = options.getBoolean("forceLegacy");
		}

	/*mxm20171229
		if (Build.VERSION.SDK_INT >= LOLLIPOP && !forceLegacy) {
			scanManager = new LollipopScanManager(reactContext, this);
		} else {
			scanManager = new LegacyScanManager(reactContext, this);
		}
*/
        //add=======================
        mBleScanner = new BleScanner(reactContext, this);
		LitePalApplication.initialize(reactContext);
        //add=======================end
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		context.registerReceiver(mReceiver, filter);
		callback.invoke();
		Log.d(LOG_TAG, "BleManager initialized");
	}



	@ReactMethod
	public void enableBluetooth(Callback callback) {
		if (getBluetoothAdapter() == null) {
			Log.d(LOG_TAG, "No bluetooth support");
			callback.invoke("No bluetooth support");
			return;
		}
		if (!getBluetoothAdapter().isEnabled()) {
			enableBluetoothCallback = callback;
			Intent intentEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			if (getCurrentActivity() == null)
				callback.invoke("Current activity not available");
			else
				getCurrentActivity().startActivityForResult(intentEnable, ENABLE_REQUEST);
		} else
			callback.invoke();
	}

	@ReactMethod
	public void scan(ReadableArray serviceUUIDs, final int scanSeconds, boolean allowDuplicates, ReadableMap options, Callback callback) {
		Log.d(LOG_TAG, "scan");
		if (getBluetoothAdapter() == null) {
			Log.d(LOG_TAG, "No bluetooth support");
			callback.invoke("No bluetooth support");
			return;
		}
		if (!getBluetoothAdapter().isEnabled())
			return;

		for (Iterator<Map.Entry<String, Peripheral>> iterator = peripherals.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry<String, Peripheral> entry = iterator.next();
			if (!entry.getValue().isConnected()) {
				iterator.remove();
			}
		}

		/**/WritableArray myServiceUUIDs = new  WritableNativeArray();
		myServiceUUIDs.pushString("1810");//1810  180F
		myServiceUUIDs.size();
		serviceUUIDs = myServiceUUIDs;
		//mxm20171229 scanManager.scan(myServiceUUIDs, scanSeconds, options, callback);
//		scanManager.scan(serviceUUIDs, scanSeconds, options, callback);
		//============================start=======================================

//		this.scanOmron(serviceUUIDs, callback);
		this.scanOmronBloodPress(serviceUUIDs, callback);

		/*





        mIsBluetoothOn = isBluetoothEnabled();
        mHandler = new Handler();
        //  mListenerRef = new WeakReference<>((OnEventListener) activity);

        mRefreshInterval = 500;

        mUUIDs = serviceUUIDs.toArrayList().toArray(new UUID[serviceUUIDs.size()]);
        mBleScanner.startScan(mUUIDs, 0 , mScanListener); //  0  no timeout

        mHandler.postDelayed(mScanResultRefreshRunnable, mRefreshInterval);
		*/
		//============================end=======================================
 
	}

	@ReactMethod
	public void scanBloodSugar(ReadableArray serviceUUIDs, final int scanSeconds, boolean allowDuplicates, ReadableMap options, Callback callback) {
		Log.d(LOG_TAG, "scan");
		if (getBluetoothAdapter() == null) {
			Log.d(LOG_TAG, "No bluetooth support");
			callback.invoke("No bluetooth support");
			return;
		}
		if (!getBluetoothAdapter().isEnabled())
			return;

		for (Iterator<Map.Entry<String, Peripheral>> iterator = peripherals.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry<String, Peripheral> entry = iterator.next();
			if (!entry.getValue().isConnected()) {
				iterator.remove();
			}
		}

		/**/WritableArray myServiceUUIDs = new  WritableNativeArray();
		myServiceUUIDs.pushString("180F");//1810  180F
		myServiceUUIDs.size();
		serviceUUIDs = myServiceUUIDs;
//		scanManager.scan(serviceUUIDs, scanSeconds, options, callback);
		//============================start=======================================

//		this.scanOmron(serviceUUIDs, callback);
		this.scanOmronBloodSugar(serviceUUIDs, callback);


		//============================end=======================================

	}


	@ReactMethod
	public void stopScan(Callback callback) {
		Log.d(LOG_TAG, "Stop scan");
		if (getBluetoothAdapter() == null) {
			Log.d(LOG_TAG, "No bluetooth support");
			callback.invoke("No bluetooth support");
			return;
		}
		if (!getBluetoothAdapter().isEnabled()) {
			callback.invoke("Bluetooth not enabled");
			return;
		}
//mxm20171229 scanManager.stopScan(callback);
		mBleScanner.stopScan();
		mRunnableFlag = true;
	}

	@ReactMethod
	public void createBond(String peripheralUUID, Callback callback) {
		Log.d(LOG_TAG, "Request bond to: " + peripheralUUID);

		Set<BluetoothDevice> deviceSet = getBluetoothAdapter().getBondedDevices();
		for (BluetoothDevice device : deviceSet) {
			if (peripheralUUID.equalsIgnoreCase(device.getAddress())) {
				callback.invoke();
				return;
			}
		}

		Peripheral peripheral = retrieveOrCreatePeripheral(peripheralUUID);
		if (peripheral == null) {
			callback.invoke("Invalid peripheral uuid");
		} else if (bondRequest != null) {
			callback.invoke("Only allow one bond request at a time");
		} else if (peripheral.getDevice().createBond()) {
			bondRequest = new BondRequest(peripheralUUID, callback); // request bond success, waiting for boradcast
			return;
		}

		callback.invoke("Create bond request fail");
	}


	@ReactMethod
	public void connectBloodPress(String peripheralUUID, Callback callback) {
		Peripheral peripheral = retrieveOrCreatePeripheral(peripheralUUID);
		if (peripheral == null) {
			callback.invoke("Invalid peripheral uuid");
			return;
		}
		//old link
//    	peripheral.connect(callback, getCurrentActivity());
//        scanList =  retrieveOrCreateDiscoverPeripheral( peripheralUUID);
		/* hm 9200*/
        /**/
		int tempInt = scanList.size();
		for(int i=0;i<tempInt;i++){
			DiscoverPeripheral dp = scanList.get(i);

			if(dp.getAddress().equals(peripheralUUID)){
				Log.d(LOG_TAG, "peripheralUUID miao :"+peripheralUUID);
				onConnect( callback, dp);
			}
		}
	}
	@ReactMethod
	public void connectBloodSugar(String peripheralUUID, Callback callback) {
		Log.d(LOG_TAG, "Connect to: " + peripheralUUID);

		Peripheral peripheral = retrieveOrCreatePeripheral(peripheralUUID);
		if (peripheral == null) {
			callback.invoke("Invalid peripheral uuid");
			return;
		}
		// 126T connect
 		this.startBondActivity();

	}

	@ReactMethod
	public void connect(String peripheralUUID, Callback callback) {
		Log.d(LOG_TAG, "Connect to: " + peripheralUUID);

		Peripheral peripheral = retrieveOrCreatePeripheral(peripheralUUID);
		if (peripheral == null) {
			callback.invoke("Invalid peripheral uuid");
			return;
		}
        //old link
//    	peripheral.connect(callback, getCurrentActivity());
//        scanList =  retrieveOrCreateDiscoverPeripheral( peripheralUUID);
		/* hm 9200*/
       /**/
        int tempInt = scanList.size();
        for(int i=0;i<tempInt;i++){
            DiscoverPeripheral dp = scanList.get(i);

            if(dp.getAddress().equals(peripheralUUID)){
                Log.d(LOG_TAG, "peripheralUUID miao :"+peripheralUUID);
                onConnect( callback, dp);
            }
        }

		// 126T connect
//		this.startBondActivity();
		//=====================add start=========================
//		BleLog.e(" onConnect =============start=========miao");
//		DiscoverPeripheral discoverPeripheral = data.getParcelableExtra(EXTRA_CONNECT_REQUEST_PERIPHERAL);
//		if (null == discoverPeripheral) {
//			enableBluetoothCallback.invoke("discoverPeripheral is null");
//			return;
//		}
//		BleLog.e(" onConnect =============start=========miao");
// 		onConnect(discoverPeripheral);
//=====================add end=========================
	}

	@ReactMethod
	public void disconnect(String peripheralUUID, Callback callback) {
		Log.d(LOG_TAG, "Disconnect from: " + peripheralUUID);

		Peripheral peripheral = peripherals.get(peripheralUUID);
		if (peripheral != null) {
			peripheral.disconnect();
			callback.invoke();
		} else
			callback.invoke("Peripheral not found");
	}

	@ReactMethod
	public void startNotification(String deviceUUID, String serviceUUID, String characteristicUUID, Callback callback) {
		Log.d(LOG_TAG, "startNotification");

		Peripheral peripheral = peripherals.get(deviceUUID);
		if (peripheral != null) {
			peripheral.registerNotify(UUIDHelper.uuidFromString(serviceUUID), UUIDHelper.uuidFromString(characteristicUUID), callback);
		} else
			callback.invoke("Peripheral not found");
	}

	@ReactMethod
	public void stopNotification(String deviceUUID, String serviceUUID, String characteristicUUID, Callback callback) {
		Log.d(LOG_TAG, "stopNotification");

		Peripheral peripheral = peripherals.get(deviceUUID);
		if (peripheral != null) {
			peripheral.removeNotify(UUIDHelper.uuidFromString(serviceUUID), UUIDHelper.uuidFromString(characteristicUUID), callback);
		} else
			callback.invoke("Peripheral not found");
	}


	@ReactMethod
	public void write(String deviceUUID, String serviceUUID, String characteristicUUID, ReadableArray message, Integer maxByteSize, Callback callback) {
		Log.d(LOG_TAG, "Write to: " + deviceUUID);

		Peripheral peripheral = peripherals.get(deviceUUID);
		if (peripheral != null) {
			byte[] decoded = new byte[message.size()];
			for (int i = 0; i < message.size(); i++) {
				decoded[i] = new Integer(message.getInt(i)).byteValue();
			}
			Log.d(LOG_TAG, "Message(" + decoded.length + "): " + bytesToHex(decoded));
			peripheral.write(UUIDHelper.uuidFromString(serviceUUID), UUIDHelper.uuidFromString(characteristicUUID), decoded, maxByteSize, null, callback, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
		} else
			callback.invoke("Peripheral not found");
	}

	@ReactMethod
	public void writeWithoutResponse(String deviceUUID, String serviceUUID, String characteristicUUID, ReadableArray message, Integer maxByteSize, Integer queueSleepTime, Callback callback) {
		Log.d(LOG_TAG, "Write without response to: " + deviceUUID);

		Peripheral peripheral = peripherals.get(deviceUUID);
		if (peripheral != null) {
			byte[] decoded = new byte[message.size()];
			for (int i = 0; i < message.size(); i++) {
				decoded[i] = new Integer(message.getInt(i)).byteValue();
			}
			Log.d(LOG_TAG, "Message(" + decoded.length + "): " + bytesToHex(decoded));
			peripheral.write(UUIDHelper.uuidFromString(serviceUUID), UUIDHelper.uuidFromString(characteristicUUID), decoded, maxByteSize, queueSleepTime, callback, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		} else
			callback.invoke("Peripheral not found");
	}

	@ReactMethod
	public void read(String deviceUUID, String serviceUUID, String characteristicUUID, Callback callback) {
		Log.d(LOG_TAG, "Read from: " + deviceUUID);
		Peripheral peripheral = peripherals.get(deviceUUID);
		if (peripheral != null) {
			peripheral.read(UUIDHelper.uuidFromString(serviceUUID), UUIDHelper.uuidFromString(characteristicUUID), callback);
		} else
			callback.invoke("Peripheral not found", null);
	}

	@ReactMethod
	public void retrieveServices(String deviceUUID, Callback callback) {
		Log.d(LOG_TAG, "Retrieve services from: " + deviceUUID);
		Peripheral peripheral = peripherals.get(deviceUUID);
		if (peripheral != null) {
			peripheral.retrieveServices(callback);
		} else
			callback.invoke("Peripheral not found", null);
	}


	@ReactMethod
	public void readRSSI(String deviceUUID, Callback callback) {
		Log.d(LOG_TAG, "Read RSSI from: " + deviceUUID);
		Peripheral peripheral = peripherals.get(deviceUUID);
		if (peripheral != null) {
			peripheral.readRSSI(callback);
		} else
			callback.invoke("Peripheral not found", null);
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback =
			new BluetoothAdapter.LeScanCallback() {


				@Override
				public void onLeScan(final BluetoothDevice device, final int rssi,
									 final byte[] scanRecord) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Log.i(LOG_TAG, "DiscoverPeripheral: " + device.getName());
							String address = device.getAddress();

							if (!peripherals.containsKey(address)) {
								Peripheral peripheral = new Peripheral(device, rssi, scanRecord, reactContext);
								peripherals.put(device.getAddress(), peripheral);
								WritableMap map = peripheral.asWritableMap();
								sendEvent("BleManagerDiscoverPeripheral", map);
							} else {
								// this isn't necessary
								Peripheral peripheral = peripherals.get(address);
								peripheral.updateRssi(rssi);
							}
						}
					});
				}


			};

	@ReactMethod
	public void checkState(ReadableMap options, Callback callback) {
		Log.d(LOG_TAG, "checkState");

		BluetoothAdapter adapter = getBluetoothAdapter();
		String state = "off";
		if (adapter != null) {
			switch (adapter.getState()) {
				case BluetoothAdapter.STATE_ON:
					state = "on";
					break;
				case BluetoothAdapter.STATE_OFF:
					state = "off";
			}
		}

		WritableMap map = Arguments.createMap();
		map.putString("state", state);
		Log.d(LOG_TAG, "state:" + state);
		sendEvent("BleManagerDidUpdateState", map);
		callback.invoke( state );

	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(LOG_TAG, "onReceive");
			final String action = intent.getAction();

			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
						BluetoothAdapter.ERROR);
				String stringState = "";

				switch (state) {
					case BluetoothAdapter.STATE_OFF:
						stringState = "off";
						break;
					case BluetoothAdapter.STATE_TURNING_OFF:
						stringState = "turning_off";
						break;
					case BluetoothAdapter.STATE_ON:
						stringState = "on";
						break;
					case BluetoothAdapter.STATE_TURNING_ON:
						stringState = "turning_on";
						break;
				}

				WritableMap map = Arguments.createMap();
				map.putString("state", stringState);
				Log.d(LOG_TAG, "state: " + stringState);
				sendEvent("BleManagerDidUpdateState", map);

			} else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
				BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				String bondStateStr = "UNKNOWN";
				switch (bondState) {
					case BluetoothDevice.BOND_BONDED:
						bondStateStr = "BOND_BONDED";
						break;
					case BluetoothDevice.BOND_BONDING:
						bondStateStr = "BOND_BONDING";
						break;
					case BluetoothDevice.BOND_NONE:
						bondStateStr = "BOND_NONE";
						break;
				}
				Log.d(LOG_TAG, "bond state: " + bondStateStr);

				if (bondRequest != null && bondRequest.uuid.equals(device.getAddress())) {
					if (bondState == BluetoothDevice.BOND_BONDED) {
						bondRequest.callback.invoke();
						bondRequest = null;
					} else if (bondState == BluetoothDevice.BOND_NONE || bondState == BluetoothDevice.ERROR) {
						bondRequest.callback.invoke("Bond request has been denied");
						bondRequest = null;
					}
				}
			}
		}
	};

	@ReactMethod
	public void getDiscoveredPeripherals(Callback callback) {
		Log.d(LOG_TAG, "Get discovered peripherals");
		WritableArray map = Arguments.createArray();
		Map<String, Peripheral> peripheralsCopy = new LinkedHashMap<>(peripherals);
		for (Map.Entry<String, Peripheral> entry : peripheralsCopy.entrySet()) {
			Peripheral peripheral = entry.getValue();
			WritableMap jsonBundle = peripheral.asWritableMap();
			map.pushMap(jsonBundle);
		}
		callback.invoke(null, map);
	}

	@ReactMethod
	public void getConnectedPeripherals(ReadableArray serviceUUIDs, Callback callback) {
		Log.d(LOG_TAG, "Get connected peripherals");
		WritableArray map = Arguments.createArray();
		Map<String, Peripheral> peripheralsCopy = new LinkedHashMap<>(peripherals);
		for (Map.Entry<String, Peripheral> entry : peripheralsCopy.entrySet()) {
			Peripheral peripheral = entry.getValue();
			Boolean accept = false;

			if (serviceUUIDs != null && serviceUUIDs.size() > 0) {
				for (int i = 0; i < serviceUUIDs.size(); i++) {
					accept = peripheral.hasService(UUIDHelper.uuidFromString(serviceUUIDs.getString(i)));
				}
			} else {
				accept = true;
			}

			if (peripheral.isConnected() && accept) {
				WritableMap jsonBundle = peripheral.asWritableMap();
				map.pushMap(jsonBundle);
			}
		}
		callback.invoke(null, map);
	}

	@ReactMethod
	public void getBondedPeripherals(Callback callback) {
		Log.d(LOG_TAG, "Get bonded peripherals");
		WritableArray map = Arguments.createArray();
		Set<BluetoothDevice> deviceSet = getBluetoothAdapter().getBondedDevices();
		for (BluetoothDevice device : deviceSet) {
			Peripheral peripheral = new Peripheral(device, reactContext);
			WritableMap jsonBundle = peripheral.asWritableMap();
			map.pushMap(jsonBundle);
		}
		callback.invoke(null, map);
	}

	@ReactMethod
	public void removePeripheral(String deviceUUID, Callback callback) {
		Log.d(LOG_TAG, "Removing from list: " + deviceUUID);
		Peripheral peripheral = peripherals.get(deviceUUID);
		if (peripheral != null) {
			if (peripheral.isConnected()) {
				callback.invoke("Peripheral can not be removed while connected");
			} else {
				peripherals.remove(deviceUUID);
				callback.invoke();
			}
		} else
			callback.invoke("Peripheral not found");
	}

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static WritableArray bytesToWritableArray(byte[] bytes) {
		WritableArray value = Arguments.createArray();
		for (int i = 0; i < bytes.length; i++)
			value.pushInt((bytes[i] & 0xFF));
		return value;
	}

	@Override
	public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		Log.d(LOG_TAG, "onActivityResult");
		if (requestCode == ENABLE_REQUEST && enableBluetoothCallback != null) {
			if (resultCode == RESULT_OK) {
				enableBluetoothCallback.invoke();
			} else {
				enableBluetoothCallback.invoke("User refused to enable");
			}
			enableBluetoothCallback = null;
		}

	}

	@Override
	public void onNewIntent(Intent intent) {

	}

	private Peripheral retrieveOrCreatePeripheral(String peripheralUUID) {
		Peripheral peripheral = peripherals.get(peripheralUUID);
		if (peripheral == null) {
			if (peripheralUUID != null) {
				peripheralUUID = peripheralUUID.toUpperCase();
			}
			if (BluetoothAdapter.checkBluetoothAddress(peripheralUUID)) {
				BluetoothDevice device = bluetoothAdapter.getRemoteDevice(peripheralUUID);
				peripheral = new Peripheral(device, reactContext);
				peripherals.put(peripheralUUID, peripheral);
			}
		}


		return peripheral;
	}

//====================================================================


	private BleScanner mBleScanner;
	private WeakReference<OnEventListener> mListenerRef;

	private UUID[] mUUIDs = null;
	private Handler mHandler;
	private int mRefreshInterval = 500;
    private List<DiscoverPeripheral> scanList = null;
    private BlePeripheral mTargetPeripheral;
    private BleCommunicationExecutor mBleCommunicationExecutor;
    private boolean mIsCtsWritten;
	private boolean mRunnableFlag = false;

	private final Runnable mScanResultRefreshRunnable = new Runnable() {
		@Override
		public void run() {
			List<DiscoverPeripheral> scanResultList = mBleScanner.getScanResults();
            scanList = scanResultList;
            Log.d(LOG_TAG, "Request scanResultList  miao : " + scanResultList);
//			mBleScanAdapter.setList(scanResultList);



			if(!mRunnableFlag){
				mHandler.postDelayed(this, mRefreshInterval);
			}


		}
	};

	private final BleScanner.ScanListener mScanListener = new BleScanner.ScanListener() {
		@Override
		public void onScanStarted() {
			// nop
			Log.d(LOG_TAG, "onScanStarted  mScanListener: ");
		}

		@Override
		public void onScanStartFailure(BleScanner.Reason reason) {
//			Log.d("Start scan failed. reason:" + reason);
			Log.d(LOG_TAG, "OMRONBLEErrMsg  mScanListener miao== errMsg" + reason);
			WritableMap map = Arguments.createMap();

			map.putString("state", JSON.toJSONString(reason));//JSON.toJSONString(errMsg)
			Log.d(LOG_TAG, " mScanListener state: " + JSON.toJSONString(reason));
			sendEvent("BleManagerDidUpdateState", map);
//			OnEventListener eventListener = mListenerRef.get();
//			if (eventListener != null) {
//				eventListener.onScanStartFailure(reason);
//			}
		}

		@Override
		public void onScanStopped(@NonNull BleScanner.Reason reason) {
			Log.d(LOG_TAG, " mScanListener onScanStopped state: " + JSON.toJSONString(reason));
//			AppLog.i("Scan stopped. reason:" + reason);
//			OnEventListener eventListener = mListenerRef.get();
//			if (eventListener != null) {
//				eventListener.onScanStopped(reason);
//			}
		}

		@Override
		public void onScan(@NonNull DiscoverPeripheral discoverPeripheral) {
			// nop
			Log.d(LOG_TAG, " mScanListener onScan state: " + JSON.toJSONString(discoverPeripheral));
		}
	};

	@ReactMethod
	public void startBloodSugar(ReadableMap options, Callback callback) {
		Log.d(LOG_TAG, "start");
		if (getBluetoothAdapter() == null) {
			Log.d(LOG_TAG, "No bluetooth support");
			callback.invoke("No bluetooth support");
			return;
		}
		boolean forceLegacy = false;
		if (options.hasKey("forceLegacy")) {
			forceLegacy = options.getBoolean("forceLegacy");
		}

		//add=======================
		mBleScanner = new BleScanner(reactContext, this);
		LitePalApplication.initialize(reactContext);
		//add=======================end
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		context.registerReceiver(mReceiver, filter);
		callback.invoke();
		Log.d(LOG_TAG, "BleManager initialized");
	}
	/**
	 * Omron
	 * @param serviceUUIDs
	 * @param callback
     */
	private void scanOmronBloodSugar(ReadableArray serviceUUIDs, Callback callback) {

	//    startScan
    	UUID uid = UUIDHelper.uuidFromString("180F");//1810   180F
		UUID[] uids = new UUID[1];
//		UUID[] uids = new UUID[serviceUUIDs.size()];
		uids[0] = uid;
		/*uids*/
		mIsBluetoothOn = isBluetoothEnabled();
		mHandler = new Handler();
		mRefreshInterval = 500;

//		mUUIDs = serviceUUIDs.toArrayList().toArray(new UUID[serviceUUIDs.size()]);//;uids new UUID[serviceUUIDs.size()]

		mBleScanner.startScan(uids, 0 , mScanListener);//mUUIDs
		mHandler.postDelayed(mScanResultRefreshRunnable, mRefreshInterval);

	}

	private void scanOmronBloodPress(ReadableArray serviceUUIDs, Callback callback) {
		//    startScan
		UUID uid = UUIDHelper.uuidFromString("1810");//1810   180F
		UUID[] uids = new UUID[1];
		uids[0] = uid;
		/*uids*/
		mIsBluetoothOn = isBluetoothEnabled();
		mHandler = new Handler();
		mRefreshInterval = 500;
//		mUUIDs = serviceUUIDs.toArrayList().toArray(new UUID[serviceUUIDs.size()]);//;uids new UUID[serviceUUIDs.size()]
		mBleScanner.startScan(uids, 0 , mScanListener);//mUUIDs
		mHandler.postDelayed(mScanResultRefreshRunnable, mRefreshInterval);

	}

    @ReactMethod
    public void onlyScan(ReadableArray serviceUUIDs, final int scanSeconds, boolean allowDuplicates, ReadableMap options, Callback callback) {
        /*
    	public void scan(ReadableArray serviceUUIDs, final int scanSeconds, boolean allowDuplicates, ReadableMap options, Callback callback) {
    	*/
        Log.d(LOG_TAG, "scan");
        if (getBluetoothAdapter() == null) {
            Log.d(LOG_TAG, "No bluetooth support");
            callback.invoke("No bluetooth support");
            return;
        }
        if (!getBluetoothAdapter().isEnabled())
            return;

        for (Iterator<Map.Entry<String, Peripheral>> iterator = peripherals.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Peripheral> entry = iterator.next();
            if (!entry.getValue().isConnected()) {
                iterator.remove();
            }
        }
        //============================start=======================================
        mIsBluetoothOn = isBluetoothEnabled();
        mHandler = new Handler();
    //  mListenerRef = new WeakReference<>((OnEventListener) activity);

        mRefreshInterval = 500;

        mUUIDs = serviceUUIDs.toArrayList().toArray(new UUID[serviceUUIDs.size()]);
        mBleScanner.startScan(mUUIDs, 0 /* no timeout */, mScanListener);

        mHandler.postDelayed(mScanResultRefreshRunnable, mRefreshInterval);

    }

	public static final String EXTRA_CONNECT_REQUEST_PERIPHERAL = "extra_connect_request_peripheral";
	private Handler mMessageHandler;
    private static final int INDICATION_WAIT_TIME = 1000 * 10;
    private boolean mIsBluetoothOn;

	protected void onConnect(Callback callback, @NonNull DiscoverPeripheral discoverPeripheral) {
		BleLog.e(" onConnect ======================miao");
			mMessageHandler = new Handler() {
			public void handleMessage(Message msg) {
				onReceiveMessage(msg);
			}
		};

		BlePeripheral blePeripheral = new BlePeripheral(context, discoverPeripheral);

		blePeripheral.connect(new BlePeripheral.ActionReceiver() {
			@Override
			public void didDisconnection(@NonNull String address) {
				mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.DidDisconnection.ordinal()));
			}

			@Override
			public void onCharacteristicChanged(@NonNull String address, @NonNull BluetoothGattCharacteristic characteristic) {
				if (GattUUID.Characteristic.BloodPressureMeasurementCharacteristic.getUuid().equals(characteristic.getUuid())) {
					mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.BPMDataRcv.ordinal(), characteristic.getValue()));
				} else if (GattUUID.Characteristic.WeightMeasurementCharacteristic.getUuid().equals(characteristic.getUuid())) {
					mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.WMDataRcv.ordinal(), characteristic.getValue()));
				} else if (GattUUID.Characteristic.BatteryLevelCharacteristic.getUuid().equals(characteristic.getUuid())) {
					mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.BatteryDataRcv.ordinal(), characteristic.getValue()));
				} else if (GattUUID.Characteristic.CurrentTimeCharacteristic.getUuid().equals(characteristic.getUuid())) {
					mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.CTSDataRcv.ordinal(), characteristic.getValue()));
				}
			}
		}, new BlePeripheral.ConnectionListener() {
			@Override
			public void onComplete(@NonNull String address, ErrorCode errorCode) {
				if (null == errorCode) {
					mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.ConnectionCompleted.ordinal()));
				} else {
					mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.ConnectionFailed.ordinal(), errorCode));
				}
			}
		}, new StateInfo.StateMonitor() {
			@Override
			public void onBondStateChanged(@NonNull StateInfo.BondState bondState) {
				mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.BondStateChanged.ordinal(), bondState));
			}

			@Override
			public void onAclConnectionStateChanged(@NonNull StateInfo.AclConnectionState aclConnectionState) {
				mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.AclConnectionStateChanged.ordinal(), aclConnectionState));
			}

			@Override
			public void onGattConnectionStateChanged(@NonNull StateInfo.GattConnectionState gattConnectionState) {
				mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.GattConnectionStateChanged.ordinal(), gattConnectionState));
			}

			@Override
			public void onConnectionStateChanged(@NonNull StateInfo.ConnectionState connectionState) {

			}

			@Override
			public void onDetailedStateChanged(@NonNull StateInfo.DetailedState detailedState) {
				mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.DetailedStateChanged.ordinal(), detailedState));
			}
		});

        mTargetPeripheral = blePeripheral;

        mBleCommunicationExecutor = new BleCommunicationExecutor(mTargetPeripheral, new Handler() {
            public void handleMessage(Message msg) {
                onBleCommunicationComplete(msg);
            }
        });
        mIsCtsWritten = false;

	}

    private void onBleCommunicationComplete(Message msg) {
        BleEvent.Type type = BleEvent.Type.values()[msg.what];
        final Object[] objects = (Object[]) msg.obj;
        final BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) objects[0];
        final int gattStatus = (int) objects[1];
        final ErrorCode errorCode = (ErrorCode) objects[2];
        if (null != errorCode) {
            BleLog.e("ble event error. " + errorCode.name());

//            disconnect(mTargetPeripheral, DisconnectReason.CommunicationError);
            return;
        }
//        AppLog.d(type.name());
        switch (type) {
            case SetNotification: {
                if (GattStatusCode.GATT_SUCCESS != gattStatus) {
//                    AppLog.e("Invalid gatt status. status:" + gattStatus);
//                    disconnect(mTargetPeripheral, DisconnectReason.GattStatusError);
                    break;
                }
                mBleCommunicationExecutor.exec();
                break;
            }
            case SetIndication: {
                if (GattStatusCode.GATT_SUCCESS != gattStatus) {
//                    AppLog.e("Invalid gatt status. status:" + gattStatus);
//                    disconnect(mTargetPeripheral, DisconnectReason.GattStatusError);
                    break;
                }
                mBleCommunicationExecutor.exec();
                if (mTargetPeripheral.getStateInfo().isBonded()) {
                    // The IndicationWaitTimer will start when both of indication of
                    // BPM or WM is registered and in Bonded state.The timer will start when the state is Bonded
                    // because indication of BPM or WM is running in Bonded state.
                    startIndicationWaitTimer();
                }
                break;
            }
            case WriteCharacteristic: {
                if (GattUUID.Characteristic.CurrentTimeCharacteristic.getUuid().equals(characteristic.getUuid())) {
                    mIsCtsWritten = true;
                    if (GattStatusCode.GATT_SUCCESS == gattStatus) {
                        mBleCommunicationExecutor.exec();
                    } else if (GattStatusCode.GATT_NO_RESOURCES == gattStatus) {   // 0x80: Write Request Rejected
//                        AppLog.i("Write Request Rejected. (0x80)");
                        // If the slave sends error response in CTS,
                        // you don't retry and should send next request.
                        mBleCommunicationExecutor.exec();
                    } else if (GattStatusCode.GATT_ERROR == gattStatus) {   // 0x85: Write Request Rejected
//                        AppLog.w("Write Request Rejected. (0x85)");
                        // The status, 0x80 (Data filed ignored) will be notified same status to the application
                        // but there are cases when notified other status, 0x85 to the application in some smartphones.
                        // So the application need to regard as 0x80 only for Current Time Characteristic.
                        mBleCommunicationExecutor.exec();
                    } else {
//                        AppLog.e("Invalid gatt status. status:" + gattStatus);
//                        disconnect(mTargetPeripheral, DisconnectReason.GattStatusError);
                    }
                } else {
                    if (GattStatusCode.GATT_SUCCESS == gattStatus) {
                        mBleCommunicationExecutor.exec();
                    } else {
//                        AppLog.e("Invalid gatt status. status:" + gattStatus);
//                        disconnect(mTargetPeripheral, DisconnectReason.GattStatusError);
                    }
                }
                break;
            }
            case ReadCharacteristic: {
                if (GattStatusCode.GATT_SUCCESS != gattStatus) {
//                    AppLog.e("Invalid gatt status. status:" + gattStatus);
//                    disconnect(mTargetPeripheral, DisconnectReason.GattStatusError);
                    break;
                }
                if (GattUUID.Characteristic.BloodPressureFeatureCharacteristic.getUuid().equals(characteristic.getUuid())) {
                    mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.BPFDataRcv.ordinal(), characteristic.getValue()));
                }
                if (GattUUID.Characteristic.WeightScaleFeatureCharacteristic.getUuid().equals(characteristic.getUuid())) {
                    mMessageHandler.sendMessage(Message.obtain(mMessageHandler, MessageType.WSFDataRcv.ordinal(), characteristic.getValue()));
                }
                mBleCommunicationExecutor.exec();
                break;
            }
        }
    }

	protected void onReceiveMessage(Message msg) {
		MessageType messageType = MessageType.values()[msg.what];

		BleLog.e(" messageType"+messageType);
        switch (messageType) {
            case BluetoothOff:
                mIsBluetoothOn = false;

                break;
            case BluetoothOn:
                mIsBluetoothOn = true;

                break;
            case ConnectionCompleted:
                BleLog.e("Connect to " + mTargetPeripheral.getLocalName() + "(" + mTargetPeripheral.getAddress() + ")");
                startCommunication();
                break;
            case BondStateChanged:
                StateInfo.BondState bondState = (StateInfo.BondState) msg.obj;

                if (mTargetPeripheral.getStateInfo().isConnected() && StateInfo.BondState.Bonded == bondState) {
                    // The IndicationWaitTimer will start when both of indication of
                    // BPM or WM is registered and in Bonded state. The timer will startwhen the state is Bonded
                    // because indication of BPM or WM is running in no Nonded state.
                    startIndicationWaitTimer();
                }
                break;
            case AclConnectionStateChanged:
                StateInfo.AclConnectionState aclConnectionState = (StateInfo.AclConnectionState) msg.obj;
                BleLog.e("Connect to aclConnectionState" + aclConnectionState + "(" + mTargetPeripheral.getAddress() + ")");
                break;
            case GattConnectionStateChanged:
                StateInfo.GattConnectionState gattConnectionState = (StateInfo.GattConnectionState) msg.obj;
                BleLog.e("Connect to gattConnectionState" + gattConnectionState + "(" + mTargetPeripheral.getAddress() + ")");
                break;
            case DetailedStateChanged:
                BleLog.e("miao mIsBluetoothOn" + mIsBluetoothOn + "(" + mTargetPeripheral.getAddress() + ")");
                if (mIsBluetoothOn) {
                    StateInfo.DetailedState detailedState = (StateInfo.DetailedState) msg.obj;
                    BleLog.e("Connect to detailedState" + detailedState + "(" + mTargetPeripheral.getAddress() + ")");
                    WritableMap map = Arguments.createMap();
                    map.putString("state", detailedState.name());
                    Log.d(LOG_TAG, "state: " + detailedState);
                    sendEvent("BleManagerDidUpdateState", map);

                }
                break;
            case BatteryDataRcv:
                byte[] batteryData = (byte[]) msg.obj;
                int batteryLevel = batteryData[0];

                BleLog.e("Battery Level Data:" + batteryLevel);
                break;
            case CTSDataRcv:
                byte[] ctsData = (byte[]) msg.obj;
                byte[] buf = new byte[2];
                System.arraycopy(ctsData, 0, buf, 0, 2);
                ByteBuffer ctsYearByteBuffer = ByteBuffer.wrap(buf);
                ctsYearByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                int ctsYear = ctsYearByteBuffer.getShort();
                int ctsMonth = ctsData[2];
                int ctsDay = ctsData[3];
                int ctsHour = ctsData[4];
                int ctsMinute = ctsData[5];
                int ctsSecond = ctsData[6];
                byte AdjustReason = ctsData[9];
                String ctsTime = String.format(Locale.US,
                        "%1$04d-%2$02d-%3$02d %4$02d:%5$02d:%6$02d",
                        ctsYear, ctsMonth, ctsDay, ctsHour, ctsMinute, ctsSecond);

                BleLog.e("CTS Data:" + ctsTime + " (AdjustReason:" + AdjustReason + ")");

                if (!mIsCtsWritten) {
                    BleLog.d("Write CTS");
                    BluetoothGattCharacteristic characteristic = mTargetPeripheral.getCharacteristic(GattUUID.Characteristic.CurrentTimeCharacteristic.getUuid());
                    if (null == characteristic) {
                        BleLog.e("null == characteristic");
                        break;
                    }
                    byte[] currentTimeData = getCurrentTimeData();

                    mBleCommunicationExecutor.add(new BleEvent(BleEvent.Type.WriteCharacteristic, characteristic));
                    if (!mBleCommunicationExecutor.isExecuting()) {
                        mBleCommunicationExecutor.exec();
                    }
                }
                break;
            case BPMDataRcv:
                restartIndicationWaitTimer();

                getBPMDataRcv(msg);

                break;
            case WMDataRcv:
                restartIndicationWaitTimer();

                getWMDataRcv(msg);

                break;
            case IndicationWaitTimeout:
                BleLog.e("Indication wait timeout.");
//                disconnect(mTargetPeripheral, DisconnectReason.IndicationWaitTimeout);
                break;
            default:
                break;
        }
	}

    private void getWMDataRcv(Message msg){
        byte[] data;
        byte[] buf = new byte[2];

        data = (byte[]) msg.obj;
        System.arraycopy(data, 0, buf, 0, 2);
        ByteBuffer bpfByteBuffer = ByteBuffer.wrap(buf);
        bpfByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        short bpfVal = bpfByteBuffer.getShort();
        String bpfStr = String.format(Locale.US, "%1$04x", (short) bpfVal);
        BleLog.e("Blood Pressure Feature Data:" + bpfStr);
    }


    private void getBPMDataRcv(Message msg){
        byte[] data;
        byte[] buf = new byte[2];
        ByteBuffer byteBuffer;
        int idx = 0;
        data = (byte[]) msg.obj;

        byte flags = data[idx++];

        // 0: mmHg	1: kPa
        boolean kPa = (flags & 0x01) > 0;
        // 0: No Timestamp info 1: With Timestamp info
        boolean timestampFlag = (flags & 0x02) > 0;
        // 0: No PlseRate info 1: With PulseRate info
        boolean pulseRateFlag = (flags & 0x04) > 0;
        // 0: No UserID info 1: With UserID info
        boolean userIdFlag = (flags & 0x08) > 0;
        // 0: No MeasurementStatus info 1: With MeasurementStatus info
        boolean measurementStatusFlag = (flags & 0x10) > 0;

        // Set BloodPressureMeasurement unit
        String unit;
        if (kPa) {
            unit = "kPa";
        } else {
            unit = "mmHg";
        }

        // Parse Blood Pressure Measurement
        short systolicVal = 0;
        short diastolicVal = 0;
        short meanApVal = 0;

        System.arraycopy(data, idx, buf, 0, 2);
        idx += 2;
        byteBuffer = ByteBuffer.wrap(buf);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        systolicVal = byteBuffer.getShort();

        System.arraycopy(data, idx, buf, 0, 2);
        idx += 2;
        byteBuffer = ByteBuffer.wrap(buf);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        diastolicVal = byteBuffer.getShort();

        System.arraycopy(data, idx, buf, 0, 2);
        idx += 2;
        byteBuffer = ByteBuffer.wrap(buf);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        meanApVal = byteBuffer.getShort();

        BleLog.e("systolicValue:" + systolicVal + " " + unit);
        BleLog.e("diastolicValue:" + diastolicVal + " " + unit);
        BleLog.e("meanApValue:" + meanApVal + " " + unit);

//                mSystolicView.setText(Float.toString(systolicVal) + " " + unit);
//                mDiastolicView.setText(Float.toString(diastolicVal) + " " + unit);
//                mMeanApView.setText(Float.toString(meanApVal) + " " + unit);

        // Parse Timestamp
        String timestampStr = "----";
        String dateStr = "--";
        String timeStr = "--";
        if (timestampFlag) {
            System.arraycopy(data, idx, buf, 0, 2);
            idx += 2;
            byteBuffer = ByteBuffer.wrap(buf);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

            int year = byteBuffer.getShort();
            int month = data[idx++];
            int day = data[idx++];
            int hour = data[idx++];
            int min = data[idx++];
            int sec = data[idx++];

            dateStr = String.format(Locale.US, "%1$04d-%2$02d-%3$02d", year, month, day);
            timeStr = String.format(Locale.US, "%1$02d:%2$02d:%3$02d", hour, min, sec);
            timestampStr = dateStr + " " + timeStr;
            BleLog.e("Timestamp Data:" + timestampStr);
        }
//                mTimestampView.setText(timestampStr);

        // Parse PulseRate
        short pulseRateVal = 0;
        String pulseRateStr = "----";
        if (pulseRateFlag) {
            System.arraycopy(data, idx, buf, 0, 2);
            idx += 2;
            byteBuffer = ByteBuffer.wrap(buf);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            pulseRateVal = byteBuffer.getShort();
            pulseRateStr = Short.toString(pulseRateVal);
            BleLog.e("PulseRate Data:" + pulseRateStr);
        }
//                mPulseRateView.setText(pulseRateStr);

        // Parse UserID
        int userIDVal = 0;
        String userIDStr = "----";
        if (userIdFlag) {
            userIDVal = data[idx++];
            userIDStr = String.valueOf(userIDVal);
            BleLog.e("UserID Data:" + userIDStr);
        }
//                mUserIDView.setText(userIDStr);

        // Parse Measurement Status
        int measurementStatusVal = 0;
        String measurementStatusStr = "----";
        if (measurementStatusFlag) {
            System.arraycopy(data, idx, buf, 0, 2);
            idx += 2;
            byteBuffer = ByteBuffer.wrap(buf);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            measurementStatusVal = byteBuffer.getShort();
            measurementStatusStr = String.format(Locale.US, "%1$04x", (short) measurementStatusVal);
            BleLog.e("MeasurementStatus Data:" + measurementStatusStr);

//                    mBodyMovementView.setText((measurementStatusVal & 0x0001) == 0 ? "No" : "Yes");
//                    mIrregularPulseView.setText((measurementStatusVal & 0x0004) == 0 ? "No" : "Yes");
        } else {
//                    mBodyMovementView.setText("----");
//                    mIrregularPulseView.setText("----");
        }

        // Output to History
        BleLog.e("Add history");
        String entry = "{"
				+"\"timestampData\":\""+timestampStr
                + "\",\"systolic\":\"" + systolicVal
                + "\",\"diastolic\":\"" + diastolicVal
                + "\",\"meanApVal\":\"" + meanApVal
                + "\",\"pulseRateStr\":\"" + pulseRateStr
                + "\",\"flags\":\"" + String.format(Locale.US, "%1$02x", flags)
                + "\",\"measurementStatus\":\"" + measurementStatusStr
				+"\"}";
		Map entrymap = new HashMap();
		entrymap.put("timestampData",timestampStr);
		entrymap.put("diastolic",diastolicVal);
		entrymap.put("meanApVal",meanApVal);
		entrymap.put("pulseRateStr",pulseRateStr);
		entrymap.put("flags",String.format(Locale.US, "%1$02x", flags));
		entrymap.put("measurementStatus",measurementStatusStr);

//
//                HistoryData hd = (HistoryData) this.getApplication();
//                hd.add(HistoryData.SERV_BLS, entry);

        // Output log for data aggregation
        // AppLog format: ## For aggregation ## timestamp(date), timestamp(time), systolic, diastolic, meanAP, current date time
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String agg = "## For aggregation ## ";
        agg += dateStr + "," + timeStr;
        agg += "," + systolicVal + "," + diastolicVal + "," + meanApVal;
        agg += "," + sdf.format(c.getTime());
        BleLog.e(agg);

        WritableMap map = Arguments.createMap();
        map.putString("entry", entry);
        Log.d(LOG_TAG, "entry:BPMDataRcv " + entry);
        sendEvent("BleManagerBPMDataRcv", map);

    }

    private void stopIndicationWaitTimer() {
        BleLog.dMethodIn();
        mMessageHandler.removeMessages(MessageType.IndicationWaitTimeout.ordinal());
    }

    private void restartIndicationWaitTimer() {
        stopIndicationWaitTimer();
        startIndicationWaitTimer();
    }

	protected enum MessageType {
		BluetoothOff,
		BluetoothOn,
		ConnectionCompleted,
		ConnectionFailed,
		DisconnectionCompleted,
		DidDisconnection,
		Disconnected,
		BondStateChanged,
		AclConnectionStateChanged,
		GattConnectionStateChanged,
		DetailedStateChanged,
		BPFDataRcv,
		BPMDataRcv,
		WMDataRcv,
		WSFDataRcv,
		BatteryDataRcv,
		CTSDataRcv,
		// The waiting time-out message for receiving Indication.
		// After Indication Enable setting, this message will be
		// displayed when not receive the Indication in the prescribed time
		// This is a solution of the  problem in some models. (The  Indication is received in OS level
		// but the OS would return the Indication Confirmation without notification to the app).
		IndicationWaitTimeout,
		UNKNOWN
	}
    private enum DisconnectReason {
        UserRequest,
        CommunicationError,
        GattStatusError,
        IndicationWaitTimeout,
    }

	public interface OnEventListener {
		void onScanStartFailure(BleScanner.Reason reason);

		void onScanStopped(BleScanner.Reason reason);

		void onConnectRequest(DiscoverPeripheral discoverPeripheral);
	}

    private static class BleEvent {
        public final Type type;
        public final BluetoothGattCharacteristic characteristic;

        public BleEvent(Type type, BluetoothGattCharacteristic characteristic) {
            this.type = type;
            this.characteristic = characteristic;
        }

        public enum Type {
            SetNotification, SetIndication, WriteCharacteristic, ReadCharacteristic
        }
    }

    private static class BleCommunicationExecutor {
        private final LinkedList<BleEvent> mBleEventList = new LinkedList<>();
        private final BlePeripheral mTargetPeripheral;
        private final Handler mCompletionHandler;
        private boolean mExecuting;

        BleCommunicationExecutor(BlePeripheral targetPeripheral, Handler completionHandler) {
            mTargetPeripheral = targetPeripheral;
            mCompletionHandler = completionHandler;
            mExecuting = false;
        }

        public boolean isExecuting() {
            return mExecuting;
        }

        public void add(BleEvent bleEvent) {
            mBleEventList.add(bleEvent);
        }

        public void clear() {
            mBleEventList.clear();
        }

        public boolean exec() {
            if (mBleEventList.isEmpty()) {

                return false;
            }
            if (mExecuting) {

                return false;
            }
            final BleEvent bleEvent = mBleEventList.poll();
            final BluetoothGattCharacteristic characteristic = bleEvent.characteristic;

            switch (bleEvent.type) {
                case SetNotification:
                    mTargetPeripheral.setNotificationEnabled(characteristic, true, new BlePeripheral.SetNotificationResultListener() {
                        @Override
                        public void onComplete(@NonNull String address, BluetoothGattCharacteristic characteristic, int gattStatus, ErrorCode errorCode) {
                            mExecuting = false;
                            Object[] objects = {characteristic, gattStatus, errorCode};
                            mCompletionHandler.sendMessage(Message.obtain(mCompletionHandler, bleEvent.type.ordinal(), objects));
                        }
                    });
                    break;
                case SetIndication:
                    mTargetPeripheral.setNotificationEnabled(characteristic, true, new BlePeripheral.SetNotificationResultListener() {
                        @Override
                        public void onComplete(@NonNull String address, BluetoothGattCharacteristic characteristic, int gattStatus, ErrorCode errorCode) {
                            mExecuting = false;
                            Object[] objects = {characteristic, gattStatus, errorCode};
                            mCompletionHandler.sendMessage(Message.obtain(mCompletionHandler, bleEvent.type.ordinal(), objects));
                        }
                    });
                    break;
                case WriteCharacteristic:
                    mTargetPeripheral.writeCharacteristic(characteristic, new BlePeripheral.WriteCharacteristicResultListener() {
                        @Override
                        public void onComplete(@NonNull String address, BluetoothGattCharacteristic characteristic, int gattStatus, ErrorCode errorCode) {
                            mExecuting = false;
                            Object[] objects = {characteristic, gattStatus, errorCode};
                            mCompletionHandler.sendMessage(Message.obtain(mCompletionHandler, bleEvent.type.ordinal(), objects));
                        }
                    });
                    break;
                case ReadCharacteristic:
                    mTargetPeripheral.readCharacteristic(characteristic, new BlePeripheral.ReadCharacteristicResultListener() {
                        @Override
                        public void onComplete(@NonNull String address, BluetoothGattCharacteristic characteristic, int gattStatus, ErrorCode errorCode) {
                            mExecuting = false;
                            Object[] objects = {characteristic, gattStatus, errorCode};
                            mCompletionHandler.sendMessage(Message.obtain(mCompletionHandler, bleEvent.type.ordinal(), objects));
                        }
                    });
                    break;
            }
            mExecuting = true;
            return true;
        }
    }

    private void startCommunication() {

        BluetoothGattCharacteristic characteristic;
        if (null != (characteristic = mTargetPeripheral.getCharacteristic(GattUUID.Characteristic.BatteryLevelCharacteristic.getUuid()))) {

            BleLog.e(" [LOG]Battery Service is discovered");
            mBleCommunicationExecutor.add(new BleEvent(BleEvent.Type.SetNotification, characteristic));
        }
        if (null != (characteristic = mTargetPeripheral.getCharacteristic(GattUUID.Characteristic.CurrentTimeCharacteristic.getUuid()))) {

            BleLog.e("[LOG]Current Time Service is discovered");
            mBleCommunicationExecutor.add(new BleEvent(BleEvent.Type.SetNotification, characteristic));
        }
        if (null != (characteristic = mTargetPeripheral.getCharacteristic(GattUUID.Characteristic.BloodPressureMeasurementCharacteristic.getUuid()))) {

            BleLog.e("[LOG]Blood Pressure Service is discovered");
            mBleCommunicationExecutor.add(new BleEvent(BleEvent.Type.SetIndication, characteristic));
        }
        if (null != (characteristic = mTargetPeripheral.getCharacteristic(GattUUID.Characteristic.WeightMeasurementCharacteristic.getUuid()))) {

            BleLog.e("[LOG]Weight Scale Service is discovered");
            mBleCommunicationExecutor.add(new BleEvent(BleEvent.Type.SetIndication, characteristic));
        }
        if (null != (characteristic = mTargetPeripheral.getCharacteristic(GattUUID.Characteristic.BloodPressureFeatureCharacteristic.getUuid()))) {
            mBleCommunicationExecutor.add(new BleEvent(BleEvent.Type.ReadCharacteristic, characteristic));
        }
        if (null != (characteristic = mTargetPeripheral.getCharacteristic(GattUUID.Characteristic.WeightScaleFeatureCharacteristic.getUuid()))) {
            mBleCommunicationExecutor.add(new BleEvent(BleEvent.Type.ReadCharacteristic, characteristic));
        }
        mBleCommunicationExecutor.exec();
    }

    private void startIndicationWaitTimer() {
        BleLog.dMethodIn();
        mMessageHandler.sendMessageDelayed(Message.obtain(mMessageHandler,
                MessageType.IndicationWaitTimeout.ordinal()), INDICATION_WAIT_TIME);
    }
    private byte[] getCurrentTimeData() {
        byte[] data = new byte[10];
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        data[0] = (byte) year;
        data[1] = (byte) ((year >> 8) & 0xFF);
        data[2] = (byte) (cal.get(Calendar.MONTH) + 1);
        data[3] = (byte) cal.get(Calendar.DAY_OF_MONTH);
        data[4] = (byte) cal.get(Calendar.HOUR_OF_DAY);
        data[5] = (byte) cal.get(Calendar.MINUTE);
        data[6] = (byte) cal.get(Calendar.SECOND);
        data[7] = (byte) ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1); // Rotate
        data[8] = (byte) (cal.get(Calendar.MILLISECOND) * 256 / 1000); // Fractions256
        data[9] = 0x01; // Adjust Reason: Manual time update

        String date = year + "/" + data[2] + "/" + data[3] + " " +
                String.format(Locale.US, "%1$02d:%2$02d:%3$02d", data[4], data[5], data[6]) +
                " (WeekOfDay:" + data[7] + " Fractions256:" + data[8] + " AdjustReason:" + data[9] + ")";
        StringBuilder sb = new StringBuilder("");
        for (byte b : data) {
            sb.append(String.format(Locale.US, "%02x,", b));
        }
        BleLog.d("CTS Tx Time:" + date);
        BleLog.d("CTS Tx Data:" + sb.toString());
        return data;
    }

    private boolean isBluetoothEnabled() {

        return getBluetoothAdapter().isEnabled();
    }
/*
    // the callback for scan device
    private ZzMedBLEDeviceManager.ZzMedBLEDeviceScanCB bleDeviceScanCB =
            new ZzMedBLEDeviceManager.ZzMedBLEDeviceScanCB() {

                @Override public void onFailure(ZzMedBLEErrMsg errMsg) {
                    final ZzMedBLEErrMsg fErrMsg = errMsg;
                    runOnUiThread(new Runnable() {

                        @Override public void run() {
                            if (fErrMsg != ZzMedBLEErrMsg.ZZMED_BLE_ERROR_DEVICE_SCAN_TIME_OUT) {
                                ToastUtils.showShort(fErrMsg.getErrMsg());
                            }
                        }
                    });
                    startScan();
                }

                @Override public void onScanProgress(int progress) {

                }

                @Override public void onScanComplete(ZzMedBLEDevice device) {
                    if (!isDeviceExist(device) && isBluetoothOpen()) {
                        mDevices.add(device);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {

                            @Override public void run() {
                                hem9200ScandeviceAdapter.notifyData(mDevices);
                            }
                        });
                    }
                }
            };

    private boolean isDeviceExist(ZzMedBLEDevice targetDevice) {
        for (ZzMedBLEDevice device : mDevices) {
            if (device.getBlueDevice().getName().equals(targetDevice.getBlueDevice().getName())) {
                return true;
            }
        }
        return false;
    }
    */
//========== xuetang start====================
//	GlucoseManager glucoseManager = new GlucoseManager(this.reactContext);

	private OMRONBLEDevice mHem126tDevice;
	private com.omron.ble.model.BGData mLastBgData;

	private void startBondActivity() {
//        OMRONBLEDeviceManager manager = OMRONBLEDeviceManager
//                .getBLEDevManager(this.reactContext);
		OMRONBLEDeviceManager manager = OMRONBLEDeviceManager
				.getBLEDevManager(this.context);
		manager.setSyncTime(true);
//		this.onPinCodeInput("892104");
		manager.scan(mScanDeviceListener);
	}
	private void onPinCodeInput(String pincode) {
		if (DeviceInfo.isDeviceInfoEmpty()) {
			DeviceInfo.savePincode(pincode);
		} else {
			DeviceInfo.updateDeviceInfo(pincode);
		}
//		mPincodeListener.onPinCodeInput(mPincode);
		startFragmentTransaction();
	}
	private void connectToDeviceFailed() {

	}

	private void startFragmentTransaction() {

	}

	private OMRONBLEDeviceManager.OMRONBLEDeviceScanCB mScanDeviceListener = new OMRONBLEDeviceManager.OMRONBLEDeviceScanCB() {

		@Override
		public void onFailure(OMRONBLEErrMsg errMsg) {
			//ɨ�賬ʱ
			Log.d(LOG_TAG, "OMRONBLEErrMsg  miao== errMsg" + errMsg);

			WritableMap map = Arguments.createMap();

			map.putString("state", errMsg.name());//JSON.toJSONString(errMsg)
			Log.d(LOG_TAG, "state: " + JSON.toJSONString(errMsg));
			sendEvent("BleManagerDidUpdateState", map);
			connectToDeviceFailed();
		}

		@Override
		public void onScanProgress(int progress) {

		}

		@Override
		public void onScanComplete(OMRONBLEDevice device) {
			mHem126tDevice = device;

//mxm20180102			List<DeviceInfo> list = DeviceInfo.getDeviceInfo();
//			device.connect(connectionCB, list.get(0).getBgPincode());
			device.connect(connectionCB, null);
		}
	};
	//the callback for connect to device
	private OMRONBLEDevice.OMRONBLEDeviceConnectionCB connectionCB = new OMRONBLEDevice.OMRONBLEDeviceConnectionCB() {
		public void onFailure(OMRONBLEErrMsg errMsg) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mHem126tDevice.disconnect(disconnectionCB);
					connectToDeviceFailed();
				}
			});
		}

		@Override
		public void onConnectionStateChange(OMRONBLEDeviceState status) {
			final OMRONBLEDeviceState fStatus = status;

			WritableMap map = Arguments.createMap();
			map.putString("state",  fStatus.toString());
			Log.d(LOG_TAG, "state:" + fStatus);

//			sendEvent("BleManagerDiscoverPeripheral", map);
			sendEvent("BleManagerDidUpdateState", map);
			switch (fStatus) {
				case STATE_CONNECTING:
					break;

				case STATE_CONNECTED:
					((OMRONBLEBGMDevice) mHem126tDevice).readData(-1, readBGDataCB);
					break;

				case STATE_DISCONNECTING:
					break;

				case STATE_DISCONNECTED:
					mHem126tDevice.disconnect(disconnectionCB);
					break;

				default:
					break;
			}



		}
	};

	//the callback for disconnect to device
	private OMRONBLEDevice.OMRONBLEDeviceConnectionCB disconnectionCB = new OMRONBLEDevice.OMRONBLEDeviceConnectionCB() {
		public void onFailure(OMRONBLEErrMsg errMsg) {
			connectToDeviceFailed();
		}

		@Override
		public void onConnectionStateChange(OMRONBLEDeviceState status) {

		}
	};

	private OMRONBLEBGMDevice.OMRONBLEReadBGDataCB readBGDataCB = new OMRONBLEBGMDevice.OMRONBLEReadBGDataCB() {

		@Override
		public void onFailure(OMRONBLEErrMsg errMsg) {
			connectToDeviceFailed();
		}

		@Override
		public void onDataReadComplete(List<BGData> data) {

			mLastBgData = getLastBgData(data);
			WritableMap map = Arguments.createMap();

			map.putString("entry",  JSON.toJSONString(mLastBgData));
			Log.d(LOG_TAG, "entry:" + JSON.toJSONString(mLastBgData));

			sendEvent("BleManagerBPMDataRcv", map);

			startFragmentTransaction();
		}

		@Override
		public void onProgress(int count, int total) {

		}
	};

	private com.omron.ble.model.BGData getLastBgData(List<com.omron.ble.model.BGData> data) {
		Comparator<BGData> flashBackComparator = new Comparator<com.omron.ble.model.BGData>() {

			@Override
			public int compare(com.omron.ble.model.BGData lhs, com.omron.ble.model.BGData rhs) {
				return rhs.getTime().compareTo(lhs.getTime());
			}
		};
		Collections.sort(data, flashBackComparator);
		return data.get(0);
	}
//====================== xuetang  end=========================
}
