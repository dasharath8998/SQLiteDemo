package com.dasharath.chittichat2.utils

import android.view.View
import android.app.Activity
import android.view.inputmethod.InputMethodManager


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

}