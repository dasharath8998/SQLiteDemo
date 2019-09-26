package com.dasharath.chittichat2.ui.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.utils.CommonUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var userRef: DatabaseReference? = null
    private var chatReqRef: DatabaseReference? = null
    private var contactRef: DatabaseReference? = null

    private var receverUserId: String = ""
    private var currentStat: String = ""
    private var senderUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        userRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.USERS_DB_REF)
        chatReqRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.CHAT_REQUEST)
        contactRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.CONTACTS)
        mAuth = FirebaseAuth.getInstance()

        receverUserId = intent.getStringExtra(CommonUtils.UID)
        currentStat = "new"
        senderUserId = mAuth?.currentUser?.uid.toString()

        retriveUserInfo()
    }

    private fun retriveUserInfo() {
        userRef?.child(receverUserId)?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.child(CommonUtils.NAME).value.toString()
                val status = dataSnapshot.child(CommonUtils.STATUS).value.toString()
                tvVisitProfileUsername.text = name
                tvVisitProfileStatus.text = status
                if(dataSnapshot.exists() && dataSnapshot.hasChild(CommonUtils.IMAGE)){
                    val imageUrl = dataSnapshot.child(CommonUtils.IMAGE).value.toString()
                    Glide.with(this@ProfileActivity).load(imageUrl).placeholder(R.drawable.profile_image).into(imgVisitProfile)
                }
                manageChatRequest()
            }

        })
    }

    private fun manageChatRequest() {

        chatReqRef?.child(senderUserId)?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.hasChild(receverUserId)){
                    val requestType = dataSnapshot.child(receverUserId).child(CommonUtils.REQUEST_TYPE).value.toString()
                    if(requestType.equals(CommonUtils.SENT)){
                        currentStat = "request_sent"
                        btnSendMessageRequest.text = "Cancel chat request"
                    }else if(requestType.equals(CommonUtils.RECEIVED)){
                        currentStat = "request_received"
                        btnSendMessageRequest.text = "Accept chat request"

                        btnDeclineMessageRequest.visibility = View.VISIBLE
                        btnDeclineMessageRequest.isEnabled = true

                        btnDeclineMessageRequest.setOnClickListener {
                            cancleChatRequest()
                        }
                    }
                }else{
                    contactRef?.child(senderUserId)?.addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.hasChild(receverUserId)){
                                currentStat = "friends"
                                btnSendMessageRequest.setText("Remove this contact")
                            }
                        }

                    })
                }
            }
        })

        if(!senderUserId.equals(receverUserId)){

            btnSendMessageRequest.setOnClickListener {
                btnSendMessageRequest.isEnabled = false

                if(currentStat.equals("new")){
                    sendChatRequest()
                }
                if(currentStat.equals("request_sent")){
                    cancleChatRequest()
                }
                if(currentStat.equals("request_received")){
                    acceptChatRequest()
                }
                if(currentStat.equals("friends")){
                    removeSpecificContact()
                }
            }

        }else{
            btnSendMessageRequest.visibility = View.GONE
        }
    }

    private fun removeSpecificContact() {
        contactRef?.child(senderUserId)?.child(receverUserId)?.removeValue()?.addOnCompleteListener {
            if(it.isSuccessful){
                contactRef?.child(receverUserId)?.child(senderUserId)?.removeValue()?.addOnCompleteListener {
                    if(it.isSuccessful){
                        if(it.isSuccessful){
                            btnSendMessageRequest.isEnabled = true
                            currentStat = "new"
                            btnSendMessageRequest.text = "Send Message"
                            btnDeclineMessageRequest.visibility = View.GONE
                            btnDeclineMessageRequest.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun acceptChatRequest() {
        contactRef?.child(senderUserId)?.child(receverUserId)?.child(CommonUtils.CONTACTS)?.setValue(CommonUtils.SAVED)?.addOnCompleteListener {
            if(it.isSuccessful){
                contactRef?.child(receverUserId)?.child(senderUserId)?.child(CommonUtils.CONTACTS)?.setValue(CommonUtils.SAVED)?.addOnCompleteListener {
                    if(it.isSuccessful){
                        chatReqRef?.child(senderUserId)?.child(receverUserId)?.removeValue()?.addOnCompleteListener {
                            if(it.isSuccessful){
                                chatReqRef?.child(receverUserId)?.child(senderUserId)?.removeValue()?.addOnCompleteListener {
                                    if(it.isSuccessful){
                                        btnSendMessageRequest.isEnabled = true
                                        currentStat = "friends"
                                        btnSendMessageRequest.text = "Remove this contact"
                                        btnDeclineMessageRequest.visibility = View.GONE
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun cancleChatRequest() {
        chatReqRef?.child(senderUserId)?.child(receverUserId)?.removeValue()?.addOnCompleteListener {
            if(it.isSuccessful){
                chatReqRef?.child(receverUserId)?.child(senderUserId)?.removeValue()?.addOnCompleteListener {
                    if(it.isSuccessful){
                        if(it.isSuccessful){
                            btnSendMessageRequest.isEnabled = true
                            currentStat = "new"
                            btnSendMessageRequest.text = "Send Message"
                            btnDeclineMessageRequest.visibility = View.GONE
                            btnDeclineMessageRequest.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun sendChatRequest() {
        chatReqRef?.child(senderUserId)?.child(receverUserId)?.child(CommonUtils.REQUEST_TYPE)?.setValue(CommonUtils.SENT)?.addOnCompleteListener {
            if(it.isSuccessful){
                chatReqRef?.child(receverUserId)?.child(senderUserId)?.child(CommonUtils.REQUEST_TYPE)?.setValue(CommonUtils.RECEIVED)?.addOnCompleteListener {
                    if(it.isSuccessful){
                        btnSendMessageRequest.isEnabled = true
                        currentStat = "request_sent"
                        btnSendMessageRequest.text = "Cancel chat request"
                    }
                }
            }
        }
    }
}
