package com.example.chainlog

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var editTextOperationId: EditText
    private lateinit var btnScan: Button
    private lateinit var layoutProgress: LinearLayout
    private lateinit var textViewPhotosStatus: TextView
    private lateinit var textViewCallsStatus: TextView
    private lateinit var textViewDeviceStatus: TextView

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
            textViewPhotosStatus.text = "📷 Fotos: coletando..."
            textViewCallsStatus.text = "📞 Chamadas: esperando..."
            textViewDeviceStatus.text = "📱 Informações do dispositivo: esperando..."
        }
    }
}