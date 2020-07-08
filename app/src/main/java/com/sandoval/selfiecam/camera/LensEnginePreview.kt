package com.sandoval.selfiecam.camera

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import com.huawei.hms.common.size.Size
import com.huawei.hms.mlsdk.common.LensEngine
import com.sandoval.selfiecam.overlay.GraphicOverlay
import java.io.IOException

class LensEnginePreview(context: Context, attrs: AttributeSet?) : ViewGroup(context, attrs) {

    private val mContext: Context = context
    private val mSurfaceView: SurfaceView
    private var mStartRequested: Boolean
    private var mSurfaceAvailable: Boolean
    private var mLensEngine: LensEngine? = null
    private var mOverlay: GraphicOverlay? = null

    @Throws(IOException::class)
    fun start(lensEngine: LensEngine?, overlay: GraphicOverlay?) {
        mOverlay = overlay
        start(lensEngine)
    }

    @Throws(IOException::class)
    fun start(lensEngine: LensEngine?) {
        if (lensEngine == null) {
            stop()
        }
        mLensEngine = lensEngine
        if (mLensEngine != null) {
            mStartRequested = true
            startIfReady()
        }
    }

    fun stop() {
        if (mLensEngine != null) {
            mLensEngine!!.close()
        }
    }

    fun release() {
        if (mLensEngine != null) {
            mLensEngine!!.release()
            mLensEngine = null
        }
    }

    @Throws(IOException::class)
    fun startIfReady() {
        if (mStartRequested && mSurfaceAvailable) {
            mLensEngine!!.run(mSurfaceView.holder)
            if (mOverlay != null) {
                val size: Size = mLensEngine!!.displayDimension
                val min: Int = size.width.coerceAtMost(size.height)
                val max: Int = size.width.coerceAtLeast(size.height)
                if (Configuration.ORIENTATION_PORTRAIT == mContext.resources.configuration.orientation) {
                    mOverlay!!.setCameraInfo(min, max, mLensEngine!!.lensType)
                } else {
                    mOverlay!!.setCameraInfo(max, min, mLensEngine!!.lensType)
                }
                mOverlay!!.clear()
            }
            mStartRequested = false
        }
    }

    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            mSurfaceAvailable = false
        }

        override fun surfaceCreated(holder: SurfaceHolder?) {
            mSurfaceAvailable = true
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e("LensPreview: ", "Could not start camera source: $e")
            }
        }
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        var previewWidth = 320
        var previewHeight = 240
        if (mLensEngine != null) {
            val size: Size? = mLensEngine!!.displayDimension
            if (size != null) {
                previewWidth = size.width
                previewHeight = size.height
            }
        }

        if (mContext.resources
                .configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        ) {
            val tmp = previewWidth
            previewWidth = previewHeight
            previewHeight = tmp
        }

        val viewWidth = right - left
        val viewHeight = bottom - top
        val childWidth: Int
        val childHeight: Int
        var childXOffset = 0
        var childYOffset = 0
        val widthRatio = viewWidth.toFloat() / previewWidth.toFloat()
        val heightRatio = viewHeight.toFloat() / previewHeight.toFloat()

        if (widthRatio > heightRatio) {
            childWidth = viewWidth
            childHeight = (previewHeight.toFloat() * heightRatio).toInt()
            childYOffset = (childHeight - viewHeight) / 2
        } else {
            childWidth = (previewWidth.toFloat() * heightRatio).toInt()
            childHeight = viewHeight
            childXOffset = (childWidth - viewWidth) / 2
        }
        for (i in 0 until this.childCount){
            getChildAt(i).layout(
                -1*childXOffset, -1*childYOffset, childWidth-childXOffset,
                childHeight-childYOffset
            )
        }
        try {
            startIfReady()
        } catch (e: IOException){
            Log.e("LensEngine: ", "Could Not start the camera: $e")
        }
    }

    init {
        mStartRequested = false
        mSurfaceAvailable = false
        mSurfaceView = SurfaceView(context)
        mSurfaceView.holder.addCallback(SurfaceCallback())
        this.addView(mSurfaceView)
    }


}