package com.example.checkbluetooth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;


import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.service.controls.ControlsProviderService.TAG;


public class MainActivity extends AppCompatActivity {

    public static final int RequestPermissionCode = 1;
    private static final String NAME = "checkBluetooth";

    private final UUID MY_UUID=UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private Button server;
    private Button mScanBtn;
    private Button mOffBtn;
    private Button mListPairedDevicesBtn;
    private Button mDiscoverBtn;
    private ListView mDevicesListView;
    private TextView status;
    private TextView connectedTo;

    private static final int REQUEST_ENABLE_CODE = 1;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;


    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;


    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_DISCONNECTED=5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

          server = (Button) findViewById(R.id.server);
          mScanBtn = (Button) findViewById(R.id.scan);
          mOffBtn = (Button) findViewById(R.id.off);
          mDiscoverBtn = (Button) findViewById(R.id.discover);
          mListPairedDevicesBtn = (Button) findViewById(R.id.paired_btn);
          status=(TextView)findViewById(R.id.samplText);
          connectedTo = (TextView) findViewById(R.id.connected_Device);


          bluetoothManager = getSystemService(BluetoothManager.class);
          bluetoothAdapter = bluetoothManager.getAdapter();

        mBTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        mDevicesListView = (ListView) findViewById(R.id.devices_list_view);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);


        if(checkPermission()){
            Toast.makeText(MainActivity.this, "All Permissions Granted Successfully", Toast.LENGTH_LONG).show();

        }
        else {
            requestPermission();
        }
        bluetoothOn();


        server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AcceptThread acceptThread=new AcceptThread();
                acceptThread.start();
            }
        });

        mScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothOn();
            }
        });

        mOffBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                bluetoothOff();
            }
        });

        mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                listPairedDevices();
            }
        });

        mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                discover();
            }
        });



    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                        BLUETOOTH_ADMIN,BLUETOOTH,
                        BLUETOOTH_CONNECT,BLUETOOTH_SCAN,ACCESS_WIFI_STATE,
                        ACCESS_FINE_LOCATION,ACCESS_NETWORK_STATE,
                        ACCESS_COARSE_LOCATION,}, RequestPermissionCode);

    }


    private boolean checkPermission() {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(),BLUETOOTH);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(),BLUETOOTH_CONNECT);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), BLUETOOTH_ADMIN);
        int fourthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), BLUETOOTH_SCAN);
        int fifthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_NETWORK_STATE);
        int sixthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_WIFI_STATE);
        int seventhPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int eightPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(),ACCESS_COARSE_LOCATION);



        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED &&
                fourthPermissionResult==PackageManager.PERMISSION_GRANTED &&
                fifthPermissionResult==PackageManager.PERMISSION_GRANTED &&
                sixthPermissionResult==PackageManager.PERMISSION_GRANTED &&
                seventhPermissionResult==PackageManager.PERMISSION_GRANTED &&
                eightPermissionResult==PackageManager.PERMISSION_GRANTED ;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {

                    boolean   bluetooth = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean   bluetoothConnect = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean   bluetoothAdmin = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean   bluetoothScan = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean   networkState= grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean   wifiState = grantResults[5] == PackageManager.PERMISSION_GRANTED;
                    boolean   fineLocation = grantResults[6] == PackageManager.PERMISSION_GRANTED;
                    boolean   coarseLocation=grantResults[7] == PackageManager.PERMISSION_GRANTED;

                    if (bluetooth && bluetoothConnect && bluetoothAdmin && bluetoothScan
                            &&networkState && wifiState && fineLocation && coarseLocation ) {

                        Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();

                    }
                }

                break;
        }

    }


    private void bluetoothOn(){
        if (bluetoothAdapter!=null &&!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
             status.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }

    private void bluetoothOff(){
         bluetoothAdapter.disable(); // turn off
         status.setText("Bluetooth disabled");
        Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }


    //The BroadcastReceiver that listens for bluetooth broadcast
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                if (device != null && device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    mBTArrayAdapter.notifyDataSetChanged();
                }

            }

            }

    };



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, Data);
        if (requestCode ==REQUEST_ENABLE_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                status.setText("Enabled");
            } else
                status.setText("Disabled");
        }
    }


    private void listPairedDevices(){
        mBTArrayAdapter.clear();
        mPairedDevices = bluetoothAdapter.getBondedDevices();
        if(bluetoothAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }
    
    private void discover(){
        // Check if the device is already discovering
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(bluetoothAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                bluetoothAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) view).getText().toString();
            final String mac = info.substring(info.length() - 17);
            final String name = info.substring(0, info.length() - 17);

            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(mac);
            if (device != null && device.getBondState() != BluetoothDevice.BOND_BONDED) {
                device.createBond();
            }
            else{
                ConnectThread clientClass = new ConnectThread(device);
                clientClass.start();
            }

        }
    };

    Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what)
            {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Connection Failed");
                    break;
                case STATE_DISCONNECTED:
                    status.setText("DISCONNECTED");
                    break;
            }
            return true;
        }
    });


    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                 e.printStackTrace();
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                     e.printStackTrace();
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    //manageMyConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                 e.printStackTrace();
            }
        }
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                 e.printStackTrace();
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                connectedTo.setText(mmDevice.getName()+""+mmDevice.getAddress());
                Message message=Message.obtain();
                message.what=STATE_CONNECTED;
                mHandler.sendMessage(message);


            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                 connectException.printStackTrace();
                try {
                    mmSocket.close();
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    mHandler.sendMessage(message);
                } catch (IOException closeException) {
                     closeException.printStackTrace();
                }

            }



            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.

           // manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                 e.printStackTrace();
            }
        }
    }

 }






