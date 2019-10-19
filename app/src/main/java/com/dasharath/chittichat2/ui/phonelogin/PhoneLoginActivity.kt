package com.dasharath.chittichat2.ui.phonelogin

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.dasharath.chittichat2.MainActivity
import com.dasharath.chittichat2.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_phone_login.*
import java.util.concurrent.TimeUnit

class PhoneLoginActivity : AppCompatActivity() {

    private var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? =  null
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mAuth: FirebaseAuth? = null
    private var loadingBar: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_login)
        mAuth = FirebaseAuth.getInstance()
        loadingBar = ProgressDialog(this@PhoneLoginActivity)

        listeners()
        callbackPhone()
    }

    private fun listeners() {
        btnSendVerificationCode.setOnClickListener {

            val phoneNumber = "+${ccp.selectedCountryCode}${etPhoneNumberLogin.text}"
            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(
                    this@PhoneLoginActivity,
                    "Phone Number is Required...",
                    Toast.LENGTH_LONG
                ).show()
            } else {

                loadingBar?.setTitle("Form verification")
                loadingBar?.setMessage("Please wait while we are authenticating your phone...")
                loadingBar?.setCanceledOnTouchOutside(false)
                loadingBar?.show()

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber, // Phone number to verify
                    60, // Timeout duration
                    TimeUnit.SECONDS, // Unit of timeout
                    this@PhoneLoginActivity, // Activity (for callback binding)
                    callbacks!!
                ) // OnVerificationStateChangedCallbacks
            }
        }

        btnVerifyLogin.setOnClickListener {
            etPhoneNumberLogin.visibility = View.INVISIBLE
            btnSendVerificationCode.visibility = View.INVISIBLE
            ccp.isVisible = false
            val verificationCode = etVerificationCode.text.toString()

            if(TextUtils.isEmpty(verificationCode)){
                Toast.makeText(this@PhoneLoginActivity,"Please write verification code first...",Toast.LENGTH_LONG).show()
            }else{
                loadingBar?.setTitle("Code verification")
                loadingBar?.setMessage("Please wait while we are verify your code...")
                loadingBar?.setCanceledOnTouchOutside(false)
                loadingBar?.show()

                val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, verificationCode)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun callbackPhone() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                loadingBar?.dismiss()

                btnSendVerificationCode.visibility = View.VISIBLE
                etPhoneNumberLogin.visibility = View.VISIBLE

                etVerificationCode.visibility = View.INVISIBLE
                btnVerifyLogin.visibility = View.INVISIBLE
                Toast.makeText(
                    this@PhoneLoginActivity,
                    "Invalide Phone, Please correct your country code than try",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {

                loadingBar?.dismiss()
                Log.d("PhoneLoginActivity", "onCodeSent:$verificationId")

                btnSendVerificationCode.visibility = View.INVISIBLE
                etPhoneNumberLogin.visibility = View.INVISIBLE

                etVerificationCode.visibility = View.VISIBLE
                btnVerifyLogin.visibility = View.VISIBLE

                // Save verification ID and resending token so we can use them latevr
                storedVerificationId = verificationId
                resendToken = token

                Toast.makeText(this@PhoneLoginActivity, "Code has sent....", Toast.LENGTH_LONG)
                    .show()
            }

        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("PhoneLoginActivity", "signInWithCredential:success")
                    loadingBar?.dismiss()
                    Toast.makeText(this@PhoneLoginActivity,"Congratulations...",Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@PhoneLoginActivity,MainActivity::class.java))
                    finish()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w("PhoneLoginActivity", "signInWithCredential:failure", task.exception)
                    val message = task.exception
                    Toast.makeText(this@PhoneLoginActivity,"Error..."+message,Toast.LENGTH_LONG).show()
                }
            }
    }
}
