package com.dasharath.chittichat2.ui.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.dasharath.chittichat2.MainActivity
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.utils.CommonUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    private var cuurentUserId = ""
    private var mAuth: FirebaseAuth? = null
    private var rootRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        init()

        etSettingsUsername.visibility = View.GONE

        retriveUserInformation()

        listeners()
    }

    private fun init() {
        mAuth = FirebaseAuth.getInstance()
        cuurentUserId = mAuth?.currentUser?.uid!!
        rootRef = FirebaseDatabase.getInstance().reference
    }

    private fun retriveUserInformation() {
        rootRef?.child(CommonUtils.USERS_DB_REF)?.addValueEventListener(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if((snapshot.exists()) && (snapshot.hasChild(CommonUtils.NAME)) && (snapshot.hasChild(CommonUtils.IMAGE))){

                    val username = snapshot.child(CommonUtils.NAME).value.toString()
                    val status = snapshot.child(CommonUtils.STATUS).value.toString()
                    val image = snapshot.child(CommonUtils.IMAGE).value.toString()

                    etSettingsUsername.setText(username)
                    etSettingsStatus.setText(status)

                }else if((snapshot.exists()) && (snapshot.hasChild(CommonUtils.NAME))){

                    val username = snapshot.child(CommonUtils.NAME).value.toString()
                    val status = snapshot.child(CommonUtils.STATUS).value.toString()
                    val image = snapshot.child(CommonUtils.IMAGE).value.toString()

                    etSettingsUsername.setText(username)
                    etSettingsStatus.setText(status)

                }else{
                    etSettingsUsername.visibility = View.VISIBLE
                    Toast.makeText(this@SettingsActivity,"Please set & update your profile information",Toast.LENGTH_LONG).show()
                }
            }

        })
    }

    private fun listeners() {
        btnSettingsUpdate.setOnClickListener {
            updateSettings()
        }
    }

    private fun updateSettings() {
        var username = etSettingsUsername.text.toString()
        var status = etSettingsStatus.text.toString()

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this@SettingsActivity,"Please write your user name first....",Toast.LENGTH_LONG).show()
        }else if(TextUtils.isEmpty(status)){
            Toast.makeText(this@SettingsActivity,"Please write your statuse first....",Toast.LENGTH_LONG).show()
        }else{
            var profile = HashMap<String,String>()
            profile.put("uid",cuurentUserId)
            profile.put("name",username)
            profile.put("status",status)
            rootRef?.child(CommonUtils.USERS_DB_REF)?.child(cuurentUserId)?.setValue(profile)
                ?.addOnCompleteListener {
                    if(it.isSuccessful) {
                        startActivity(Intent(this@SettingsActivity,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                        Toast.makeText(
                            this@SettingsActivity,
                            "Profile updated succesfully",
                            Toast.LENGTH_LONG
                        ).show()
                    }else{
                        var message = it.exception.toString()
                        Toast.makeText(
                            this@SettingsActivity,
                            "Error:"+message ,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}
