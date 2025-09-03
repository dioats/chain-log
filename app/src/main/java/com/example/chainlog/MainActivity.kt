package com.example.chainlog

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chainlog.data.DataCollector
import com.example.chainlog.firebase.FirebaseUploader
import com.example.chainlog.utils.PermissionHelper
import com.google.firebase.FirebaseApp
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var editTextOperationId: EditText
    private lateinit var btnScan: Button
    private lateinit var layoutProgress: LinearLayout
    private lateinit var textViewPhotosStatus: TextView
    private lateinit var textViewCallsStatus: TextView
    private lateinit var textViewDeviceStatus: TextView

    private lateinit var deviceId: String
    private val REQUEST_CALL_LOG = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        deviceId = UUID.randomUUID().toString()

        editTextOperationId = findViewById(R.id.edittext_operation_id)
        btnScan = findViewById(R.id.btn_scan)
        layoutProgress = findViewById(R.id.layout_progress)
        textViewPhotosStatus = findViewById(R.id.textview_photos_status)
        textViewCallsStatus = findViewById(R.id.textview_calls_status)
        textViewDeviceStatus = findViewById(R.id.textview_devices_status)

        btnScan.setOnClickListener {
            val operationId = editTextOperationId.text.toString().trim()

            if (operationId.isEmpty()) {
                Toast.makeText(this, "Digite o ID da operação", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            layoutProgress.visibility = LinearLayout.VISIBLE

            // TODO: Aqui vamos chamar DataCollector e FirebaseUploader
            textViewDeviceStatus.text = "📱 Informações do dispositivo: esperando..."
            textViewCallsStatus.text = "📞 Chamadas: esperando..."
            textViewPhotosStatus.text = "📷 Fotos: esperando..."

            collectDeviceInfo()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CALL_LOG) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                collectCalls()
            } else {
                textViewCallsStatus.text = "📞 Chamadas: permissão negada ❌"
            }
        }
    }

    private fun collectDeviceInfo() {
        Log.d("MainActivity", "[collectDeviceInfo] coletando dados do dispositivo");
        textViewDeviceStatus.text = "📱 Informações do dispositivo: coletando..."

        val operationId = editTextOperationId.text.toString().trim()
        val deviceInfo = DataCollector.getDeviceInfo()

        FirebaseUploader.initDevice(deviceId, operationId, deviceInfo) { success ->
            runOnUiThread {
                if (success) {
                    textViewDeviceStatus.text = "📱 Informações do dispositivo: concluído ✔️"
                    if (!PermissionHelper.hasPermission(this, android.Manifest.permission.READ_CALL_LOG)) {
                        PermissionHelper.requestPermission(
                            this,
                            arrayOf(android.Manifest.permission.READ_CALL_LOG),
                            REQUEST_CALL_LOG)
                    } else {
                        collectCalls()
                    }
                } else {
                    textViewDeviceStatus.text = "📱 Informações do dispositivo: erro ❌"
                    textViewCallsStatus.text = "📞 Chamadas: cancelado por erro ❌"
                    textViewPhotosStatus.text = "📷 Fotos: cancelado por erro ❌"
                }
            }
        }
    }

    private fun collectCalls() {
        Log.d("MainActivity", "[collectCalls] coletando dados das chamadas");
        textViewCallsStatus.text = "📞 Chamadas: coletando..."

        val calls = DataCollector.getLastCalls(this, 5)

        if (calls.isEmpty()) {
            textViewCallsStatus.text = "📞 Chamadas: nenhuma encontrada"
            return
        }

        // Envia para o Firebase
        FirebaseUploader.uploadCalls(deviceId, calls) { success ->
            runOnUiThread {
                textViewCallsStatus.text = if (success) {
                    "📞 Chamadas: concluído ✔️"
                } else {
                    "📞 Chamadas: erro no upload ❌"
                }
            }
        }
    }
}
