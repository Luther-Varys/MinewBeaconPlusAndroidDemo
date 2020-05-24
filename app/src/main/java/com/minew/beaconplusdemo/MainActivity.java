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
import com.minew.beaconplus.sdk.MTConnectionFeature;
import com.minew.beaconplus.sdk.MTConnectionHandler;
import com.minew.beaconplus.sdk.MTPeripheral;
import com.minew.beaconplus.sdk.Utils.LogUtils;
import com.minew.beaconplus.sdk.enums.BluetoothState;
import com.minew.beaconplus.sdk.enums.ConnectState;
import com.minew.beaconplus.sdk.enums.ConnectionStatus;
import com.minew.beaconplus.sdk.enums.FeatureSupported;
import com.minew.beaconplus.sdk.enums.FrameType;
import com.minew.beaconplus.sdk.enums.PasswordState;
import com.minew.beaconplus.sdk.enums.TriggerType;
import com.minew.beaconplus.sdk.enums.Version;
import com.minew.beaconplus.sdk.exception.MTException;
import com.minew.beaconplus.sdk.frames.IBeaconFrame;
import com.minew.beaconplus.sdk.frames.LineBeaconFrame;
import com.minew.beaconplus.sdk.frames.MinewFrame;
import com.minew.beaconplus.sdk.frames.UidFrame;
import com.minew.beaconplus.sdk.interfaces.ConnectionStatueListener;
import com.minew.beaconplus.sdk.interfaces.GetPasswordListener;
import com.minew.beaconplus.sdk.interfaces.MTCOperationCallback;
import com.minew.beaconplus.sdk.interfaces.MTCentralManagerListener;
import com.minew.beaconplus.sdk.interfaces.OnBluetoothStateChangedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.minew.beaconplus.sdk.enums.FrameType.FrameiBeacon;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT          = 3;
    private static final int PERMISSION_COARSE_LOCATION = 2;
    @BindView(R.id.recycle)
    RecyclerView mRecycle;

    private MTCentralManager mMtCentralManager;
    private RecycleAdapter   mAdapter;
    private MTPeripheral     mtPeripheralSelected;

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

                String name = mtPeripheral.mMTFrameHandler.getName();
                String mac = mtPeripheral.mMTFrameHandler.getMac();
                int rssi = mtPeripheral.mMTFrameHandler.getRssi();
                int battery = mtPeripheral.mMTFrameHandler.getBattery();


                List<MinewFrame> advFrames = mtPeripheral.mMTFrameHandler.getAdvFrames();
                for (int i = 0; i < advFrames.size(); i++){
                    MinewFrame advFrame = advFrames.get(i);
                    FrameType frameType = advFrame.getFrameType();

                    if (frameType == FrameiBeacon) {
                        IBeaconFrame iBeaconframe = (IBeaconFrame) advFrame;
                        int txPower = iBeaconframe.getTxPower();
                        int radioPower = iBeaconframe.getRadiotxPower();
                        int major = iBeaconframe.getMajor();
                        int minor = iBeaconframe.getMinor();
                        String iuuid = iBeaconframe.getUuid();


                        // create a uid instance
                        IBeaconFrame iBeaconFrame = new IBeaconFrame();
                        iBeaconFrame.setFrameType(FrameiBeacon);
                        iBeaconFrame.setMinor(111);
                        iBeaconFrame.setMajor(222);
                        iBeaconFrame.setCurSlot(1);
                        iBeaconFrame.setAdvtxPower(txPower);
                        iBeaconFrame.setRadiotxPower(radioPower);
                        iBeaconFrame.setAdvInterval(300);
                        iBeaconFrame.setUuid(iuuid);

                        mtPeripheral.mMTConnectionHandler.writeSlotFrame(iBeaconframe, 0, new MTCOperationCallback() {
                            @Override
                            public void onOperation(boolean success, MTException mtException) {
                                if(success){
                                    Log.v("beaconplus","Success!");
                                }else{
                                    Log.v("beaconplus",mtException.getMessage());
                                }
                            }
                        });



                    }
                }

                //mtPeripheralSelected = mtPeripheral;
                //mMtCentralManager.stopScan();
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                //ArrayList<MinewFrame> advFrames = mtPeripheral.mMTFrameHandler.getAdvFrames();

//                for (int counter = 0; counter < advFrames.size(); counter++) {
//                    MinewFrame minewFrame = advFrames.get(counter);
//
//                    //System.out.println(advFrames.get(counter));
//                    minewFrame.
//                }
//                mMtCentralManager.stopScan();
//                mMtCentralManager.connect(mtPeripheral, connectionStatueListener);

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void getInfo(MTPeripheral mtPeripheral){
        // device features
        MTConnectionFeature mtConnectionFeature = mtPeripheral.mMTConnectionHandler.mTConnectionFeature;
//// atitude of slot(s),
//        int slotAtitude = mtConnectionFeature.getSlotAtitude();
//        // parameters can be modified：none，adv,txpower,adv/txpower
//        FeatureSupported featureSupported = mtConnectionFeature.getFeatureSupported();
//// // frames supported（multiple）
//        List<FrameType> supportedSlots = mtConnectionFeature.getSupportedSlots();
//// Txpower supported（multiple）
//        byte[] supportedTxpowers = mtConnectionFeature.getSupportedTxpowers();
//// trigger supported（multiple）
//        ArrayList<TriggerType> supportTriggers = mtConnectionFeature.supportTriggers;
//// Version of firmware;
        Version version = mtConnectionFeature.getVersion();
    }


    private ConnectionStatueListener connectionStatueListener = new ConnectionStatueListener() {
        @Override
        public void onUpdateConnectionStatus(final ConnectionStatus connectionStatus, final GetPasswordListener getPasswordListener) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //ArrayList<MinewFrame> allFrames;
                    switch (connectionStatus) {
                        case CONNECTING:
                            Log.e("tag", "CONNECTING");
                            Toast.makeText(MainActivity.this, "CONNECTING", Toast.LENGTH_SHORT).show();
                            break;
                        case CONNECTED:
                            {
                                Log.e("tag", "CONNECTED");
                                Toast.makeText(MainActivity.this, "CONNECTED", Toast.LENGTH_SHORT).show();
//                                ArrayList<MinewFrame> allFrames = mtPeripheralSelected.mMTFrameHandler.getAdvFrames();
                            }
                            break;
                        case READINGINFO:
                            Log.e("tag", "READINGINFO");
                            Toast.makeText(MainActivity.this, "READINGINFO", Toast.LENGTH_SHORT).show();

//                            mMtCentralManager.stopScan();
                            MTConnectionHandler mtConnectionHandler2 = mtPeripheralSelected.mMTConnectionHandler;
                            //mtPeripheralSelected.mMTFrameHandler.;
// current connection status
//                            ConnectState connectState = mtConnectionHandler2.getConnectState();
// password require or not. None, Require
//                            PasswordState passwordState = mtConnectionHandler2.getPasswordState();
// device info, such as：（Firmware Version： 0.9.1）;
                            HashMap<String, String> systeminfos = mtConnectionHandler2.systeminfos;
                            Log.e("tag", "ZR readinginfo count "+systeminfos.size());
//                            String manufacturer = systeminfos.get(Constants.manufacturer);
//                            String modlenumber = systeminfos.get(Constants.modlenumber);
//                            String macAddress = systeminfos.get(Constants.serialnumber);
//                            String hardware = systeminfos.get(Constants.hardware);
//                            String firmware = systeminfos.get(Constants.firmware);
//                            String software = systeminfos.get(Constants.software);
                            //mMtCentralManager.startScan();
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
                        case READINGFRAMES: {
                            Log.e("tag", "READINGFRAMES");
                            Toast.makeText(MainActivity.this, "READINGFRAMES", Toast.LENGTH_SHORT).show();
//                            mtPeripheralSelected.mMTConnectionHandler.allFrames;
//                            MTConnectionHandler mtConnectionHandler = mtPeripheralSelected.mMTConnectionHandler;
//                            ArrayList<MinewFrame> allFrames = mtConnectionHandler.allFrames;
//                            ArrayList<MinewFrame> allFrames = mtPeripheralSelected.mMTFrameHandler.getAdvFrames();
//                            Log.e("tag", "ZR: count frames " + allFrames.size());




                        }
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
