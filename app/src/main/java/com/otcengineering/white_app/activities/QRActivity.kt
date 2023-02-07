package com.otcengineering.white_app.activities

import android.Manifest
import com.otcengineering.white_app.activities.BaseActivity
import android.view.SurfaceView
import android.os.Bundle
import com.otcengineering.white_app.R
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.barcode.Barcode
import android.util.DisplayMetrics
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import android.util.SparseArray
import android.content.Intent
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import android.view.SurfaceHolder
import androidx.core.app.ActivityCompat
import com.otcengineering.apible.Crypt
import com.otcengineering.white_app.databinding.ActivityQrBinding
import com.otcengineering.white_app.databinding.ActivityVerificationBinding
import com.otcengineering.white_app.utils.Common
import com.otcengineering.white_app.utils.Preferences
import com.otcengineering.white_app.views.components.NewVehicleDialog
import java.io.IOException
import java.util.regex.Pattern

class QRActivity : BaseActivity("QR Activity") {
    private var mSurfaceView: SurfaceView? = null
    private var pattern: String? = null
    private var encrypted = false

    private val binding: ActivityQrBinding by lazy { ActivityQrBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        pattern = intent.getStringExtra("QR_PATTERN")
        encrypted = intent.getBooleanExtra("QR_ENCRYPTED", false)
        mSurfaceView = findViewById(R.id.surfaceView)
        startViews()

        binding.addButton.setOnClickListener {
            val dialog = NewVehicleDialog(this)

            dialog.callback = { vin ->
                if (Pattern.matches(pattern ?: ".", vin)) {
                    val intent = Intent()
                    intent.putExtra("QR_RESULT", vin)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }

            dialog.showOnMainThread()
        }

    }

    private fun startViews() {
        val detector = BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build()
        val metrics = resources.displayMetrics
        val ratio = metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
        val src = CameraSource.Builder(this, detector)
            .setRequestedPreviewSize((ratio * 1000).toInt(), 1000).setAutoFocusEnabled(true).build()
        detector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}
            override fun receiveDetections(detections: Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() != 0) {
                    val msg = if (encrypted) {
                        val code = barcodes.valueAt(0).displayValue
                        val split = code.split("\\?").toTypedArray()
                        val iv = Base64.decode(split[1], Base64.DEFAULT)
                        val data = Base64.decode(split[0], Base64.DEFAULT)
                        val decrypt = Crypt.decrypt(data, iv)
                        String(decrypt)
                    } else {
                        barcodes.valueAt(0).displayValue
                    }
                    if (Pattern.matches(pattern ?: ".", msg)) {
                        detector.release()
                        val intent = Intent()
                        intent.putExtra("QR_RESULT", msg)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
            }
        })
        mSurfaceView!!.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            this@QRActivity,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    src.start(mSurfaceView!!.holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                src.stop()
            }
        })
    }

    companion object {
        const val QR_RESULT = 690
    }
}