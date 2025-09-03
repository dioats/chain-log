package com.example.chainlog.data

import android.content.Context
import com.example.chainlog.data.models.CallInfo
import com.example.chainlog.data.models.DeviceInfo
import com.example.chainlog.data.models.PhotoInfo

object DataCollector {
    fun getLastCalls(context: Context, limit: Int = 5): List<CallInfo> {
        val callList = mutableListOf<CallInfo>()
        val resolver = context.contentResolver

        val cursor = resolver.query(
            android.provider.CallLog.Calls.CONTENT_URI,
            null, // todas as colunas
            null,
            null,
            android.provider.CallLog.Calls.DATE + " DESC" // apenas ordenação
        )

        cursor?.use {
            val numberIndex = it.getColumnIndex(android.provider.CallLog.Calls.NUMBER)
            val typeIndex = it.getColumnIndex(android.provider.CallLog.Calls.TYPE)
            val dateIndex = it.getColumnIndex(android.provider.CallLog.Calls.DATE)
            val durationIndex = it.getColumnIndex(android.provider.CallLog.Calls.DURATION)

            var count = 0
            while (it.moveToNext() && count < limit) {
                val number = it.getString(numberIndex)
                val typeInt = it.getInt(typeIndex)
                val date = it.getLong(dateIndex)
                val duration = it.getInt(durationIndex)

                val type = when (typeInt) {
                    android.provider.CallLog.Calls.INCOMING_TYPE -> "Recebida"
                    android.provider.CallLog.Calls.OUTGOING_TYPE -> "Efetuada"
                    android.provider.CallLog.Calls.MISSED_TYPE -> "Perdida"
                    else -> "Outra"
                }

                callList.add(CallInfo(number, type, date, duration))
                count++
            }
        }

        return callList
    }

    fun getDeviceInfo(): DeviceInfo {
        val manufacturer = android.os.Build.MANUFACTURER ?: "Unknown"
        val model = android.os.Build.MODEL ?: "Unknown"
        val androidVersion = android.os.Build.VERSION.RELEASE ?: "Unknown"
        val sdkInt = android.os.Build.VERSION.SDK_INT

        return DeviceInfo(
            manufacturer = manufacturer,
            model = model,
            androidVersion = "Android $androidVersion (SDK $sdkInt)"
        )
    }

    fun getLastPhotos(context: Context, limit: Int = 5): List<PhotoInfo> {
        // TODO: Implementar coleta de fotos via MediaStore
        return emptyList()
    }
}