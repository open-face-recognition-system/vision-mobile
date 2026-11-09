package com.mariofronza.face_collection_app.ui.photos

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mariofronza.face_collection_app.api.safeCall
import com.mariofronza.face_collection_app.databinding.ActivityConfirmPhotoBinding
import com.mariofronza.face_collection_app.ui.handle
import com.mariofronza.face_collection_app.ui.toast
import com.mariofronza.face_collection_app.ui.visible
import com.mariofronza.face_collection_app.vision
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ConfirmPhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmPhotoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val jpeg = intent.getByteArrayExtra(EXTRA_JPEG) ?: return finish()
        val photoId = intent.getIntExtra(EXTRA_ID, 0)
        val photoType = intent.getStringExtra(EXTRA_TYPE).orEmpty()

        binding.ivPhoto.setImageBitmap(BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size))
        binding.btnRetake.setOnClickListener {
            startActivity(Intent(this, CapturePhotoActivity::class.java).putExtras(intent))
            finish()
        }
        binding.btnConfirm.setOnClickListener { upload(jpeg, photoId, photoType) }
    }

    private fun upload(jpeg: ByteArray, photoId: Int, photoType: String) {
        setLoading(true)
        val file = MultipartBody.Part.createFormData("file", "face.jpg", jpeg.toRequestBody("image/jpeg".toMediaType()))
        lifecycleScope.launch {
            safeCall { if (photoId == 0) vision.api.createPhoto(photoType, file) else vision.api.replacePhoto(photoId, file) }
                .onSuccess {
                    toast("Foto salva")
                    finish()
                }
                .onFailure { handle(it) }
            setLoading(false)
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnConfirm.isEnabled = !loading
        binding.btnRetake.isEnabled = !loading
        binding.progress.visible(loading)
    }

    companion object {
        private const val EXTRA_JPEG = "jpeg"
        private const val EXTRA_ID = "photoId"
        private const val EXTRA_TYPE = "photoType"

        fun intent(context: Context, jpeg: ByteArray, photoId: Int, photoType: String): Intent =
            Intent(context, ConfirmPhotoActivity::class.java)
                .putExtra(EXTRA_JPEG, jpeg)
                .putExtra(EXTRA_ID, photoId)
                .putExtra(EXTRA_TYPE, photoType)
    }
}
