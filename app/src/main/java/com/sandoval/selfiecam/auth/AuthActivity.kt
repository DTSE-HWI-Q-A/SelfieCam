package com.sandoval.selfiecam.auth

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.sandoval.selfiecam.R
import com.sandoval.selfiecam.main.MainActivity
import com.sandoval.selfiecam.push.GetTokenAction
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        btnLogin.setOnClickListener {
            loginWithHuaweiID()
        }

        GetTokenAction().getToken(this) {
            Log.d("PushToken: ", it)
        }
    }

    private fun loginWithHuaweiID() {
        val mAuthParams = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setEmail()
            .setAccessToken()
            .setProfile()
            .setIdToken()
            .setUid()
            .setId()
            .createParams()
        val mAuthManager = HuaweiIdAuthManager.getService(this, mAuthParams)
        startActivityForResult(mAuthManager.signInIntent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Login Cancelado!", Toast.LENGTH_LONG).show()
            } else if (resultCode == Activity.RESULT_OK) {
                val authHuaweiTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
                if (authHuaweiTask.isSuccessful) {
                    val huaweiAccount = authHuaweiTask.result
                    val intent = Intent(this@AuthActivity, MainActivity::class.java)
                    intent.putExtra("dispName", huaweiAccount.displayName)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Fallo el login con Huawei ID", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}