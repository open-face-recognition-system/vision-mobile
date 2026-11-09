package com.mariofronza.face_collection_app.ui.photos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mariofronza.face_collection_app.api.Photo
import com.mariofronza.face_collection_app.databinding.ActivityHandlePhotoBinding
import com.mariofronza.face_collection_app.face.FaceCameraActivity
import com.mariofronza.face_collection_app.ui.PHOTO_TYPES
import org.opencv.android.CameraBridgeViewBase

class CapturePhotoActivity : FaceCameraActivity() {

    private lateinit var binding: ActivityHandlePhotoBinding
    override val cameraView: CameraBridgeViewBase get() = binding.cameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHandlePhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val photoType = intent.getStringExtra(EXTRA_TYPE).orEmpty()
        binding.tvHint.text = "Foto: ${PHOTO_TYPES[photoType] ?: photoType}"
        binding.btnSwitchCamera.setOnClickListener { switchCamera() }
        binding.btnTakePicture.setOnClickListener {
            faceJpeg()?.let { jpeg ->
                startActivity(ConfirmPhotoActivity.intent(this, jpeg, intent.getIntExtra(EXTRA_ID, 0), photoType))
                finish()
            }
        }
        startCamera()
    }

    override fun onFacesDetected(count: Int) {
        runOnUiThread { binding.btnTakePicture.isEnabled = count == 1 }
    }

    companion object {
        private const val EXTRA_ID = "photoId"
        private const val EXTRA_TYPE = "photoType"

        fun intent(context: Context, photo: Photo): Intent =
            Intent(context, CapturePhotoActivity::class.java)
                .putExtra(EXTRA_ID, photo.id ?: 0)
                .putExtra(EXTRA_TYPE, photo.photoType)
    }
}
