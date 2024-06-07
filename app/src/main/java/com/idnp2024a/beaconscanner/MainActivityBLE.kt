package com.idnp2024a.beaconscanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanResult
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat
import com.idnp2024a.beaconscanner.BeaconScanerLibrary.Beacon
import com.idnp2024a.beaconscanner.BeaconScanerLibrary.BeaconParser
import com.idnp2024a.beaconscanner.BeaconScanerLibrary.BleScanCallback
import com.idnp2024a.beaconscanner.permissions.BTPermissions

class MainActivityBLE : AppCompatActivity() {

    private val TAG: String = "MainActivityBLE"
    private var alertDialog: AlertDialog? = null
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var btScanner: BluetoothLeScanner
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var txtMessage: TextView
    private val permissionManager = PermissionManager.from(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_ble)
        initUI()
        BTPermissions(this).check()
        initBluetooth()
    }

    private fun initUI() {
        val btnAdvertising = findViewById<Button>(R.id.btnAdversting)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)
        txtMessage = findViewById(R.id.txtMessage)

        val scanCallBack  = createBleScanCallback();
        btnAdvertising.setOnClickListener { handleAdvertisingClick() }
        btnStart.setOnClickListener { handleStartClick(scanCallBack) }
        btnStop.setOnClickListener { handleStopClick(scanCallBack) }
    }

    private fun handleAdvertisingClick() {
        Log.i(TAG, "Press start advertising button");

    }

    private fun handleStartClick(bleScanCallback: BleScanCallback) {
        Log.i(TAG, "Press start scan button");
        if (!isLocationEnabled() || !isBluetoothEnabled()) {
            showPermissionDialog("Servicios no activados", "La localizacion y el Bluetooth tienen que estar activos");
            return
        }

        bluetoothScanStart(bleScanCallback)

    }

    private fun isLocationEnabled(): Boolean {
        Log.i(TAG, "Verificando localizacion activo");
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager;
        Log.d(TAG, "Localizacion activado: "+ LocationManagerCompat.isLocationEnabled(locationManager));
        return LocationManagerCompat.isLocationEnabled(locationManager);
    }

    private fun isBluetoothEnabled(): Boolean {
        Log.i(TAG, "Verificando Bluetooth activo");
        Log.d(TAG, "Bluetooth activado: "+ bluetoothAdapter.isEnabled);
        return bluetoothAdapter.isEnabled;
    }

    private fun handleStopClick(bleScanCallback: BleScanCallback) {
        Log.i(TAG, "Press stop scan button");
        bluetoothScanStop(bleScanCallback)
    }

    private fun initBluetooth() {
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter != null) {
            btScanner = bluetoothAdapter.bluetoothLeScanner
        } else {
            Log.d(TAG, "BluetoothAdapter is null")
        }
    }

    private fun bluetoothScanStart(bleScanCallback: BleScanCallback) {
        Log.d(TAG, "Starting Bluetooth scan...")
        if (btScanner != null) {
            permissionManager
                .request(Permission.Location, Permission.Bluetooth)
                .rationale("Bluetooth permission is needed")
                .checkPermission { isGranted ->
                    if (isGranted) {
                        Log.d(TAG, "Permissions granted, starting scan.")
                        btScanner.startScan(bleScanCallback)
                    } else {
                        Log.d(TAG, "Bluetooth permission not granted.")
                    }
                }
        } else {
            Log.d(TAG, "BluetoothLeScanner is null")
        }
    }

    @SuppressLint("MissingPermission")
    private fun bluetoothScanStop(bleScanCallback: BleScanCallback) {
        Log.d(TAG, "Stopping Bluetooth scan...")
        if (btScanner != null) {
            btScanner.stopScan(bleScanCallback)
        } else {
            Log.d(TAG, "BluetoothLeScanner is null")
        }
    }


    private fun showPermissionDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }

        if (alertDialog == null) {
            alertDialog = builder.create()
        }

        if (!alertDialog!!.isShowing) {
            alertDialog!!.show()
        }
    }

    private fun createBleScanCallback(): BleScanCallback {
        return BleScanCallback(
            onScanResultAction,
            onBatchScanResultAction,
            onScanFailedAction
        )
    }

    private val onScanResultAction: (ScanResult?) -> Unit = { result ->
        Log.d(TAG, "onScanResultAction")

        val scanRecord = result?.scanRecord
        val beacon = Beacon(result?.device?.address).apply {
            manufacturer = result?.device?.name
            rssi = result?.rssi
        }
        Log.d(TAG, "Scan: $beacon")

        scanRecord?.bytes?.let {
            val parsedBeacon = BeaconParser.parseIBeacon(it, beacon.rssi)
            txtMessage.text = parsedBeacon.toString()
        }
    }

    private val onBatchScanResultAction: (MutableList<ScanResult>?) -> Unit = {
        Log.d(TAG, "BatchScanResult: ${it.toString()}")
    }

    private val onScanFailedAction: (Int) -> Unit = {
        Log.d(TAG, "ScanFailed: $it")
    }
}
