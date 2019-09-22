package com.dasharath.chittichat2.ui.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dasharath.chittichat2.MainActivity
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.ui.phonelogin.PhoneLoginActivity
import com.dasharath.chittichat2.ui.register.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var loadingBar: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        loadingBar = ProgressDialog(this)

        listeners()
    }

    private fun listeners() {
        tvNeedNewAccount.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        btnLoginButton.setOnClickListener {
            allowUserToLogIn()
        }

        btnLoginPhoneButton.setOnClickListener {
            startActivity(Intent(this@LoginActivity, PhoneLoginActivity::class.java))
        }
    }

    private fun allowUserToLogIn() {
        var email = etLoginEmail.text.toString()
        var password = etLoginPassword.text.toString()

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this@LoginActivity,"Please enter email...",Toast.LENGTH_LONG).show()
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(this@LoginActivity,"Please enter password...",Toast.LENGTH_LONG).show()
        }else{
            loadingBar?.setTitle("Sign In")
            loadingBar?.setTitle("Please wait...")
            loadingBar?.setCanceledOnTouchOutside(true)
            loadingBar?.show()
            mAuth?.signInWithEmailAndPassword(email,password)
                ?.addOnCompleteListener {
                    if(it.isSuccessful){
                        sendUserToMainActivity()
                        Toast.makeText(this@LoginActivity,"Login successfully...",Toast.LENGTH_LONG).show()
                        loadingBar?.dismiss()
                    }else{
                        var message = it.exception.toString()
                        Toast.makeText(this@LoginActivity,"Error: "+message,Toast.LENGTH_LONG).show()
                        loadingBar?.dismiss()
                    }

                }
        }
    }

    private fun sendUserToMainActivity() {
        startActivity(Intent(this@LoginActivity,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }
}
