package com.sandoval.selfiecam.face

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.huawei.hms.mlsdk.common.LensEngine
import com.sandoval.selfiecam.R
import com.sandoval.selfiecam.camera.LensEnginePreview
import com.sandoval.selfiecam.overlay.GraphicOverlay
import java.lang.RuntimeException

class LiveFaceAnalyzeActivity : AppCompatActivity() {

    private var mLensEngine: LensEngine? = null
    private var mPreview: LensEnginePreview? = null
    private var overlay: GraphicOverlay? = null
    private var lensType = LensEngine.FRONT_LENS
    private var detectMode = 0

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
        overlay = findViewById(R.id.face_overlay)
    }


    override fun onPause() {
        super.onPause()
        mPreview!!.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mLensEngine!=null){
            mLensEngine!!.release()
        }
    }

    public override fun onSaveInstanceState(
        outState: Bundle
    ) {
        outState.putInt("lensType", lensType)
        super.onSaveInstanceState(outState)
    }
}