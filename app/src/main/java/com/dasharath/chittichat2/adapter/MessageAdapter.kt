package com.dasharath.chittichat2.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.ui.findfriends.Messages
import com.dasharath.chittichat2.utils.CommonUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_messages_layout.view.*

class MessageAdapter(var userMessageList:List<Messages>,var context:Context): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private var mAuth: FirebaseAuth? = null
    private var usersRef: DatabaseReference? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_messages_layout,parent,false)
        mAuth = FirebaseAuth.getInstance()
        return MessageViewHolder(view)
    }

    override fun getItemCount(): Int = userMessageList.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val messageSenderId = mAuth?.currentUser?.uid!!
        val messages = userMessageList[position]

        val fromUserId = messages.from
        val fromMessageType = messages.type
        usersRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.USERS_DB_REF).child(fromUserId)
        usersRef?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var receiverImage = ""
                if(dataSnapshot.hasChild(CommonUtils.IMAGE)){
                    receiverImage = dataSnapshot.child(CommonUtils.IMAGE).value.toString()
                }
                Glide.with(context).load(receiverImage).placeholder(R.drawable.profile_image).into(holder.imgMessages!!)
            }
        })
        if(fromMessageType == "text"){
            holder.tvReceiverMessageText?.visibility = View.GONE
            holder.tvSenderMessageText?.visibility = View.GONE
            holder.imgMessages?.visibility = View.GONE

            if(fromUserId == messageSenderId){
                holder.tvSenderMessageText?.visibility = View.VISIBLE
                holder.tvSenderMessageText?.setBackgroundResource(R.drawable.sender_messages_layout)
                holder.tvSenderMessageText?.setTextColor(Color.BLACK)
                holder.tvSenderMessageText?.text = messages.message
            }else{
                holder.imgMessages?.visibility = View.VISIBLE
                holder.tvReceiverMessageText?.visibility = View.VISIBLE

                holder.tvReceiverMessageText?.setBackgroundResource(R.drawable.receiver_messages_layout)
                holder.tvReceiverMessageText?.setTextColor(Color.BLACK)
                holder.tvReceiverMessageText?.text = messages.message
            }
        }
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvSenderMessageText: TextView? = null
        var tvReceiverMessageText: TextView? = null
        var imgMessages: CircleImageView? = null

        init {
            tvSenderMessageText = itemView.tvSenderMessageText
            tvReceiverMessageText = itemView.tvReceiverMessageText
            imgMessages = itemView.imgItemMessageProfile
        }
    }
}