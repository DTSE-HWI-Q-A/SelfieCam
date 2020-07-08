package com.sandoval.selfiecam.camera

open class CameraConfiguration {

    var fps = 20.0f
    var previewHeight = MAX_HEIGH
    var isAutoFocus = true

    @Synchronized
    fun setCameraFacing(facing: Int) {
        require(!(facing != CAMERA_FACING_BACK && facing != CAMERA_FACING_FRONT)) {
            "Invalid Camera: $facing"

        }
        cameraFacing = facing
    }

    companion object {
        val CAMERA_FACING_BACK: Int = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
        val CAMERA_FACING_FRONT: Int = android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT
        const val MAX_HEIGH = 720

        @get:Synchronized
        var cameraFacing = CAMERA_FACING_BACK
            protected set
    }
}