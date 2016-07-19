package com.bluetooth.leo.bluetoothcommunication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.leo.potato.Potato;
import com.leo.potato.PotatoInjection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Potato.initInjection(this);
        achieveBluetoothAdapter();
    }
    private void achieveBluetoothAdapter() {
        BluetoothManager bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bm.getAdapter();
    }
//    public void checkBluetoothOpen() {
//        if(!bluetoothAdapter.isEnabled()) {
//
//            if (!bluetoothAdapter.isEnabled()){
//
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//
//                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//
//            }
//
//        }
//    }
}
