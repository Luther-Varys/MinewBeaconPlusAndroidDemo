package com.minew.beaconplusdemo;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.minew.beaconplus.sdk.MTCentralManager;
import com.minew.beaconplus.sdk.MTPeripheral;
import com.minew.beaconplus.sdk.Utils.LogUtils;
import com.minew.beaconplus.sdk.enums.BluetoothState;
import com.minew.beaconplus.sdk.enums.ConnectionStatus;
import com.minew.beaconplus.sdk.exception.MTException;
import com.minew.beaconplus.sdk.frames.MinewFrame;
import com.minew.beaconplus.sdk.interfaces.ConnectionStatueListener;
import com.minew.beaconplus.sdk.interfaces.GetPasswordListener;
import com.minew.beaconplus.sdk.interfaces.MTCentralManagerListener;
import com.minew.beaconplus.sdk.interfaces.OnBluetoothStateChangedListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT          = 3;
    private static final int PERMISSION_COARSE_LOCATION = 2;
    @BindView(R.id.recycle)
    RecyclerView mRecycle;

    private MTCentralManager mMtCentralManager;
    private RecycleAdapter   mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        if (!ensureBleExists())
            finish();
        if (!isBLEEnabled()) {
            showBLEDialog();
        }
        initView();
        initManager();
        getRequiredPermissions();
        initListener();
    }

    private boolean ensureBleExists() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Phone does not support BLE", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    protected boolean isBLEEnabled() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter adapter = bluetoothManager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    private void showBLEDialog() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                initData();
            } else {
                finish();
            }
        }
    }

    private void initView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecycle.setLayoutManager(layoutManager);
        mAdapter = new RecycleAdapter();
        mRecycle.setAdapter(mAdapter);
        mRecycle.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager
                .HORIZONTAL));
    }

    private void initListener() {
        mMtCentralManager.setMTCentralManagerListener(new MTCentralManagerListener() {
            @Override
            public void onScanedPeripheral(final List<MTPeripheral> peripherals) {
                Log.e("demo", "scan size is: " + peripherals.size());
                mAdapter.setData(peripherals);
            }
        });
        mAdapter.setOnItemClickListener(new RecycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MTPeripheral mtPeripheral = mAdapter.getData(position);
                mMtCentralManager.connect(mtPeripheral, connectionStatueListener);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private ConnectionStatueListener connectionStatueListener = new ConnectionStatueListener() {
        @Override
        public void onUpdateConnectionStatus(final ConnectionStatus connectionStatus, final GetPasswordListener getPasswordListener) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (connectionStatus) {
                        case CONNECTING:
                            Log.e("tag", "CONNECTING");
                            Toast.makeText(MainActivity.this, "CONNECTING", Toast.LENGTH_SHORT).show();
                            break;
                        case CONNECTED:
                            Log.e("tag", "CONNECTED");
                            Toast.makeText(MainActivity.this, "CONNECTED", Toast.LENGTH_SHORT).show();
                            break;
                        case READINGINFO:
                            Log.e("tag", "READINGINFO");
                            Toast.makeText(MainActivity.this, "READINGINFO", Toast.LENGTH_SHORT).show();
                            break;
                        case DEVICEVALIDATING:
                            Log.e("tag", "DEVICEVALIDATING");
                            Toast.makeText(MainActivity.this, "DEVICEVALIDATING", Toast.LENGTH_SHORT).show();
                            break;
                        case PASSWORDVALIDATING:
                            Log.e("tag", "PASSWORDVALIDATING");
                            Toast.makeText(MainActivity.this, "PASSWORDVALIDATING", Toast.LENGTH_SHORT).show();
                            String password = "minew123";
                            getPasswordListener.getPassword(password);
                            break;
                        case SYNCHRONIZINGTIME:
                            Log.e("tag", "SYNCHRONIZINGTIME");
                            Toast.makeText(MainActivity.this, "SYNCHRONIZINGTIME", Toast.LENGTH_SHORT).show();
                            break;
                        case READINGCONNECTABLE:
                            Log.e("tag", "READINGCONNECTABLE");
                            Toast.makeText(MainActivity.this, "READINGCONNECTABLE", Toast.LENGTH_SHORT).show();
                            break;
                        case READINGFEATURE:
                            Log.e("tag", "READINGFEATURE");
                            Toast.makeText(MainActivity.this, "READINGFEATURE", Toast.LENGTH_SHORT).show();
                            break;
                        case READINGFRAMES:
                            Log.e("tag", "READINGFRAMES");
                            Toast.makeText(MainActivity.this, "READINGFRAMES", Toast.LENGTH_SHORT).show();
                            break;
                        case READINGTRIGGERS:
                            Log.e("tag", "READINGTRIGGERS");
                            Toast.makeText(MainActivity.this, "READINGTRIGGERS", Toast.LENGTH_SHORT).show();
                            break;
                        case COMPLETED:
                            Log.e("tag", "COMPLETED");
                            Toast.makeText(MainActivity.this, "COMPLETED", Toast.LENGTH_SHORT).show();
                            break;
                        case CONNECTFAILED:
                        case DISCONNECTED:
                            Log.e("tag", "DISCONNECTED");
                            Toast.makeText(MainActivity.this, "DISCONNECTED", Toast.LENGTH_SHORT).show();
                            break;
                    }

                }
            });
        }

        @Override
        public void onError(MTException e) {
            Log.e("tag", e.getMessage());
        }
    };

    @Override
    public void onRequestPermissionsResult(int code, String permissions[], int[] grantResults) {
        switch (code) {
            case PERMISSION_COARSE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initData();
                } else {
                    finish();
                }
                break;
        }
    }


    private void initManager() {
        mMtCentralManager = MTCentralManager.getInstance(this);
        //startservice
        mMtCentralManager.startService();
        BluetoothState bluetoothState = mMtCentralManager.getBluetoothState(this);
        switch (bluetoothState) {
            case BluetoothStateNotSupported:
                Log.e("tag", "BluetoothStateNotSupported");
                break;
            case BluetoothStatePowerOff:
                Log.e("tag", "BluetoothStatePowerOff");
                break;
            case BluetoothStatePowerOn:
                Log.e("tag", "BluetoothStatePowerOn");
                break;
        }

        mMtCentralManager.setBluetoothChangedListener(new OnBluetoothStateChangedListener() {
            @Override
            public void onStateChanged(BluetoothState state) {
                switch (state) {
                    case BluetoothStateNotSupported:
                        Log.e("tag", "BluetoothStateNotSupported");
                        break;
                    case BluetoothStatePowerOff:
                        Log.e("tag", "BluetoothStatePowerOff");
                        break;
                    case BluetoothStatePowerOn:
                        Log.e("tag", "BluetoothStatePowerOn");
                        break;
                }
            }
        });
    }

    private void getRequiredPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_COARSE_LOCATION);
        } else {
            initData();
        }
    }

    private void initData() {
        //三星手机系统可能会限制息屏下扫描，导致息屏后无法获取到广播数据
        mMtCentralManager.startScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMtCentralManager.stopService();
    }
}
