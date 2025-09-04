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
    private val REQUEST_MEDIA_IMAGES = 1002

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

        if(requestCode == REQUEST_MEDIA_IMAGES) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                collectPhotos()
            } else {
                textViewPhotosStatus.text = "📷 Fotos: permissão negada ❌"
            }
        }
    }

    private fun collectDeviceInfo() {
        Log.d("MainActivity", "[collectDeviceInfo] coletando dados do dispositivo");
        textViewDeviceStatus.text = "📱 Informações do dispositivo: coletando..."

        val operationId = editTextOperationId.text.toString().trim()
        val deviceInfo = DataCollector.getDeviceInfo()

        FirebaseUploader.initDevice(deviceId, operationId, deviceInfo) { success ->
            if (success) {
                textViewDeviceStatus.text = "📱 Informações do dispositivo: concluído ✔️"
                collectCalls()
                collectPhotos()
            } else {
                textViewDeviceStatus.text = "📱 Informações do dispositivo: cancelado por erro ❌"
                textViewCallsStatus.text = "📞 Chamadas: cancelado por erro ❌"
                textViewPhotosStatus.text = "📷 Fotos: cancelado por erro ❌"
            }
        }
    }

    private fun collectCalls() {
        Log.d("MainActivity", "[collectCalls] coletando dados das chamadas");
        textViewCallsStatus.text = "📞 Chamadas: coletando..."

        if (!PermissionHelper.hasPermission(this, android.Manifest.permission.READ_CALL_LOG)) {
            PermissionHelper.requestPermission(
                this,
                arrayOf(android.Manifest.permission.READ_CALL_LOG),
                REQUEST_CALL_LOG)
            return
        }

        val calls = DataCollector.getLastCalls(this, 5)

        if (calls.isEmpty()) {
            textViewCallsStatus.text = "📞 Chamadas: nenhuma encontrada"
            return
        }

        FirebaseUploader.uploadCalls(deviceId, calls) { success ->
            if(success) {
                textViewCallsStatus.text = "📞 Chamadas: concluído ✔️"
            } else {
                textViewCallsStatus.text = "📞 Chamadas: erro no upload ❌"
            }
        }
    }

    private fun collectPhotos() {
        Log.d("MainActivity", "[collectPhotos] coletando dados das imagens");
        textViewPhotosStatus.text = "📷 Fotos: coletando..."

        if (!PermissionHelper.hasPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)) {
            PermissionHelper.requestPermission(
                this,
                arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_MEDIA_IMAGES)
            return
        }

        val photos = DataCollector.getLastPhotos(this, 5)

        if (photos.isEmpty()) {
            textViewPhotosStatus.text = "📷 Fotos: nenhuma encontrada"
            return
        }

        FirebaseUploader.uploadPhotos(deviceId, photos) { success ->
            if(success) {
                textViewPhotosStatus.text = "📷 Fotos: concluído ✔️"
            } else {
                textViewPhotosStatus.text = "📷 Fotos: erro no upload ❌"
            }
        }
    }
}
