package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

public class BluetoothActivity extends AppCompatActivity {
    ListView usersListView;
    ArrayList<String> users = new ArrayList<>();
    ArrayList<String> addresses = new ArrayList<>();
    ArrayAdapter<String> adapter;
    ProgressBar progressBar;
    BluetoothAdapter bluetoothAdapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Linking Main Activity to the main_menu activity
        getMenuInflater().inflate(R.menu.bluetooth_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.enableBluetoothMenuItem == item.getItemId()) {

            //if bluetooth is disabled then show the bluetooth enabling dialog
            if(!bluetoothAdapter.isEnabled()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
            else{
                bluetoothAdapter.startDiscovery();
            }
            return true;
        }
        return false;
    }

    //Listen to the bluetooth adapter's actions
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        // A callback which holds the chunk of code that decides what will happen next(currently rendering the info on ListView)
        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                progressBar.setVisibility(View.VISIBLE);
                Toast.makeText(context, "Searching devices...", Toast.LENGTH_SHORT).show();
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                progressBar.setVisibility(View.INVISIBLE);

                if(users.isEmpty()) {
                    Toast.makeText(context, "No devices found :(", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(context, "Found nearby devices :)", Toast.LENGTH_SHORT).show();
                }
            }
            else if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = bluetoothDevice.getName();
                String address = bluetoothDevice.getAddress();
                String userInfo = "";

                //Get unique list of users
                if(!addresses.contains(address))
                {
                    addresses.add(address);
                    if(name != null && !name.equals("")){
                        userInfo = name;
                    }else {
                        userInfo = address;
                    }

                    users.add(userInfo);
                    adapter.notifyDataSetChanged();

                    usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                //Go to Chat Activity
                        }
                    });
                }
            }
        }
    };

    public void configureBluetooth(){

        //Accessing bluetooth service from the system services
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        //Getting access to the bluetooth adapter from bluetooth hardware
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter == null){
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        else {
            //Setting up intent filter for storing actions of the bluetooth adapter
            IntentFilter intentFilter = new IntentFilter();

            //Action occurs bluetooth is on or off
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

            //Action occurs when bluetooth adapter has started finding other bluetooth devices in vicinity
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);

            //Action occurs when a bluetooth device is found
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);

            //Action occurs when all nearby bluetooth devices have been discovered
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

            //Registering the actions in the broadcastReceiver to decide what next should happen
            registerReceiver(broadcastReceiver, intentFilter);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK){
            bluetoothAdapter.startDiscovery();
        }
        else{
            Toast.makeText(this, "Access Denied!", Toast.LENGTH_SHORT).show();
        }
    }

    //Evaluating the user's permission regarding bluetooth access
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){

            //Checking for user's response to the asked permission
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                //Checking for location permission
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    configureBluetooth();
                }
            }
            else{
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();

            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        users.clear();
        addresses.clear();
        
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.primary_dark), PorterDuff.Mode.MULTIPLY);

        //Linking Main Activity to the ListView Layout
        usersListView = findViewById(R.id.usersListView);

        //Creating Array Adapter responsible for ListView manipulation
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, users);

        //Establishing connection between ListView and Array Adapter
        usersListView.setAdapter(adapter);

        //If we have the user's permission we commence bluetooth configuration else we ask for permission through dialog
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            configureBluetooth();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }
}















