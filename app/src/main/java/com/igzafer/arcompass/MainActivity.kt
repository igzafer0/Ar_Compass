package com.igzafer.arcompass

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.common.util.concurrent.ListenableFuture
import com.igzafer.arcompass.databinding.ActivityMainBinding
import java.lang.Exception
import java.util.concurrent.Executor
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var cameraListener: ListenableFuture<ProcessCameraProvider>
    private lateinit var sensorManager: SensorManager
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    (this as Activity?)!!,
                    Manifest.permission.CAMERA
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    (this as Activity?)!!, arrayOf(Manifest.permission.CAMERA), 1
                )
            }
        }
        cameraListener = ProcessCameraProvider.getInstance(this)
        cameraListener.addListener({
            val x = cameraListener.get()
            startCamera(x)
        }, getExecutor())
        window.setInImmersiveMode()
        binding.mainRl.setupInsets()

    }

    private fun View.setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.layoutParams = (view.layoutParams as FrameLayout.LayoutParams).apply {
                bottomMargin = insets.bottom
            }
            WindowInsetsCompat.CONSUMED
        }

    }

    private fun Window.setInImmersiveMode() {
        val windowInsetsController = ViewCompat.getWindowInsetsController(decorView) ?: return
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        WindowCompat.setDecorFitsSystemWindows(this, false)
    }

    private fun startCamera(x: ProcessCameraProvider?) {
        x!!.unbindAll()
        val selector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        val preview = Preview.Builder().build()
        val view = findViewById<PreviewView>(R.id.preview)
        preview.setSurfaceProvider(view.surfaceProvider)
        val imageCapture =
            ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
        x.bindToLifecycle(this, selector, preview, imageCapture)


    }

    private fun getExecutor(): Executor {
        return ContextCompat.getMainExecutor(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_UI
        )

    }

    val kutuplar = listOf<String>("K", "KD", "D", "GD", "G", "GB", "B", "KB")

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(p0: SensorEvent?) {

        var degree = (p0?.values?.get(0)!!).roundToInt()
        try {

            if (degree != 359) {
                if (degree in 339..358){
                    binding.position.text = kutuplar[0]

                }else{
                    binding.position.text = kutuplar[degree/(360/8)]

                }


            }
        } catch (e: Exception) {

        }


        binding.degree.text = degree.toString()


        if (degree > 180) {
            degree -= 360

        }
        val displaymetrics = resources.displayMetrics

        if (degree > 0 && degree * 10 < 450 || degree < 0 && ((degree * 10) * -1) < 450) {

            binding.degree.translationX = ((degree * 10) * (-1).toFloat())


        } else {
            if (degree != 0) {
                binding.degree.text = if (degree < 0) ">" else "<"
            } else {
                binding.degree.text = "0"

            }

        }


    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

}