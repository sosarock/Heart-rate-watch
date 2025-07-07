package com.sosaomar.myhearth.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.sosaomar.myhearth.HeartRateService
import com.sosaomar.myhearth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val heartRateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.sosaomar.myhearth.HEART_RATE_UPDATE") {
                val bpm = intent.getIntExtra("bpm", -1)
                if (bpm != -1) {
                    runOnUiThread {
                        binding.tvHeartRate.text = "Pulso: $bpm"
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            if (checkSelfPermission(Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
                startHeartRateService()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
            }

        }

    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        registerReceiver(heartRateReceiver, IntentFilter("com.sosaomar.myhearth.HEART_RATE_UPDATE"))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(heartRateReceiver)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startHeartRateService()
        } else {
            Toast.makeText(this, "Permiso BODY_SENSORS denegado", Toast.LENGTH_SHORT).show()
        }
    }


    private fun startHeartRateService() {
//            startService(Intent(this@MainActivity, HeartRateService::class.java))
        startForegroundService(Intent(this, HeartRateService::class.java))
    }
}