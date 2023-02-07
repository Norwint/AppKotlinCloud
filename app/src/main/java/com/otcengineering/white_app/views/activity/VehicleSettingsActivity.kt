package com.otcengineering.white_app.views.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaRecorder
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.otc.alice.api.model.Shared
import com.otc.alice.api.model.Vehicle
import com.otcengineering.white_app.R
import com.otcengineering.white_app.activities.EditBasicProfileActivity
import com.otcengineering.white_app.databinding.ActivityVehicleSettingsBinding
import com.otcengineering.white_app.network.utils.ApiCaller
import com.otcengineering.white_app.utils.*
import com.otcengineering.white_app.viewModel.Network
import com.otcengineering.white_app.viewModel.OtcCallback

class VehicleSettingsActivity : AppCompatActivity() {

    private val binding: ActivityVehicleSettingsBinding by lazy { ActivityVehicleSettingsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val id = intent.getLongExtra(NewConstants.EXTRA_VEH_ID, 0L)

        binding.vehicleImage.setOnClickListener(View.OnClickListener { v: View? -> showPictureDialog() })

        binding.btnSave.setOnClickListener(View.OnClickListener { v: View? ->
            val newName = binding.etNickName.text.toString()
            Common.sharedPreferences.putString(Preferences.vehicleName, newName)
        })

//        binding.btnUnlink.setOnClickListener{
//            val vin = "00000000000L"
//            val body = com.otc.alice.api.model.Vehicle.VehicleLink.newBuilder()
//                .setVin(vin)
//                .build()
//
//            Network.vehicle.unlinkVehicle(body).enqueue(object: OtcCallback<Vehicle.VehicleData>(
//                com.otc.alice.api.model.Vehicle.VehicleData::class.java) {
//                override fun response(response: com.otc.alice.api.model.Vehicle.VehicleData) {
//                    Log.d("alert", "Linked")
//                }
//
//                override fun error(status: Shared.OTCStatus) {
//                    Log.d("alert", "Not Linked: ${status.name}")
//                }
//            })
//        }

    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle(R.string.select_action)
        val pictureDialogItems = arrayOf(
            getString(R.string.select_from_gallery),
            getString(R.string.capture_from_camera)
        )
        pictureDialog.setItems(
            pictureDialogItems
        ) { dialog: DialogInterface?, which: Int ->
            when (which) {
                0 -> choosePhotoFromGallary()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, MediaRecorder.VideoSource.CAMERA)
    }

    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (galleryIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(galleryIntent, EditBasicProfileActivity.PICK_IMAGE)
        } else {
            val filesIntent =
                Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(filesIntent, EditBasicProfileActivity.PICK_IMAGE)
        }
    }

    companion object {
        fun newInstance(ctx: Context, vehicleID: Long) {
            val intent = Intent(ctx, VehicleSettingsActivity::class.java)
            intent.putExtra(NewConstants.EXTRA_VEH_ID, vehicleID)
            ctx.startActivity(intent)
        }

    }

}