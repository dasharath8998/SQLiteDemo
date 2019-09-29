package com.dasharath.chittichat2.ui.chatactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.adapter.MessageAdapter
import com.dasharath.chittichat2.ui.findfriends.Messages
import com.dasharath.chittichat2.utils.CommonUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {

    private var messageReceiverId: String = ""
    private var messageReceiverName: String = ""
    private var messageReceiverImage: String = ""
    private var messageSenderId: String = ""
    private var tvUserName: TextView? = null
    private var tvLastSeen: TextView? = null
    private var imgProfile: CircleImageView? = null

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
        Toast.makeText(this@ChatActivity,messageReceiverId,Toast.LENGTH_LONG).show()
        Toast.makeText(this@ChatActivity,messageReceiverName,Toast.LENGTH_LONG).show()
    }

    private fun init() {
        messageReceiverId = intent.getStringExtra(CommonUtils.UID)
        messageReceiverName = intent.getStringExtra(CommonUtils.NAME)
        messageReceiverImage = intent.getStringExtra(CommonUtils.IMAGE)

        setSupportActionBar(appBarChat as Toolbar)
        val actionBar = supportActionBar

        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        val view = layoutInflater.inflate(R.layout.item_chat_bar, null)
        (appBarChat as Toolbar).addView(view)

        tvUserName = findViewById<TextView>(R.id.tvItemProfileName)
        tvLastSeen = findViewById<TextView>(R.id.tvItemUserLastSeen)
        imgProfile = findViewById<CircleImageView>(R.id.imgItemChatProfile)

        mAuth = FirebaseAuth.getInstance()
        messageSenderId = mAuth?.currentUser?.uid!!
        rootRef = FirebaseDatabase.getInstance().reference
        rvPrivateChatMessagesList.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(messagesList!!,this@ChatActivity)
        rvPrivateChatMessagesList.adapter = messageAdapter

        setAdapter()
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

            val messagePushId = userMessageKeyRef?.key
            val messageTextBody:HashMap<String,Any> = HashMap()
            messageTextBody[CommonUtils.MESSAGE] = messageTexr
            messageTextBody["type"] = "text"
            messageTextBody["from"] = messageSenderId

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
}
