package com.bluetooth.leo.bluetoothcommunication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.potato.Potato;
import com.leo.potato.PotatoInjection;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Logger;

public class FindDeviceActivity extends AppCompatActivity {
    @PotatoInjection(idStr = "begin_scan", click = "beginScanDevices")
    Button beginScan;
    @PotatoInjection(idStr = "recycleView")
    RecyclerView recycleView;

    boolean isBluetoothOpen = true;
    private BluetoothAdapter bluetoothAdapter;
    private static final int BLUETOOTH_REQUEST_CODE = 1001;
    public static String uniqueUuid = "";

    ArrayList<BluetoothDevice> devices = new ArrayList<>();
    public static final String DEVICE_INFO = "device_info";
    public static final String BUNDLE_INFO = "bundle_info";
    private Adapter chessAdapter;
//    private ScanBluetoothDevicesCallback scanCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Potato.initInjection(this);

        init();
    }

    private void init() {
        checkBleSupport();
        initBluetoothAdapter();
        checkBlutoothOpen();
        recycleView.setLayoutManager(new LinearLayoutManager(FindDeviceActivity.this));
        chessAdapter = new Adapter();
        recycleView.setAdapter(chessAdapter);
    }

    private void initBluetoothAdapter() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        beginScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(FindDeviceActivity.this, "蓝牙未开", Toast.LENGTH_SHORT).show();
                }
                devices.clear();
                chessAdapter.notifyDataSetChanged();
                beginScanDevices(v);
            }
        });
    }

    private void checkBlutoothOpen() {
        if (!bluetoothAdapter.isEnabled()) {
            isBluetoothOpen = false;
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BLUETOOTH_REQUEST_CODE);
        } else {
            isBluetoothOpen = true;
        }
    }

    private void checkBleSupport() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(FindDeviceActivity.this, "本设备不支持Ble数据传输", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLUETOOTH_REQUEST_CODE) {

//            checkBlutoothOpen();
        }
    }

    void beginScanDevices(View v) {
        if (bluetoothAdapter != null) {
//            final BluetoothLeScanner scan = bluetoothAdapter.getBluetoothLeScanner();
//            scanCallback = new ScanBluetoothDevicesCallback();
//            scan.startScan(scanCallback);
//            handler.postDelayed(new TimerTask() {
//                @Override
//                public void run() {
//                    isScan = false;
//                    scan.startScan(scanCallback);
//                    chessAdapter.notifyDataSetChanged();
//                }
//            }, 1000);
            bluetoothAdapter.startLeScan(deviceScanResults);
            isScan = true;
            handler.postDelayed(new TimerTask() {
                @Override
                public void run() {
                    isScan = false;
                    bluetoothAdapter.stopLeScan(deviceScanResults);
                    chessAdapter.notifyDataSetChanged();
                }
            }, 1000);
        }


    }

    boolean isScan = false;
    DeviceScanResults deviceScanResults = new DeviceScanResults();
    Handler handler = new Handler();

//    class ScanBluetoothDevicesCallback extends ScanCallback {
//        @Override
//        public void onBatchScanResults(List<ScanResult> results) {
//            super.  onBatchScanResults(results);
//
//        }
//
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
//            super.onScanResult(callbackType, result);
//
//        }
//
//        @Override
//        public void onScanFailed(int errorCode) {
//            super.onScanFailed(errorCode);
//
//        }
//    }

    class DeviceScanResults implements BluetoothAdapter.LeScanCallback {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (isScan) {

                ParcelUuid[] uuid = device.getUuids();
//                if (uuid == null)
//                    return;
//                for (ParcelUuid puid : uuid) {
//                    UUID uid = puid.getUuid();
//                    Log.i("-------", uid.toString());
//                }
                String macAddress = device.getAddress();
                if (!checkReplicated(macAddress)) {
                    devices.add(device);
                }
//                chessAdapter.notifyDataSetChanged();
            } else {
                devices.clear();
            }
        }
    }

    private boolean checkReplicated(String address) {
        for (BluetoothDevice device : devices) {
            if (address.equalsIgnoreCase(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    class Adapter extends RecyclerView.Adapter<Adapter.ChessViewHolder> {
        @Override
        public ChessViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ChessViewHolder chessViewHolder = new ChessViewHolder(LayoutInflater.from(FindDeviceActivity.this).inflate(R.layout.item_recyclerview, null));
            return chessViewHolder;
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public void onBindViewHolder(ChessViewHolder holder, final int position) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(FindDeviceActivity.this, BluetoothDetailInfoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(DEVICE_INFO, devices.get(position));
                    intent.putExtra(BUNDLE_INFO, bundle);
                    startActivity(intent);
                }
            });
            holder.tvDes.setText(TextUtils.isEmpty(devices.get(position).getName()) ? "null" : devices.get(position).getName());
            holder.tvMacAddress.setText(devices.get(position).getAddress());
        }


        class ChessViewHolder extends RecyclerView.ViewHolder {

            public ChessViewHolder(View itemView) {
                super(itemView);
                tvDes = (TextView) itemView.findViewById(R.id.tvDes);
                tvMacAddress = (TextView) itemView.findViewById(R.id.tvMacAddress);
            }

            public TextView tvDes;
            public TextView tvMacAddress;
        }

    }

}
