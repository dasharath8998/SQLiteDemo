package com.dasharath.chittichat2.ui.register

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dasharath.chittichat2.MainActivity
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.ui.login.LoginActivity
import com.dasharath.chittichat2.utils.CommonUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var loadingBar: ProgressDialog? = null
    private var rootReference: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        init()
        listeners()
    }

    private fun init() {
        loadingBar = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        rootReference = FirebaseDatabase.getInstance().reference
    }

    private fun listeners() {
        tvAlredyHaveAccountLink.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
        }

        btnRegisterButton.setOnClickListener {
            createNewAccount()
        }
    }

    private fun createNewAccount() {
        var email = etRegisterEmail.text.toString()
        var password = etRegisterPassword.text.toString()

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this@RegisterActivity,"Please enter email...",Toast.LENGTH_LONG).show()
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(this@RegisterActivity,"Please enter password...",Toast.LENGTH_LONG).show()
        }else{
            loadingBar?.setTitle("Creating new account")
            loadingBar?.setTitle("Please wail while we are creating new account for you")
            loadingBar?.setCanceledOnTouchOutside(true)
            loadingBar?.show()
            mAuth?.createUserWithEmailAndPassword(email,password)
                ?.addOnCompleteListener {
                    if(it.isSuccessful){

                        var currentUserId = mAuth?.currentUser?.uid
                        rootReference?.child(CommonUtils.USERS_DB_REF)?.child(currentUserId!!)?.setValue("")

                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                        finish()
                        Toast.makeText(this@RegisterActivity,"Account created successfully...",Toast.LENGTH_LONG).show()
                        loadingBar?.dismiss()
                    }else{
                        var message = it.exception.toString()
                        Toast.makeText(this@RegisterActivity,"Error: "+message,Toast.LENGTH_LONG).show()
                        loadingBar?.dismiss()
                    }
                }
        }
    }
}
