package com.mariofronza.face_collection_app.face

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.mariofronza.face_collection_app.R
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfRect
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * Camera screen that finds the largest face on every frame and keeps its grayscale crop,
 * so subclasses only decide what to do with a face.
 */
abstract class FaceCameraActivity : CameraActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    protected abstract val cameraView: CameraBridgeViewBase

    private var detector: CascadeClassifier? = null
    private var cameraIndex = CameraBridgeViewBase.CAMERA_ID_FRONT
    private var face: Mat? = null

    /** Coroutines tied to this screen; OpenCV camera activities are not lifecycle owners. */
    protected val scope = MainScope()

    /** Called on the camera thread with the number of faces in the latest frame. */
    protected open fun onFacesDetected(count: Int) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    protected fun startCamera() {
        if (!OpenCVLoader.initLocal()) {
            Toast.makeText(this, "Não foi possível iniciar o OpenCV", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        detector = loadDetector()
        cameraView.setCameraIndex(cameraIndex)
        cameraView.setCvCameraViewListener(this)
    }

    protected fun switchCamera() {
        cameraIndex = if (cameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT) {
            CameraBridgeViewBase.CAMERA_ID_BACK
        } else {
            CameraBridgeViewBase.CAMERA_ID_FRONT
        }
        cameraView.disableView()
        cameraView.setCameraIndex(cameraIndex)
        cameraView.enableView()
    }

    /** The latest face as a JPEG upload part, or null when no face is in view. */
    protected fun facePart(): MultipartBody.Part? = faceJpeg()?.let {
        MultipartBody.Part.createFormData("file", "face.jpg", it.toRequestBody("image/jpeg".toMediaType()))
    }

    protected fun faceJpeg(): ByteArray? = synchronized(this) {
        face?.let { crop -> MatOfByte().also { Imgcodecs.imencode(".jpg", crop, it) }.toArray() }
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase> = listOf(cameraView)

    override fun onResume() {
        super.onResume()
        cameraView.enableView()
    }

    override fun onPause() {
        super.onPause()
        cameraView.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {}

    override fun onCameraViewStopped() {}

    override fun onCameraFrame(frame: CvCameraViewFrame): Mat {
        val rgba = frame.rgba()
        val gray = frame.gray()
        toPortrait(rgba)
        toPortrait(gray)

        val faces = MatOfRect().also {
            detector?.detectMultiScale(gray, it, 1.1, 4, 0, Size(MIN_FACE, MIN_FACE))
        }.toArray()

        val largest = faces.maxByOrNull { it.area() }
        synchronized(this) {
            face = largest?.let { Mat(gray, it).clone() }
        }
        largest?.let { Imgproc.rectangle(rgba, it.tl(), it.br(), Scalar(0.0, 255.0, 0.0, 255.0), 3) }

        onFacesDetected(faces.size)
        return rgba
    }

    private fun toPortrait(mat: Mat) {
        val center = Point(mat.cols() / 2.0, mat.rows() / 2.0)
        Imgproc.warpAffine(mat, mat, Imgproc.getRotationMatrix2D(center, 90.0, 1.0), mat.size())
        Core.flip(mat, mat, if (cameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT) 1 else 0)
    }

    private fun loadDetector(): CascadeClassifier? {
        val cascade = File(cacheDir, "lbpcascade_frontalface.xml")
        if (!cascade.exists()) {
            resources.openRawResource(R.raw.lbpcascade_frontalface).use { input ->
                cascade.outputStream().use { input.copyTo(it) }
            }
        }
        return CascadeClassifier(cascade.absolutePath).takeUnless { it.empty() }
    }

    private companion object {
        const val MIN_FACE = 200.0
    }
}
