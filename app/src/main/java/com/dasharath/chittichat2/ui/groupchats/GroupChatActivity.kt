package com.dasharath.chittichat2.ui.groupchats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.dasharath.chittichat2.MainActivity
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.utils.CommonFunction
import com.dasharath.chittichat2.utils.CommonUtils
import com.dasharath.chittichat2.utils.CommonFunction.visible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_group_chat.*
import kotlinx.android.synthetic.main.app_bar_layout.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class GroupChatActivity : AppCompatActivity() {

    private var currentGroupName: String = ""
    private var currentUserId: String = ""
    private var currentUserName: String = ""

    private var mAuth: FirebaseAuth? = null
    private var usersRef: DatabaseReference? = null
    private var groupNameRef: DatabaseReference? = null
    private var groupMessageKeyRef: DatabaseReference? = null

    private var currentDate: String = ""
    private var currentTime: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        init()
        getUserInfo()
        listeners()
    }

    override fun onStart() {
        super.onStart()
        groupNameRef?.addChildEventListener(object : ChildEventListener {

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {
                if(dataSnapshot.exists()){
                    displayMessages(dataSnapshot)
                }
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                if(dataSnapshot.exists()){
                    displayMessages(dataSnapshot)
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })
    }

    private fun init() {
        imgBack.visible()
        currentGroupName = intent.getStringExtra(CommonUtils.GROUP_NAME)
        setSupportActionBar(groupChatBarLayout as Toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        tvTitle.text = currentGroupName

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth?.currentUser?.uid!!
        usersRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.USERS_DB_REF)
        groupNameRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.GROUPS).child(currentGroupName)

    }

    private fun listeners() {
        btnSendMessageGroupChat.setOnClickListener {
            saveMessageInfoToDatabase()
            scrollViewGroupChat.fullScroll((ScrollView.FOCUS_DOWN))
            etInputGroupMessage.setText("")
        }

        imgBack.setOnClickListener {
            CommonFunction.hideKeyboard(this@GroupChatActivity)
            onBackPressed()
        }
    }

    private fun getUserInfo() {
        usersRef?.child(currentUserId)?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    currentUserName = snapshot.child(CommonUtils.NAME).getValue().toString()
                }
            }
        })
    }

    private fun displayMessages(dataSnapshot: DataSnapshot) {
        val iterator = dataSnapshot.children.iterator()
        while (iterator.hasNext()){
            val chatDate = iterator.next().value.toString()
            val chatMessage = iterator.next().value.toString()
            val chatName = iterator.next().value.toString()
            val chatTime = iterator.next().value.toString()

            tvGroupChatTextDisplay.append(chatName+":\n"+chatMessage+"\n"+chatTime+"     "+chatDate+"\n\n\n")

            scrollViewGroupChat.fullScroll((ScrollView.FOCUS_DOWN))
        }
    }

    private fun saveMessageInfoToDatabase() {
        val message = etInputGroupMessage.text.toString()
        val messageKey = groupNameRef?.push()?.key
        if(TextUtils.isEmpty(message)){
            Toast.makeText(this@GroupChatActivity,"Please enter message...",Toast.LENGTH_LONG).show()
        }else{
            val calForDate = Calendar.getInstance()
            val currentDateFormat = SimpleDateFormat(CommonUtils.SIMPLE_DATE_FORMAT)
            currentDate = currentDateFormat.format(calForDate.time)

            val calForTime = Calendar.getInstance()
            val currentTimeFormat = SimpleDateFormat(CommonUtils.SIMPLE_TIME_FORMAT)
            currentTime = currentTimeFormat.format(calForTime.time)

            val groupMessageKey = HashMap<String,Any>()
            groupNameRef?.updateChildren(groupMessageKey)

            groupMessageKeyRef = groupNameRef?.child(messageKey!!)

            val messageInfoMap = HashMap<String,Any>()
            messageInfoMap.put(CommonUtils.NAME,currentUserName)
            messageInfoMap.put(CommonUtils.MESSAGE,message)
            messageInfoMap.put(CommonUtils.DATE,currentDate)
            messageInfoMap.put(CommonUtils.TIME,currentTime)

            groupMessageKeyRef?.updateChildren(messageInfoMap)

        }
    }
}
