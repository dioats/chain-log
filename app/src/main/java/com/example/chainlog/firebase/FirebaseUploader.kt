package com.example.chainlog.firebase

import android.util.Log
import com.example.chainlog.data.models.CallInfo
import com.example.chainlog.data.models.DeviceInfo
import com.example.chainlog.data.models.PhotoInfo
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUploader {

    private val db = FirebaseFirestore.getInstance()

    fun initDevice(deviceId: String, operationId: String, device: DeviceInfo, onComplete: (Boolean) -> Unit) {

        Log.d("FirebaseUploader", "[initDevice] salvando dispositivo $deviceId")

        val deviceData = mapOf(
            "operationId" to operationId,
            "manufacturer" to device.manufacturer,
            "model" to device.model,
            "androidVersion" to device.androidVersion
        )

        db.collection("devices")
            .document(deviceId)
            .set(deviceData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun uploadCalls(deviceId: String, calls: List<CallInfo>, onComplete: (Boolean) -> Unit) {
        val batch = db.batch()
        val callsRef = db.collection("devices").document(deviceId).collection("calls")

        calls.forEach { call ->
            val doc = callsRef.document()
            val callData = mapOf(
                "number" to call.number,
                "type" to call.type,
                "date" to call.date,
                "duration" to call.duration
            )
            batch.set(doc, callData)
        }

        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun uploadPhotos(deviceId: String, photos: List<PhotoInfo>, onComplete: (Boolean) -> Unit) {
        val batch = db.batch()
        val photosRef = db.collection("devices").document(deviceId).collection("photos")

        photos.forEach { photo ->
            val doc = photosRef.document()
            val photoData = mapOf(
                "uri" to photo.uri,
                "dateTaken" to photo.dateTaken
            )
            batch.set(doc, photoData)
        }

        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}