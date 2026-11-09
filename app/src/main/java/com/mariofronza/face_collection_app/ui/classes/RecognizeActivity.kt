package com.mariofronza.face_collection_app.ui.classes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.mariofronza.face_collection_app.api.ApiError
import com.mariofronza.face_collection_app.api.Lecture
import com.mariofronza.face_collection_app.api.PresenceRequest
import com.mariofronza.face_collection_app.api.Recognition
import com.mariofronza.face_collection_app.api.safeCall
import com.mariofronza.face_collection_app.databinding.ActivityRealtimeRecognizeStudentBinding
import com.mariofronza.face_collection_app.face.FaceCameraActivity
import com.mariofronza.face_collection_app.ui.handle
import com.mariofronza.face_collection_app.ui.toast
import com.mariofronza.face_collection_app.ui.visible
import com.mariofronza.face_collection_app.vision
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.opencv.android.CameraBridgeViewBase

/**
 * Recognizes one face at a time: a new attempt starts only after the previous face left the frame,
 * so the same student is not sent over and over.
 */
class RecognizeActivity : FaceCameraActivity() {

    private lateinit var binding: ActivityRealtimeRecognizeStudentBinding
    override val cameraView: CameraBridgeViewBase get() = binding.cameraView

    private val classId by lazy { intent.getIntExtra(EXTRA_CLASS_ID, 0) }

    @Volatile private var busy = false
    @Volatile private var faceLeft = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRealtimeRecognizeStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = intent.getStringExtra(EXTRA_SUBJECT)
        binding.btnSwitchCamera.setOnClickListener { switchCamera() }
        startCamera()
    }

    override fun onFacesDetected(count: Int) {
        if (count == 0) faceLeft = true
        if (count != 1 || busy || !faceLeft) return
        val face = facePart() ?: return
        busy = true
        faceLeft = false
        runOnUiThread { recognize(face) }
    }

    private fun recognize(face: MultipartBody.Part) {
        showStatus("Reconhecendo...", loading = true)
        scope.launch {
            safeCall { vision.api.recognize(classId, face) }
                .onSuccess { recognition ->
                    if (recognition.confidence > CONFIRMATION_DISTANCE) confirm(recognition, face)
                    else done("${recognition.student.user.name}: presente")
                }
                .onFailure {
                    showStatus(it.message ?: "Aluno não reconhecido")
                    if ((it as? ApiError)?.status == 401) handle(it)
                    busy = false
                }
        }
    }

    private fun confirm(recognition: Recognition, face: MultipartBody.Part) {
        val name = recognition.student.user.name
        AlertDialog.Builder(this)
            .setTitle("Confirmar aluno")
            .setMessage("Este aluno é $name?")
            .setCancelable(false)
            .setPositiveButton("Sim") { _, _ -> teach(recognition, face) }
            .setNegativeButton("Não") { _, _ -> undoPresence(recognition) }
            .show()
    }

    /** A confirmed but distant match becomes a new training sample, so the student is recognized faster next time. */
    private fun teach(recognition: Recognition, face: MultipartBody.Part) {
        scope.launch {
            safeCall { vision.api.teach(classId, recognition.student.id, face) }
                .onFailure { toast("Presença registrada, mas a foto não foi aprendida: ${it.message}") }
            done("${recognition.student.user.name}: presente")
        }
    }

    private fun undoPresence(recognition: Recognition) {
        scope.launch {
            safeCall {
                vision.api.setPresence(recognition.attendanceId, PresenceRequest(classId, recognition.student.id, false))
            }
                .onSuccess { showStatus("Presença desfeita. Tente novamente.") }
                .onFailure { showStatus("Não foi possível desfazer a presença de ${recognition.student.user.name}: ${it.message}") }
            busy = false
        }
    }

    private fun done(message: String) {
        showStatus(message)
        busy = false
    }

    private fun showStatus(message: String, loading: Boolean = false) {
        binding.tvStatus.text = message
        binding.progress.visible(loading)
    }

    companion object {
        /** Matches farther than this ask the teacher to confirm; the server rejects anything past its own threshold. */
        private const val CONFIRMATION_DISTANCE = 45.0
        private const val EXTRA_CLASS_ID = "classId"
        private const val EXTRA_SUBJECT = "subject"

        fun intent(context: Context, lecture: Lecture): Intent =
            Intent(context, RecognizeActivity::class.java)
                .putExtra(EXTRA_CLASS_ID, lecture.id)
                .putExtra(EXTRA_SUBJECT, lecture.subject.name)
    }
}
