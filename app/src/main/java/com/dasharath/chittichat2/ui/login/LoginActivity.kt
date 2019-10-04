package com.dasharath.chittichat2.ui.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dasharath.chittichat2.MainActivity
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.ui.phonelogin.PhoneLoginActivity
import com.dasharath.chittichat2.ui.register.RegisterActivity
import com.dasharath.chittichat2.utils.CommonUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*



class LoginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var loadingBar: ProgressDialog? = null
    private var userRef:DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        userRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.USERS_DB_REF)
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

                        FirebaseInstanceId.getInstance().instanceId
                            .addOnCompleteListener(OnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Log.w("TAG", "getInstanceId failed", task.exception)
                                    return@OnCompleteListener
                                }

                                val currentUserId = mAuth?.currentUser?.uid!!
                                // Get new Instance ID token
                                val token = task.result!!.token
                                userRef?.child(currentUserId)?.child(CommonUtils.DEVICE_TOKEN)?.setValue(token)?.addOnCompleteListener {
                                    if(it.isSuccessful){
                                        sendUserToMainActivity()
                                        Toast.makeText(this@LoginActivity,"Login successfully...",Toast.LENGTH_LONG).show()
                                        loadingBar?.dismiss()
                                    }
                                }

                                // Log and toast
                                val msg = token.toString()
                                Log.d("TAG", msg)
                                Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                            })


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
