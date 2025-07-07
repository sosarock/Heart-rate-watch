package com.sosaomar.myhearth

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.Wearable

class HeartRateService : Service(), SensorEventListener {

    private val TAG = "HeartRateService"
    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        startForeground(1, createNotification())
        heartRateSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        if (heartRateSensor == null) {
            Log.e(TAG, "Sensor de pulso no disponible.")
        }

        Log.d(TAG, "onCreate ejecutado")

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            val bpm = event.values[0].toInt()
            Log.d(TAG, "Pulso: $bpm")
            sendToPhone(bpm)
            updateUIOnWatch(bpm)
        }
        Log.d(TAG, "onSensorChanged recibido: ${event?.values?.joinToString()}")

    }

    private fun updateUIOnWatch(bpm: Int) {
        val intent = Intent("com.sosaomar.myhearth.HEART_RATE_UPDATE")
        intent.putExtra("bpm", bpm)
        sendBroadcast(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun sendToPhone(bpm: Int) {
        val nodeClient = Wearable.getNodeClient(this)
        val messageClient = Wearable.getMessageClient(this)

        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                messageClient.sendMessage(
                    node.id,
                    "/heart_rate",
                    bpm.toString().toByteArray()
                ).addOnSuccessListener {
                    Log.d(TAG, "Enviado al teléfono: $bpm bpm")
                }.addOnFailureListener {
                    Log.e(TAG, "Error al enviar: ${it.message}")
                }
            }
        }
    }

    private fun createNotification(): Notification {
        val channelId = "heart_rate_channel"
        val channel =
            NotificationChannel(channelId, "Heart Rate", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Monitoreando pulso")
            .setSmallIcon(R.drawable.splash_icon)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
