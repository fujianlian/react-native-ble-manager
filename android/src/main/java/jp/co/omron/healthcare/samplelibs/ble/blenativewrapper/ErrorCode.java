//
//  ErrorCode.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.samplelibs.ble.blenativewrapper;

public enum ErrorCode {
    DeadObject,
    Destroy,
    BluetoothOff,
    BadState,
    Busy,
    PairingFailed,
    PairingTimeout,
    GattConnectionFailure,
    GattConnectionTimeout,
    DiscoverServiceFailure,
    DiscoverServiceTimeout,
    CommunicationTimeout,
    InvalidResponseData,
    OSNativeError,
    Unknown
}
