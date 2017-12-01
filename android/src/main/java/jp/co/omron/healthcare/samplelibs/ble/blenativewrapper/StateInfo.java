//
//  StateInfo.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.samplelibs.ble.blenativewrapper;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.EnumMap;

public class StateInfo implements Parcelable {

    /**
     * Implement the Parcelable interface
     */
    public static final Creator<StateInfo> CREATOR =
            new Creator<StateInfo>() {
                public StateInfo createFromParcel(Parcel in) {
                    return new StateInfo(in);
                }

                public StateInfo[] newArray(int size) {
                    return new StateInfo[size];
                }
            };
    private static final EnumMap<DetailedState, ConnectionState> sStateMap = new EnumMap<>(DetailedState.class);

    static {
        sStateMap.put(DetailedState.Disconnected, ConnectionState.Disconnected);
        sStateMap.put(DetailedState.Pairing, ConnectionState.Connecting);
        sStateMap.put(DetailedState.GattConnecting, ConnectionState.Connecting);
        sStateMap.put(DetailedState.ServiceDiscovering, ConnectionState.Connecting);
        sStateMap.put(DetailedState.Cleanup, ConnectionState.Connecting);
        sStateMap.put(DetailedState.CommunicationReady, ConnectionState.Connected);
        sStateMap.put(DetailedState.Communicating, ConnectionState.Connected);
        sStateMap.put(DetailedState.Disconnecting, ConnectionState.Disconnecting);
        sStateMap.put(DetailedState.Dead, ConnectionState.Disconnected);
    }

    private BondState mBondState;
    private ConnectionState mConnectionState;
    private DetailedState mDetailedState;
    private Reason mReason;
    private AclConnectionState mAclConnectionState;
    private GattConnectionState mGattConnectionState;
    private StateMonitor mStateMonitor;

    StateInfo() {
        mBondState = BondState.NotBonded;
        setDetailedState(DetailedState.Disconnected, null, false);
        mAclConnectionState = AclConnectionState.Disconnected;
        mGattConnectionState = GattConnectionState.Disconnected;
    }

    StateInfo(StateInfo source) {
        mBondState = source.getBondState();
        mConnectionState = source.getConnectionState();
        mDetailedState = source.getDetailedState();
        mReason = source.getReason();
        mAclConnectionState = source.getAclConnectionState();
        mGattConnectionState = source.getGattConnectionState();
    }

    protected StateInfo(Parcel in) {
        synchronized (this) {
            mBondState = BondState.valueOf(in.readString());
            mConnectionState = ConnectionState.valueOf(in.readString());
            mDetailedState = DetailedState.valueOf(in.readString());
            mReason = Reason.valueOf(in.readString());
            mAclConnectionState = AclConnectionState.valueOf(in.readString());
            mGattConnectionState = GattConnectionState.valueOf(in.readString());
        }
    }

    public boolean isBonded() {
        synchronized (this) {
            return BondState.Bonded == mBondState;
        }
    }

    public BondState getBondState() {
        synchronized (this) {
            return mBondState;
        }
    }

    public boolean isConnected() {
        synchronized (this) {
            return ConnectionState.Connected == mConnectionState;
        }
    }

    public ConnectionState getConnectionState() {
        synchronized (this) {
            return mConnectionState;
        }
    }

    public DetailedState getDetailedState() {
        synchronized (this) {
            return mDetailedState;
        }
    }

    public Reason getReason() {
        synchronized (this) {
            return mReason;
        }
    }

    public AclConnectionState getAclConnectionState() {
        synchronized (this) {
            return mAclConnectionState;
        }
    }

    public GattConnectionState getGattConnectionState() {
        synchronized (this) {
            return mGattConnectionState;
        }
    }

    void setStateMonitor(StateMonitor stateMonitor) {
        synchronized (this) {
            mStateMonitor = stateMonitor;
        }
    }

    void setBondState(int bondState, boolean isNotify) {
        BondState bondStateEnum = BondState.NotBonded;
        switch (bondState) {
            case BluetoothDevice.BOND_NONE:
                bondStateEnum = BondState.NotBonded;
                break;
            case BluetoothDevice.BOND_BONDING:
                bondStateEnum = BondState.Bonding;
                break;
            case BluetoothDevice.BOND_BONDED:
                bondStateEnum = BondState.Bonded;
                break;
            default:
                break;
        }
        setBondState(bondStateEnum, isNotify);
    }

    void setBondState(BondState bondState, boolean isNotify) {
        synchronized (this) {
            if (mBondState == bondState) {
                return;
            }
            mBondState = bondState;
            if (null != mStateMonitor && isNotify) {
                mStateMonitor.onBondStateChanged(mBondState);
            }
        }
    }

    private void setConnectionState(ConnectionState connectionState, boolean isNotify) {
        synchronized (this) {
            if (mConnectionState == connectionState) {
                return;
            }
            mConnectionState = connectionState;
            if (null != mStateMonitor && isNotify) {
                mStateMonitor.onConnectionStateChanged(mConnectionState);
            }
        }
    }

    void setDetailedState(DetailedState detailedState, Reason reason, boolean isNotify) {
        synchronized (this) {
            if (mDetailedState == detailedState) {
                return;
            }
            setConnectionState(sStateMap.get(detailedState), isNotify);
            mDetailedState = detailedState;
            mReason = reason;
            if (null != mStateMonitor && isNotify) {
                mStateMonitor.onDetailedStateChanged(mDetailedState);
            }
        }
    }

    void setAclConnectionState(AclConnectionState aclConnectionState, boolean isNotify) {
        synchronized (this) {
            if (mAclConnectionState == aclConnectionState) {
                return;
            }
            mAclConnectionState = aclConnectionState;
            if (null != mStateMonitor && isNotify) {
                mStateMonitor.onAclConnectionStateChanged(mAclConnectionState);
            }
        }
    }

    void setGattConnectionState(GattConnectionState gattConnectionState, boolean isNotify) {
        synchronized (this) {
            if (mGattConnectionState == gattConnectionState) {
                return;
            }
            mGattConnectionState = gattConnectionState;
            if (null != mStateMonitor && isNotify) {
                mStateMonitor.onGattConnectionStateChanged(mGattConnectionState);
            }
        }
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
        synchronized (this) {
            dest.writeString(mBondState.name());
            dest.writeString(mConnectionState.name());
            dest.writeString(mDetailedState.name());
            dest.writeString(mReason.name());
            dest.writeString(mAclConnectionState.name());
            dest.writeString(mGattConnectionState.name());
        }
    }

    public enum ConnectionState {
        Disconnected,
        Connecting,
        Connected,
        Disconnecting,
        Unknown
    }

    public enum DetailedState {
        Disconnected,
        Pairing,
        GattConnecting,
        ServiceDiscovering,
        Cleanup,
        CommunicationReady,
        Communicating,
        Disconnecting,
        Dead,
        Unknown
    }

    public enum Reason {
        ConnectRequest,
        BluetoothOff,
        AlreadyConnected,
        OSNativeError,
        DisconnectRequest,
        DidDisconnection,
        EncryptionFailed,
        Timeout,
        Unknown
    }

    public enum BondState {
        NotBonded,
        Bonding,
        Bonded,
        Unknown
    }

    public enum AclConnectionState {
        Disconnected,
        Connected,
        Unknown
    }

    public enum GattConnectionState {
        Disconnected,
        Connected,
        Unknown
    }

    public interface StateMonitor {
        void onBondStateChanged(@NonNull StateInfo.BondState bondState);

        void onAclConnectionStateChanged(@NonNull StateInfo.AclConnectionState aclConnectionState);

        void onGattConnectionStateChanged(@NonNull StateInfo.GattConnectionState gattConnectionState);

        void onConnectionStateChanged(@NonNull StateInfo.ConnectionState connectionState);

        void onDetailedStateChanged(@NonNull StateInfo.DetailedState detailedState);
    }
}
