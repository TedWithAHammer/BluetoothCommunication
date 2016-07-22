package com.bluetooth.leo.bluetoothcommunication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.leo.potato.Potato;
import com.leo.potato.PotatoInjection;

import java.util.List;
import java.util.UUID;


public class BluetoothDetailInfoActivity extends AppCompatActivity {
    private static final String Tag = "log_info";
    @PotatoInjection(id = R.id.startConnect)
    Button startConnect;
    @PotatoInjection(id = R.id.receiveData)
    TextView receiveData;
    @PotatoInjection(id = R.id.lostPackageNum)
    TextView lostPackageNum;


    private BluetoothDevice device;
    private BluetoothGatt mGatt;

    protected static String uuidQppService = "0000fee9-0000-1000-8000-00805f9b34fb";
    protected static String uuidQppCharWrite = "d44bc439-abfd-45a2-b575-925416129600";
    private static final String UUIDDes = "00002902-0000-1000-8000-00805f9b34fb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_detail_info);
        Potato.initInjection(this);
        if (getIntent() != null) {
            Bundle bundle = getIntent().getBundleExtra(FindDeviceActivity.BUNDLE_INFO);
            device = bundle.getParcelable(FindDeviceActivity.DEVICE_INFO);
        }
        bindEvent();
    }

    private void bindEvent() {
        startConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (device != null) {
                    mGatt = device.connectGatt(BluetoothDetailInfoActivity.this, true, new ChessBluetoothGatt());
                }
            }
        });
    }

    class ChessBluetoothGatt extends BluetoothGattCallback {
        public ChessBluetoothGatt() {
            super();
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i(Tag, "onConnectionStateChange");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mGatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.i(Tag, "onConnectionStateChange");
            filterTheData(gatt);
        }

        private void filterTheData(BluetoothGatt gatt) {
            BluetoothGattService server = mGatt.getService(UUID.fromString(uuidQppService));
            List<BluetoothGattCharacteristic> datas = server.getCharacteristics();
            for (BluetoothGattCharacteristic data : datas) {
                if (data.getUuid().toString().equals(uuidQppCharWrite)) {

                } else if (data.getProperties() == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
                    gatt.setCharacteristicNotification(data, true);
                    BluetoothGattDescriptor descriptor = data.getDescriptor(UUID.fromString(UUIDDes));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i(Tag, "onConnectionStateChange");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    byte[] data = characteristic.getValue();
                    int lastNum = data[data.length - 1];
                    lastNum = lastNum & 0xff;
                    decodeData(lastNum);
                    receiveData.setText(lastNum + "");
                }
            });
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(Tag, "onConnectionStateChange");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(Tag, "onConnectionStateChange");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    byte[] data = characteristic.getValue();
                    int lastNum = data[data.length - 1];
                    lastNum = lastNum & 0xff;
                    decodeData(lastNum);
                    receiveData.setText(lastNum + "");
                }
            });
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.i(Tag, "onConnectionStateChange");


        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i(Tag, "onConnectionStateChange");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.i(Tag, "onConnectionStateChange");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.i(Tag, "onConnectionStateChange");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.i(Tag, "onConnectionStateChange");
        }
    }

    int counter = 1;
    int preNum = 0;

    int receiveNum = 0;

    private void decodeData(int lastNum) {
        if (lastNum < preNum) {
            lostPackageNum.setText(counter * 256 + "个包收到" + receiveNum + "个包");
            counter++;
        }
        preNum = lastNum;
        receiveNum++;
    }

    Handler handler = new Handler();

}
