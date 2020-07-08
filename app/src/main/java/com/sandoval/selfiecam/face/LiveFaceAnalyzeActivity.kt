package com.sandoval.selfiecam.face

import android.content.Context
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.LensEngine
import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.common.MLAnalyzer.MLTransactor
import com.huawei.hms.mlsdk.common.MLResultTrailer
import com.huawei.hms.mlsdk.face.MLFace
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting
import com.huawei.hms.mlsdk.face.MLMaxSizeFaceTransactor
import com.sandoval.selfiecam.R
import com.sandoval.selfiecam.camera.LensEnginePreview
import com.sandoval.selfiecam.overlay.GraphicOverlay
import com.sandoval.selfiecam.overlay.LocalFaceGraphic
import java.io.IOException
import java.lang.RuntimeException

class LiveFaceAnalyzeActivity : AppCompatActivity(), View.OnClickListener {

    private var analyzer: MLFaceAnalyzer? = null
    private var mLensEngine: LensEngine? = null
    private var mPreview: LensEnginePreview? = null
    private var overlay: GraphicOverlay? = null
    private var lensType = LensEngine.FRONT_LENS
    private var detectMode = 0
    private var isFront = false
    private val smilingRate = 0.8f
    private var safeToTakePicture = false
    private val smilingPossibility = 0.95f
    private var restart: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_face_analyze)
        if (savedInstanceState != null) {
            lensType = savedInstanceState.getInt("lensType")
        }
        mPreview = findViewById(R.id.preview)
        val intent = this.intent
        try {
            detectMode = intent.getIntExtra("detect_mode", 1)
        } catch (e: RuntimeException) {
            Log.e("LiveFace:", "Get intent value failed: $e")
        }
        createFaceAnalyzer()
        overlay = findViewById(R.id.face_overlay)
        findViewById<View>(R.id.facingSwitch).setOnClickListener(this)
        restart = findViewById(R.id.restart)
        createLensEngine()
    }

    override fun onResume() {
        super.onResume()
        startLensEngine()
    }

    override fun onPause() {
        super.onPause()
        mPreview!!.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mLensEngine != null) {
            mLensEngine!!.release()
        }
    }

    public override fun onSaveInstanceState(
        outState: Bundle
    ) {
        outState.putInt("lensType", lensType)
        super.onSaveInstanceState(outState)
    }

    override fun onClick(v: View?) {
        isFront = !isFront
        if (isFront) {
            lensType = LensEngine.FRONT_LENS
        } else {
            lensType = LensEngine.BACK_LENS
        }
        if (mLensEngine != null) {
            mLensEngine!!.close()
        }
        startPreview(v)
    }

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                STOP_PREVIEW -> stopPreview()
                TAKE_PHOTO -> takePhoto()
                else -> {

                }
            }
        }
    }

    private fun createFaceAnalyzer() {
        val setting = MLFaceAnalyzerSetting.Factory()
            .setFeatureType(MLFaceAnalyzerSetting.TYPE_FEATURES)
            .setKeyPointType(MLFaceAnalyzerSetting.TYPE_UNSUPPORT_KEYPOINTS)
            .setMinFaceProportion(0.1f)
            .setTracingAllowed(true)
            .create()
        analyzer = MLAnalyzerFactory.getInstance().getFaceAnalyzer(setting)
        if (detectMode == 1003) {
            val transactor =
                MLMaxSizeFaceTransactor.Creator(analyzer, object : MLResultTrailer<MLFace?>() {
                    override fun objectCreateCallback(
                        itemId: Int,
                        obj: MLFace?
                    ) {
                        overlay!!.clear()
                        if (obj == null) {
                            return
                        }
                        val faceGraphic = LocalFaceGraphic(
                            overlay!!,
                            obj,
                            this@LiveFaceAnalyzeActivity
                        )
                        overlay!!.addGraphic(faceGraphic)
                        val emotion = obj.emotions
                        if (emotion.smilingProbability > smilingPossibility) {
                            safeToTakePicture = false
                            mHandler.sendEmptyMessage(TAKE_PHOTO)
                        }
                    }

                    override fun objectUpdateCallback(
                        var1: MLAnalyzer.Result<MLFace?>?,
                        obj: MLFace?
                    ) {
                        overlay!!.clear()
                        if (obj == null) {
                            return
                        }
                        val faceGraphic = LocalFaceGraphic(
                            overlay!!,
                            obj,
                            this@LiveFaceAnalyzeActivity
                        )
                        overlay!!.addGraphic(faceGraphic)
                        val emotion = obj.emotions
                        if (emotion.smilingProbability > smilingPossibility && safeToTakePicture) {
                            safeToTakePicture = false
                            mHandler.sendEmptyMessage(TAKE_PHOTO)
                        }
                    }

                    override fun lostCallback(result: MLAnalyzer.Result<MLFace?>?) {
                        overlay!!.clear()
                    }

                    override fun completeCallback() {
                        overlay!!.clear()
                    }
                }).create()
            analyzer!!.setTransactor(transactor)
        } else {
            analyzer!!.setTransactor(object : MLTransactor<MLFace> {
                override fun destroy() {}
                override fun transactResult(result: MLAnalyzer.Result<MLFace>) {
                    val faceSparseArray = result.analyseList
                    var flag = 0
                    for (i in 0 until faceSparseArray.size()) {
                        val emotion = faceSparseArray.valueAt(i).emotions
                        if (emotion.smilingProbability > smilingPossibility) {
                            flag++
                        }
                    }
                    if (flag > faceSparseArray.size() * smilingRate && safeToTakePicture) {
                        safeToTakePicture = false
                        mHandler.sendEmptyMessage(TAKE_PHOTO)
                    }
                }
            })
        }
    }

    private fun createLensEngine() {
        val context: Context = this.applicationContext
        mLensEngine = LensEngine.Creator(context, analyzer).setLensType(lensType)
            .applyDisplayDimension(640, 480)
            .applyFps(25.0f)
            .enableAutomaticFocus(true)
            .create()
    }

    private fun startLensEngine() {
        restart!!.visibility = View.GONE
        if (mLensEngine != null) {
            try {
                if (detectMode == 1003) {
                    mPreview!!.start(mLensEngine, overlay)
                } else {
                    mPreview!!.start(mLensEngine)
                }
                safeToTakePicture = true
            } catch (e: IOException) {
                mLensEngine!!.release()
                mLensEngine = null
            }
        }
    }

    private fun takePhoto() {
        mLensEngine!!.photograph(null,
            LensEngine.PhotographListener { bytes ->
                mHandler.sendEmptyMessage(STOP_PREVIEW)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            })
    }

    fun startPreview(view: View?) {
        createFaceAnalyzer()
        mPreview!!.release()
        createLensEngine()
        startLensEngine()
    }

    fun stopPreview() {
        restart!!.setVisibility(View.VISIBLE)
        if (mLensEngine != null) {
            mLensEngine!!.release()
            safeToTakePicture = false
        }
        if (analyzer != null) {
            try {
                analyzer!!.stop()
            } catch (e: IOException) {
                Log.e("Failed", "Stop Failed: $e")
            }
        }
    }

    companion object {
        private const val STOP_PREVIEW = 1
        private const val TAKE_PHOTO = 2
    }
}