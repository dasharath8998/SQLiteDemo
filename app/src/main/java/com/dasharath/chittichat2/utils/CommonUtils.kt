package com.dasharath.chittichat2.utils

import android.annotation.SuppressLint
import android.view.View
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.dasharath.chittichat2.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView


object CommonUtils {

    const val MESSAGE_ID = "messageID"
    const val USER_STATE = "userState"
    const val DEVICE_TOKEN = "device_token"
    const val NOTIFICATION = "Notification"
    const val MESSAGES = "Messages"
    const val SAVED = "Saved"
    const val CONTACTS = "Contacts"
    const val RECEIVED = "received"
    const val SENT = "sent"
    const val REQUEST_TYPE: String = "request_type"
    const val CHAT_REQUEST = "Chat Request"
    const val GROUP_NAME = "groupName"
    const val USERS_DB_REF = "Users"
    const val GROUPS = "Groups"
    const val SIMPLE_DATE_FORMAT = "MMM dd, yyyy"
    const val SIMPLE_TIME_FORMAT = "hh:mm a"

    const val STATUS = "status"
    const val IMAGE = "image"
    const val IMAGE_FILES = "Image Files"

    const val NAME = "name"
    const val TIME = "time"
    const val DATE = "date"
    const val STATE = "state"
    const val MESSAGE = "message"

    const val UID = "uid"

    const val PROFILE_IMAGES = "Profile Images"
}

object CommonFunction{

    fun View.visible() {
        visibility = View.VISIBLE
    }

    fun View.gone() {
        visibility = View.GONE
    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @SuppressLint("ResourceType")
    fun showImage(imageUri: String, context: Context,isCircle: Boolean) {
        var imageView: ImageView? = null
        val builder = Dialog(context)
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
        builder.window!!.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )

        if(isCircle) {
            imageView = CircleImageView(context)
        } else {
            imageView = ImageView(context)
        }
        Glide.with(context).load(imageUri).placeholder(R.drawable.profile_image).into(imageView)
        builder.addContentView(
            imageView, RelativeLayout.LayoutParams(
                550,
                650
            )
        )
        builder.show()
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun showSnackBar(view:View,activity: Activity){
        val snackbar = Snackbar.make(view,"No internet connection!!", Snackbar.LENGTH_INDEFINITE).setAction("Retry") {
            activity.finish()
            activity.startActivity(activity.intent)
        }
        snackbar.setActionTextColor(ContextCompat.getColor(activity,R.color.colorPrimaryDark))
        snackbar.show()
    }

}