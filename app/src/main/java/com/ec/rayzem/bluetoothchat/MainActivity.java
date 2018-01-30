package com.ec.rayzem.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import co.lujun.lmbluetoothsdk.BluetoothController;
import co.lujun.lmbluetoothsdk.BluetoothLEController;
import co.lujun.lmbluetoothsdk.base.BluetoothLEListener;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "***BLUETOOTH CHAT***";

    private TextView connectionState, chatContent;
    private Button btnScan, btnDisconnect, btnReconnect, btnSend;
    private EditText msnToSend;
    private ListView listDevicesAvalaibles;
    private List<String> devicesName;
    private BaseAdapter adapter;

    private BluetoothLEController bluetoothLEController;

    private BluetoothLEListener bluetoothLEListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothLEController.release();
    }

    private void initComponents() {
        bluetoothLEController = BluetoothLEController.getInstance().build(this);
        createListener();
        //set listener to the controller
        bluetoothLEController.setBluetoothListener(bluetoothLEListener);
        devicesName = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devicesName);

        listDevicesAvalaibles = findViewById(R.id.listBLEDevices);
        btnScan = findViewById(R.id.scanBLE);
        btnDisconnect = findViewById(R.id.disconnectBLE);
        btnReconnect = findViewById(R.id.reonnectBLE);
        btnSend = findViewById(R.id.sendBLE);
        connectionState = findViewById(R.id.stateConnectionBLE);
        chatContent = findViewById(R.id.chatContentBLE);
        msnToSend = findViewById(R.id.contentToSendBLE);

        listDevicesAvalaibles.setAdapter(adapter);

        bluetoothLEController.setReadCharacteristic("READING BLUETOOTH");
        bluetoothLEController.setWriteCharacteristic("WRITING BLUETOOTH");

        //Set listeners in widgets
        setListenerOnWidgets();

    }

    private void setListenerOnWidgets(){

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!bluetoothLEController.isSupportBLE()) {
                    Toast.makeText(MainActivity.this, "BLE bluetooth is not supported.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                devicesName.clear();
                adapter.notifyDataSetChanged();

                if(bluetoothLEController.startScan()){
                    Toast.makeText(MainActivity.this, "Scanning!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothLEController.disconnect();
            }
        });

        btnReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothLEController.disconnect();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msn = msnToSend.getText().toString();
                if(!TextUtils.isEmpty(msn))
                    bluetoothLEController.write(msn.getBytes());
            }
        });

        listDevicesAvalaibles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = devicesName.get(position);
                bluetoothLEController.connect(item.substring(item.length() - 17));
            }
        });
    }

    private void createListener () {
        bluetoothLEListener = new BluetoothLEListener() {
            @Override
            public void onReadData(final BluetoothGattCharacteristic characteristic) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatContent.append("FROM "+characteristic.getUuid()+": "+characteristic.getStringValue(0)+"\n");
                    }
                });

            }

            @Override
            public void onWriteData(final BluetoothGattCharacteristic characteristic) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatContent.append("ME: "+characteristic.getStringValue(0)+"\n");
                    }
                });
            }

            @Override
            public void onDataChanged(BluetoothGattCharacteristic characteristic) {

                final String dataValue = characteristic.getStringValue(0);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatContent.append( dataValue );
                    }
                });
            }

            @Override
            public void onDiscoveringCharacteristics(final List<BluetoothGattCharacteristic> characteristics) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for(BluetoothGattCharacteristic characteristic : characteristics){
                            Log.d(TAG,"onDiscoveringCharacteristics - Characteristic: "+characteristic.getUuid());
                        }
                    }
                });
            }

            @Override
            public void onDiscoveringServices(final List<BluetoothGattService> services) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for(BluetoothGattService service: services){
                            Log.d(TAG,"Services - service: "+service.getUuid());

                        }
                    }
                });

            }

            @Override
            public void onActionStateChanged(int preState, int state) {
                Log.d(TAG, "Action State Changed: "+state);
            }

            @Override
            public void onActionDiscoveryStateChanged(String discoveryState) {
                if(discoveryState.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
                    Toast.makeText(MainActivity.this, "Scanning..", Toast.LENGTH_SHORT).show();
                else if(discoveryState.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
                    Toast.makeText(MainActivity.this, "Scan finished.", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onActionScanModeChanged(int preScanMode, int scanMode) {
                Log.d(TAG, "Action Scan Mode Changed: "+scanMode);
            }

            @Override
            public void onBluetoothServiceStateChanged(int state) {
                final int status = state;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectionState.setText("STATUS: "+status);
                    }
                });
            }

            @Override
            public void onActionDeviceFound(final BluetoothDevice device, short rssi) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        devicesName.add(device.getName() +"@" +device.getAddress());
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        };
    }
}


