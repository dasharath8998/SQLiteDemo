package com.dasharath.chittichat2.ui.p2pchat

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.adapter.MessageAdapter
import com.dasharath.chittichat2.models.Messages
import com.dasharath.chittichat2.utils.CommonFunction
import com.dasharath.chittichat2.utils.CommonUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chat.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {

    private var messageReceiverId: String = ""
    private var messageReceiverName: String = ""
    private var messageReceiverImage: String = ""
    private var saveCurrentDate: String = ""
    private var currentTime: String = ""
    private var myUrl: String = ""
    private var messageSenderId: String = ""
    private var checker: String = ""
    private var tvUserName: TextView? = null
    private var tvLastSeen: TextView? = null
    private var imgBackChat: ImageView? = null
    private var imgProfile: CircleImageView? = null
    private var loadingBar: ProgressDialog? = null

    private var mAuth:FirebaseAuth? = null
    private var rootRef:DatabaseReference? = null
    private val messagesList:ArrayList<Messages>? = ArrayList()
    private var messageAdapter:MessageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        init()

        tvUserName?.text = messageReceiverName
        Glide.with(this@ChatActivity).load(messageReceiverImage).placeholder(R.drawable.profile_image).into(imgProfile!!)

        btnSendPrivateChat.setOnClickListener {
            sendMessage()
        }

        btnSendFile.setOnClickListener {
            checker = CommonUtils.IMAGE
            val intent = Intent().setAction(Intent.ACTION_GET_CONTENT).setType("image/*")
            startActivityForResult(intent,123)
        }

        imgBackChat?.setOnClickListener {
            CommonFunction.hideKeyboard(this@ChatActivity)
            onBackPressed()
        }

        displayLastSeen()
    }

    private fun init() {

        messageReceiverId = intent.getStringExtra(CommonUtils.UID)
        messageReceiverName = intent.getStringExtra(CommonUtils.NAME)
        messageReceiverImage = intent.getStringExtra(CommonUtils.IMAGE)

        setSupportActionBar(appBarChat as Toolbar)

        val view = layoutInflater.inflate(R.layout.item_chat_bar, null)
        (appBarChat as Toolbar).addView(view)

        tvUserName = findViewById<TextView>(R.id.tvItemProfileName)
        tvLastSeen = findViewById<TextView>(R.id.tvItemUserLastSeen)
        imgBackChat = findViewById<ImageView>(R.id.imgBackChat)
        imgProfile = findViewById<CircleImageView>(R.id.imgItemChatProfile)

        mAuth = FirebaseAuth.getInstance()
        messageSenderId = mAuth?.currentUser?.uid!!
        rootRef = FirebaseDatabase.getInstance().reference
        loadingBar = ProgressDialog(this@ChatActivity)
        rvPrivateChatMessagesList.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(messagesList!!,this@ChatActivity)
        rvPrivateChatMessagesList.adapter = messageAdapter

        val cal = Calendar.getInstance()
        val currentDate = SimpleDateFormat("MMM dd, yyyy")
        saveCurrentDate = currentDate.format(cal.time)

        val currentTimeFormat = SimpleDateFormat("hh:mm a")
        currentTime = currentTimeFormat.format(cal.time)

        setAdapter()
    }

    private fun displayLastSeen(){
        rootRef?.child(CommonUtils.USERS_DB_REF)?.child(messageReceiverId)?.addValueEventListener(
            object : ValueEventListener {

                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.child(CommonUtils.USER_STATE).hasChild(CommonUtils.STATE)){
                        val state = dataSnapshot.child(CommonUtils.USER_STATE).child(CommonUtils.STATE).value.toString()
                        val time = dataSnapshot.child(CommonUtils.USER_STATE).child(CommonUtils.TIME).value.toString()
                        val date = dataSnapshot.child(CommonUtils.USER_STATE).child(CommonUtils.DATE).value.toString()

                        if(state == "online"){
                            tvLastSeen?.text = "Online"
                        } else if (state == "offline") {
                            tvLastSeen?.text = "$time $date"
                        }

                    } else {
                        tvLastSeen?.text = "Offline"
                    }
                }

            })
    }

    private fun setAdapter() {

        rootRef?.child(CommonUtils.MESSAGES)?.child(messageSenderId)?.child(messageReceiverId)
            ?.addChildEventListener(
                object : ChildEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onChildMoved(p0: DataSnapshot, p1: String?) {

                    }

                    override fun onChildChanged(p0: DataSnapshot, p1: String?) {

                    }

                    override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                        val messages = dataSnapshot.getValue(Messages::class.java)
                        messagesList?.add(messages!!)
                        messageAdapter?.notifyDataSetChanged()
                        rvPrivateChatMessagesList.smoothScrollToPosition(rvPrivateChatMessagesList.adapter?.itemCount!!)
                    }

                    override fun onChildRemoved(p0: DataSnapshot) {

                    }

                })
    }

    private fun sendMessage(){
        val messageTexr = etPrivateChatMessage.text.toString()
        if(TextUtils.isEmpty(messageTexr)){
            Toast.makeText(this@ChatActivity,"Please enter any message first...",Toast.LENGTH_LONG).show()
        }else{
            val messageSenderRef = "Messages/$messageSenderId/$messageReceiverId"
            val messageReceiverRef = "Messages/$messageReceiverId/$messageSenderId"

            val userMessageKeyRef = rootRef?.child(CommonUtils.MESSAGES)?.child(messageSenderId)?.child(messageReceiverId)?.push()

            val messagePushId = userMessageKeyRef?.key.toString()
            val messageTextBody:HashMap<String,Any> = HashMap()
            messageTextBody[CommonUtils.MESSAGE] = messageTexr
            messageTextBody["type"] = "text"
            messageTextBody["from"] = messageSenderId
            messageTextBody["to"] = messageReceiverId
            messageTextBody[CommonUtils.MESSAGE_ID] = messagePushId
            messageTextBody[CommonUtils.TIME] = currentTime
            messageTextBody[CommonUtils.DATE] = saveCurrentDate

            val messageBodyDetail:HashMap<String,Any> = HashMap()
            messageBodyDetail["$messageSenderRef/$messagePushId"] = messageTextBody
            messageBodyDetail["$messageReceiverRef/$messagePushId"] = messageTextBody

            rootRef?.updateChildren(messageBodyDetail)?.addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(this@ChatActivity,"Message Sent",Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this@ChatActivity,"Error",Toast.LENGTH_LONG).show()
                }
                etPrivateChatMessage.setText("")
            }

        }
    }

    private fun setProgress() {
        loadingBar?.setTitle("Sending Image")
        loadingBar?.setMessage("Please wait, we are sending your image...")
        loadingBar?.setCanceledOnTouchOutside(false)
        loadingBar?.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 123 && data != null && resultCode == Activity.RESULT_OK && data.data != null){

            setProgress()

            val uri = data.data!!

            if (checker != CommonUtils.IMAGE){

            } else if (checker == CommonUtils.IMAGE){
                val storageRef = FirebaseStorage.getInstance().reference.child(CommonUtils.IMAGE_FILES)

                val messageSenderRef = "Messages/$messageSenderId/$messageReceiverId"
                val messageReceiverRef = "Messages/$messageReceiverId/$messageSenderId"

                val userMessageKeyRef = rootRef?.child(CommonUtils.MESSAGES)?.child(messageSenderId)?.child(messageReceiverId)?.push()

                val messagePushId = userMessageKeyRef?.key.toString()

                val filePath = storageRef.child("$messagePushId.jpg")
                val uploadTask = filePath.putFile(uri!!)
                uploadTask.continueWithTask {
                    if(!it.isSuccessful){
                        throw it.exception!!
                    }
                    return@continueWithTask filePath.downloadUrl
                }.addOnCompleteListener {task ->
                    if(task.isSuccessful){
                        myUrl = task.result.toString()

                        val messageImageBody:HashMap<String,Any> = HashMap()
                        messageImageBody[CommonUtils.MESSAGE] = myUrl
                        messageImageBody[CommonUtils.NAME] = uri.lastPathSegment!!
                        messageImageBody["type"] = checker
                        messageImageBody["from"] = messageSenderId
                        messageImageBody["to"] = messageReceiverId
                        messageImageBody[CommonUtils.MESSAGE_ID] = messagePushId
                        messageImageBody[CommonUtils.TIME] = currentTime
                        messageImageBody[CommonUtils.DATE] = saveCurrentDate

                        val messageBodyDetail:HashMap<String,Any> = HashMap()
                        messageBodyDetail["$messageSenderRef/$messagePushId"] = messageImageBody
                        messageBodyDetail["$messageReceiverRef/$messagePushId"] = messageImageBody

                        rootRef?.updateChildren(messageBodyDetail)?.addOnCompleteListener {
                            if(it.isSuccessful){
                                loadingBar?.dismiss()
                                Toast.makeText(this@ChatActivity,"Message Sent",Toast.LENGTH_LONG).show()
                            }else{
                                loadingBar?.dismiss()
                                Toast.makeText(this@ChatActivity,"Error",Toast.LENGTH_LONG).show()
                            }
                            etPrivateChatMessage.setText("")
                        }
                    }
                }

            } else {
                loadingBar?.dismiss()
                Toast.makeText(this@ChatActivity,"Error",Toast.LENGTH_LONG).show()
            }

        }

    }
}
