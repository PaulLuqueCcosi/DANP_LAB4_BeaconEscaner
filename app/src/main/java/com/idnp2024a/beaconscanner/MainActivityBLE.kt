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
import com.idnp2024a.beaconscanner.BeaconScanerLibrary.Utils
import com.idnp2024a.beaconscanner.permissions.BTPermissions


class MainActivityBLE : AppCompatActivity() {

    // Constante para etiquetar los logs
    private val TAG: String = "MainActivityBLE"
    // Diálogo de alerta para mostrar mensajes al usuario
    private var alertDialog: AlertDialog? = null
    // Administrador de Bluetooth
    private lateinit var bluetoothManager: BluetoothManager
    // Scanner de Bluetooth LE
    private lateinit var btScanner: BluetoothLeScanner
    // Adaptador de Bluetooth
    private lateinit var bluetoothAdapter: BluetoothAdapter
    // TextView para mostrar mensajes en la interfaz
    private lateinit var txtMessage: TextView
    // Gestor de permisos
    private val permissionManager = PermissionManager.from(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_ble)

        // Comprobar y solicitar permisos de Bluetooth
        BTPermissions(this).check()
        // Inicializar componentes de Bluetooth
        initBluetooth()

        // Obtener referencias a los botones y el TextView desde el layouts
        val btnAdversting = findViewById<Button>(R.id.btnAdversting)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)
        txtMessage = findViewById(R.id.txtMessage)


        // Crear el callback para el escaneo de BLE
        val bleScanCallback = BleScanCallback(
            onScanResultAction,
            onBatchScanResultAction,
            onScanFailedAction
        )

        // Configurar listeners para los botones
        btnAdversting.setOnClickListener {

        }

        btnStart.setOnClickListener {
            // Iniciar escaneo si la localización está habilitada, de lo contrario mostrar diálogo
            if (isLocationEnabled()) {
                bluetoothScanStart(bleScanCallback)
            } else {
                showPermissionDialog()
            }
        }

        btnStop.setOnClickListener {
            // Detener el escaneo
            bluetoothScanStop(bleScanCallback)
        }

    }

    // Inicializar componentes de Bluetooth
    fun initBluetooth() {

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter != null) {
            btScanner = bluetoothAdapter.bluetoothLeScanner
        } else {
            Log.d(TAG, "BluetoothAdapter is null")
        }
    }

    // Iniciar escaneo de BLE
    private fun bluetoothScanStart(bleScanCallback: BleScanCallback) {
        Log.d(TAG, "btScan ...1")
        if (btScanner != null) {
            Log.d(TAG, "btScan ...2")
            permissionManager
                .request(Permission.Location)
                .rationale("Bluetooth permission is needed")
                .checkPermission { isgranted ->
                    if (isgranted) {
                        Log.d(TAG, "Everything okey")
                        btScanner.startScan(bleScanCallback)
                    } else {
                        Log.d(TAG, "Alert you don't have Bluetooth permission")
                    }

                }

        } else {
            Log.d(TAG, "btScanner is null")
        }

    }

    // Comprobar si la localización está habilitada
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    // Detener el escaneo de BLE
    @SuppressLint("MissingPermission")
    private fun bluetoothScanStop(bleScanCallback: BleScanCallback) {
        Log.d(TAG, "btScan ...1")
        if (btScanner != null) {
            Log.d(TAG, "btScan ...2")
            btScanner!!.stopScan(bleScanCallback)

        } else {
            Log.d(TAG, "btScanner is null")
        }

    }

    // Acción a realizar cuando se obtiene un resultado de escaneo
    @SuppressLint("MissingPermission")
    val onScanResultAction: (ScanResult?) -> Unit = { result ->
        Log.d(TAG, "onScanResultAction ")

        val scanRecord = result?.scanRecord
        val beacon = Beacon(result?.device?.address)
        beacon.manufacturer = result?.device?.name
        beacon.rssi = result?.rssi
        //beacon.manufacturer == "ESP32 Beacon
        Log.d(TAG, "Scan: " + beacon)

        if (scanRecord != null) {
            scanRecord?.bytes?.let {
                val parserBeacon = BeaconParser.parseIBeacon(it, beacon.rssi)
                txtMessage.setText(parserBeacon.toString())
            }
        }

    }

    // Acción a realizar cuando se obtienen resultados de escaneo en lote
    val onBatchScanResultAction: (MutableList<ScanResult>?) -> Unit = {
        if (it != null) {
            Log.d(TAG, "BatchScanResult " + it.toString())
        }
    }

    // Acción a realizar cuando el escaneo falla
    val onScanFailedAction: (Int) -> Unit = {
        Log.d(TAG, "ScanFailed " + it.toString())
    }

    // Mostrar un diálogo de permiso
    private fun showPermissionDialog() {

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Alerta")
            .setMessage("El servicio de localizacion no esta activo")
            .setPositiveButton("Close") { dialog, which ->
                dialog.dismiss()
            }

        if (alertDialog == null) {
            alertDialog = builder.create()
        }

        if (!alertDialog!!.isShowing()) {
            alertDialog!!.show()
        }
    }

}