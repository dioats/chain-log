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
import com.example.chainlog.utils.PermissionHelper

class MainActivity : AppCompatActivity() {

    private lateinit var editTextOperationId: EditText
    private lateinit var btnScan: Button
    private lateinit var layoutProgress: LinearLayout
    private lateinit var textViewPhotosStatus: TextView
    private lateinit var textViewCallsStatus: TextView
    private lateinit var textViewDeviceStatus: TextView
    private val REQUEST_CALL_LOG = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            textViewCallsStatus.text = "📞 Chamadas: esperando..."
            textViewPhotosStatus.text = "📷 Fotos: esperando..."
            textViewDeviceStatus.text = "📱 Informações do dispositivo: esperando..."

            if (!PermissionHelper.hasPermission(this, android.Manifest.permission.READ_CALL_LOG)) {
                PermissionHelper.requestPermission(this, arrayOf(android.Manifest.permission.READ_CALL_LOG), REQUEST_CALL_LOG)
            } else {
                collectCalls()
            }


        }
    }

    private fun collectCalls() {
        Log.d("MainActivity", "[collectCalls] coletando dados das chamadas");
        textViewCallsStatus.text = "📞 Chamadas: coletando..."
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
}