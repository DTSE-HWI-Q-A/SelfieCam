package com.sandoval.selfiecam.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.sandoval.selfiecam.R
import com.sandoval.selfiecam.auth.AuthActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val displayName = intent.getStringExtra("dispName")
        if (displayName!!.isNotEmpty()) {
            displayNameText.text = displayName
        } else {
            Log.d("DisplayName: ", "Vacion")
        }
        logoutBtn.setOnClickListener {
            logoutWithHuaweiID()
        }
    }

    private fun logoutWithHuaweiID() {
        val mAuthParams = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .createParams()
        val mAuthManager = HuaweiIdAuthManager.getService(this, mAuthParams)
        val logoutTask = mAuthManager.signOut()
        logoutTask.addOnSuccessListener {
            startActivity(Intent(this@MainActivity, AuthActivity::class.java))
            finish()
        }
        logoutTask.addOnFailureListener {
            Toast.makeText(this, "Logout Fallo!", Toast.LENGTH_LONG).show()
        }

    }

    override fun onBackPressed() {
        //Do nothing
    }
}