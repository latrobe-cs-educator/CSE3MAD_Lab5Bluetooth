package com.example.lab5bluetooth2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //Member Variables
    private Spinner resultsSpinner;
    private Button scanBtn;
    private BluetoothAdapter BA;
    private BroadcastReceiver BTrx;
    private ArrayAdapter<String> adapter;

    //for debugging
    private String TAG = "MainAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialise bluetooth Adapter
        BA = BluetoothAdapter.getDefaultAdapter();

        //Spinner
        resultsSpinner = (Spinner) findViewById(R.id.spinner);
        //prepare arraylist
        ArrayList list = new ArrayList();
        //set arrayList values to spinner
        adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, list);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        resultsSpinner.setAdapter(adapter);

        //Button
        scanBtn = (Button) findViewById(R.id.scanBtn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Permission already granted, initiating scan");
                BA.startDiscovery();
            }
        });

        //Get Permission to use bluetooth
        if (!hasPermission()) {
            scanBtn.setEnabled(false); //Deactivate button if permission not granted
            //ask permissions
            Log.d(TAG, "Asking Real time permission");
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        //Filter & Register RX
        IntentFilter filter = new IntentFilter();
        //receive system bluetooth broadcast
        filter.addAction("android.bluetooth.device.action.FOUND");
        BTrx = new MyBluetoothReceiver();
        registerReceiver(BTrx, filter);
    }

    private boolean hasPermission() {
        //return the permission
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    scanBtn.setEnabled(true);
                } else {
                    // Send a toast message to the user telling them they will not be able to use the app without allowing fine location permission.
                    // There are multiple ways to do this, view the textbook for more information https://ebookcentral-proquest-com.ez.library.latrobe.edu.au/lib/latrobe/reader.action?docID=6642493&ppg=631
                    Toast.makeText(this, "Essential permission not granted please restart the app to try again",Toast.LENGTH_SHORT).show();
                }
            });

    //Detect Bluetooth state change and handle switch
    public class MyBluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Finding devices
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "Found: " + device.getName() + " " + device.getAddress());
                //add found devices to array adapter so we can see them in the spinner
                adapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    //Unregister the broadcast receiver when it is no longer needed:
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(BTrx);
    }

}