package pat.project.facedetection.face

import android.annotation.SuppressLint
import android.util.Half.toFloat
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.graphics.toPoint
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.Math.pow
import kotlin.math.sqrt
import kotlin.math.pow

@ExperimentalGetImage
object FaceAnalyzer : ImageAnalysis.Analyzer {

    private val _faceFlow = MutableStateFlow(0f)
    val faceFlow = _faceFlow.asStateFlow()

    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .setMinFaceSize(0.16f)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(realTimeOpts)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        mediaImage?.let {
            val inputImage =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    faces.firstOrNull()?.let { face ->
                        face.getContour(FaceContour.FACE)?.let { faceContour ->
                            val faceContourLeft =
                                faceContour.points.getOrNull(9)?.toPoint() //google devs points image
                            val faceContourRight = faceContour.points.getOrNull(27)?.toPoint()
                            if (faceContourLeft != null && faceContourRight != null) {
                                val (lx, ly) = listOf(faceContourLeft.x, faceContourLeft.y)
                                val (rx, ry) = listOf(faceContourRight.x, faceContourRight.y)
                                val faceWidth = sqrt((lx - rx).toDouble().pow(2) + (ly - ry).toDouble().pow(2)).toFloat()
                                val imageWidth = mediaImage.width.toFloat()
                                if(imageWidth!=0f){
                                    CoroutineScope(Dispatchers.Default).launch {
                                        _faceFlow.emit(faceWidth/imageWidth)

                                    }
                                }

                            }
                        }
                    } ?: run {
                        CoroutineScope(Dispatchers.Default).launch {
                            _faceFlow.emit(0f)
                        }
                    }
                    imageProxy.close()
                }
                .addOnFailureListener {
                    imageProxy.close()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}