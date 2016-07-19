package com.bluetooth.leo.bluetoothlib;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by Administrator on 2016/7/15.
 */
public class BluetoothManager {
    private static final String BLUETOOTH_TAG = "bluetooth_manager";
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> devices;
    private Activity activity;
    private static final int SEARCH_TIMEMILLS = 1000;
    private Handler handler = new Handler();
    private boolean isScanning = false;
    private BluetoothGatt bluetoothGatt;

    //device
    LeScanDeviceCallback leScanDeviceCallback;
    List<BluetoothDevice> scanDevices;
    GattCallback gattCallback;


    public BluetoothManager() {
        if (bluetoothAdapter == null)
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * get the single instance
     *
     * @param activity
     * @return
     */
    public BluetoothManager getInstance(Activity activity) {
        synchronized (bluetoothManager) {
            if (bluetoothManager == null) {
                bluetoothManager = new BluetoothManager();
            }
        }
        if (this.activity != null && !this.activity.equals(activity)) {
            leScanDeviceCallback = new LeScanDeviceCallback();
            scanDevices = new ArrayList<>();
            gattCallback = new GattCallback();
        }
        this.activity = activity;

        return bluetoothManager;
    }

    /**
     * check if the device support ble
     *
     * @return
     */
    public boolean checkBleSupport() {
        if (activity != null) {
            if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                return false;
            }
            return true;
        } else {
            Log.i(BLUETOOTH_TAG, "no activity reference");
            return false;
        }
    }

    /**
     * check if the bluetooth is open
     *
     * @return
     */
    public boolean checkBluetoothOpen() {
        return bluetoothAdapter.isEnabled();
    }

    /**
     * open the system's bluetooth setting page
     *
     * @param requestCode the return code
     */
    public void openSystemBluetoothSetting(int requestCode) {
        if (!checkBluetoothOpen()) {
            Intent blueIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(blueIntent, requestCode);
        }
    }

    /**
     * start search devices nearby
     *
     * @param callback
     */
    public void startSearchDeviceNearby(final BluetoothAdapter.LeScanCallback callback) {
        handler.postDelayed(new TimerTask() {
            @Override
            public void run() {
                isScanning = false;
                bluetoothAdapter.stopLeScan(callback);
            }
        }, SEARCH_TIMEMILLS);
        isScanning = true;
        bluetoothAdapter.startLeScan(callback);
    }

    /**
     * start search devices nearby in given range
     *
     * @param callback
     * @uuids the device range
     */
    public void startSearchDeviceNearby(UUID[] uuids, final BluetoothAdapter.LeScanCallback callback) {
        handler.postDelayed(new TimerTask() {
            @Override
            public void run() {
                isScanning = false;
                bluetoothAdapter.stopLeScan(callback);
            }
        }, SEARCH_TIMEMILLS);
        isScanning = true;
        bluetoothAdapter.startLeScan(uuids, callback);
    }

    public void startSearchDeviceNearby() {
        handler.postDelayed(new TimerTask() {
            @Override
            public void run() {
                isScanning = false;
                bluetoothAdapter.stopLeScan(leScanDeviceCallback);
            }
        }, SEARCH_TIMEMILLS);
        isScanning = true;
        bluetoothAdapter.startLeScan(leScanDeviceCallback);
    }

    /**
     * return scaned devices
     *
     * @return
     */
    public List<BluetoothDevice> getScanedDevices() {
        return scanDevices;
    }

    /**
     * connect the certain device
     *
     * @param device   the device searched
     * @param callback
     */
    public void startConnect(BluetoothDevice device, AbsGattCallback callback) {
        bluetoothGatt = device.connectGatt(activity, false, callback);
    }

    /**
     * connect the certain device
     *
     * @param device the device searched
     */
    public void startConnect(BluetoothDevice device) {
        bluetoothGatt = device.connectGatt(activity, false, gattCallback);
    }

    class LeScanDeviceCallback implements BluetoothAdapter.LeScanCallback {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            scanDevices.add(device);
        }
    }

    public class GattCallback extends BluetoothGattCallback {
        public GattCallback() {
            super();
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i(BLUETOOTH_TAG, "onConnectionStateChange");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(BLUETOOTH_TAG, "STATE_CONNECTED");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(BLUETOOTH_TAG, "STATE_DISCONNECTED");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.i(BLUETOOTH_TAG, "onServicesDiscovered");
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i(BLUETOOTH_TAG, "onCharacteristicRead");

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(BLUETOOTH_TAG, "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(BLUETOOTH_TAG, "onCharacteristicChanged");
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.i(BLUETOOTH_TAG, "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i(BLUETOOTH_TAG, "onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.i(BLUETOOTH_TAG, "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.i(BLUETOOTH_TAG, "onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.i(BLUETOOTH_TAG, "onMtuChanged");
        }
    }

    public abstract class AbsGattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i(BLUETOOTH_TAG, "onConnectionStateChange");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(BLUETOOTH_TAG, "STATE_CONNECTED");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(BLUETOOTH_TAG, "STATE_DISCONNECTED");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            String strValue = characteristic.getStringValue(0);
            readCharacteristicString(strValue);
        }

        public abstract void readCharacteristicString(String value);

        public abstract void writeCharacteristicString(String value);

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }
    }


}

