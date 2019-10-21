package com.dasharath.chittichat2.ui.settings

import android.app.Activity
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
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_settings.*
import android.R.attr.data
import android.R.attr.process
import android.app.ProgressDialog
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.dasharath.chittichat2.utils.CommonFunction
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.hbb20.CountryCodePicker
import kotlinx.android.synthetic.main.app_bar_layout.*


class SettingsActivity : AppCompatActivity() {

    private var cuurentUserId = ""
    private var mAuth: FirebaseAuth? = null
    private var rootRef: DatabaseReference? = null
    private val GALLARY_PICK = 123
    private var userPfofileImagesRef: StorageReference? = null
    private var loadingBar: ProgressDialog? = null
    private var networkFlag = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        init()
        listeners()
        retriveUserInformation()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        networkFlag = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        networkFlag = false
    }

    private fun init() {
        setSupportActionBar(appBarSettings as Toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        tvTitle.text = "Settings"
        mAuth = FirebaseAuth.getInstance()
        cuurentUserId = mAuth?.currentUser?.uid!!
        rootRef = FirebaseDatabase.getInstance().reference
        userPfofileImagesRef =
            FirebaseStorage.getInstance().getReference().child(CommonUtils.PROFILE_IMAGES)
        loadingBar = ProgressDialog(this)
        if(intent.getBooleanExtra("IsFirstTime",false)){
            imgBack.visibility = View.GONE
        }
    }

    private fun listeners() {
        btnSettingsUpdate.setOnClickListener {
            updateSettings()
        }

        imgSettingProfile.setOnClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this)
        }

        imgBack.setOnClickListener {
            CommonFunction.hideKeyboard(this@SettingsActivity)
            onBackPressed()
        }
    }

    private fun retriveUserInformation() {
        rootRef?.child(CommonUtils.USERS_DB_REF)?.child(cuurentUserId)
            ?.addValueEventListener(object : ValueEventListener {

                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    val hasName = snapshot.hasChild(CommonUtils.NAME)
                    val hasImage = snapshot.hasChild(CommonUtils.IMAGE)
                    val exist = snapshot.exists()

                    if(exist && hasImage){
                        val image = snapshot.child(CommonUtils.IMAGE).value.toString()
                        if(networkFlag) {
                            Glide.with(this@SettingsActivity).load(image)
                                .placeholder(R.drawable.profile_image)
                                .error(R.drawable.profile_image).into(imgSettingProfile)
                        }
                    }

                    if (exist && hasName && hasImage) {

                        val username = snapshot.child(CommonUtils.NAME).value.toString()
                        val status = snapshot.child(CommonUtils.STATUS).value.toString()
                        val image = snapshot.child(CommonUtils.IMAGE).value.toString()

                        etSettingsUsername.setText(username)
                        etSettingsStatus.setText(status)
                        ccpProfile.setCountryForNameCode(snapshot.child("countryCode").value.toString())
                        if(snapshot.child("gender").value.toString() == "Female") {
                            spinnerGender.setSelection(1)
                        } else {
                            spinnerGender.setSelection(0)
                        }

                        if(networkFlag) {
                            Glide.with(this@SettingsActivity).load(image).into(imgSettingProfile)
                        }

                    }else if (exist && hasName) {

                        val username = snapshot.child(CommonUtils.NAME).value.toString()
                        val status = snapshot.child(CommonUtils.STATUS).value.toString()

                        etSettingsUsername.setText(username)
                        etSettingsStatus.setText(status)
                        ccpProfile.setCountryForNameCode(snapshot.child("countryCode").value.toString())

                        if(snapshot.child("gender").value.toString() == "Female") {
                            spinnerGender.setSelection(1)
                        } else {
                            spinnerGender.setSelection(0)
                        }

                    } else {
                        Toast.makeText(this@SettingsActivity, "Please set & update your profile information", Toast.LENGTH_LONG).show()
                    }
                }

            })
    }

    private fun updateSettings() {
        val username = etSettingsUsername.text.toString()
        val status = etSettingsStatus.text.toString()

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this@SettingsActivity, "Please write your user name first....", Toast.LENGTH_LONG).show()
        } else if (TextUtils.isEmpty(status)) {
            Toast.makeText(this@SettingsActivity, "Please write your status first....", Toast.LENGTH_LONG).show()
        } else {
            val profile = HashMap<String, Any>()
            profile.put("uid", cuurentUserId)
            profile.put("name", username)
            profile.put("status", status)
            profile.put("gender", spinnerGender.selectedItem.toString())
            profile.put("countryCode", ccpProfile.selectedCountryNameCode)
            rootRef?.child(CommonUtils.USERS_DB_REF)?.child(cuurentUserId)?.updateChildren(profile)
                ?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        startActivity(
                            Intent(
                                this@SettingsActivity,
                                MainActivity::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                        CommonFunction.hideKeyboard(this@SettingsActivity)
                        Toast.makeText(this@SettingsActivity, "Profile updated succesfully", Toast.LENGTH_LONG).show()
                    } else {
                        val message = it.exception.toString()
                        Toast.makeText(this@SettingsActivity, "Error:" + message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun putFileToFirebase(result: CropImage.ActivityResult) {
        val resultUri = result.uri
        val filePath = userPfofileImagesRef?.child(cuurentUserId + ".jpg")
        filePath?.putFile(resultUri)?.addOnCompleteListener {
            if (it.isSuccessful) {
                storeDownloadUrl(filePath)
            } else {
                loadingBar?.dismiss()
                Toast.makeText(this@SettingsActivity, "Error: " + it.exception.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun storeDownloadUrl(filePath: StorageReference) {
        filePath.downloadUrl.addOnSuccessListener {
            rootRef?.child(CommonUtils.USERS_DB_REF)?.child(cuurentUserId)
                ?.child(CommonUtils.IMAGE)?.setValue(it.toString())
                ?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        loadingBar?.dismiss()
                        finish()
                        startActivity(intent)
                        Toast.makeText(this@SettingsActivity, "Profile URL uploaded successfully...", Toast.LENGTH_LONG).show()
                    } else {
                        loadingBar?.dismiss()
                        Toast.makeText(this@SettingsActivity, "Error: " + it.exception.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            var url = it.toString()
        }
        Toast.makeText(
            this@SettingsActivity,
            "Profile Image uploaded successfully...",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun setProgress() {
        loadingBar?.setTitle("Set Profile Image")
        loadingBar?.setMessage("Please wait, your profile image is updating...")
        loadingBar?.setCanceledOnTouchOutside(false)
        loadingBar?.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                setProgress()
                putFileToFirebase(result)
            }
        }
    }
}
