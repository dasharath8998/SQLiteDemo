package com.dasharath.chittichat2.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.models.Messages
import com.dasharath.chittichat2.utils.CommonFunction
import com.dasharath.chittichat2.utils.CommonUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.item_messages_layout.view.*

class MessageAdapter(var userMessageList:List<Messages>, var context:Context): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private var mAuth: FirebaseAuth? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_messages_layout,parent,false)
        mAuth = FirebaseAuth.getInstance()
        return MessageViewHolder(view)
    }

    override fun getItemCount(): Int = userMessageList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val messageSenderId = mAuth?.currentUser?.uid!!
        val messages = userMessageList[position]

        val fromUserId = messages.from
        val fromMessageType = messages.type

        holder.tvReceiverMessageText?.visibility = View.GONE
        holder.tvSenderMessageText?.visibility = View.GONE
        holder.imgMessageSenderPicture?.visibility = View.GONE
        holder.imgMessageReceiverPicture?.visibility = View.GONE
        holder.tvReceiverDateTime?.visibility = View.GONE
        holder.tvSenderDateTime?.visibility = View.GONE

        if(fromMessageType == "text"){

            if(fromUserId == messageSenderId){
                holder.tvSenderMessageText?.visibility = View.VISIBLE
                holder.tvSenderDateTime?.visibility = View.VISIBLE
                holder.tvSenderMessageText?.setBackgroundResource(R.drawable.sender_messages_layout)
                holder.tvSenderMessageText?.setTextColor(Color.BLACK)
                holder.tvSenderMessageText?.text = messages.message
                holder.tvSenderDateTime?.text = messages.time +" - "+ messages.date
            }else{
                holder.tvReceiverMessageText?.visibility = View.VISIBLE
                holder.tvReceiverDateTime?.visibility = View.VISIBLE

                holder.tvReceiverMessageText?.setBackgroundResource(R.drawable.receiver_messages_layout)
                holder.tvReceiverMessageText?.setTextColor(Color.BLACK)
                holder.tvReceiverMessageText?.text = messages.message
                holder.tvReceiverDateTime?.text = messages.time +" - "+ messages.date
            }
        }else if(fromMessageType == CommonUtils.IMAGE){
            if (fromUserId == messageSenderId){
                holder.imgMessageSenderPicture?.visibility = View.VISIBLE
                Glide.with(context).load(messages.message).into(holder.imgMessageSenderPicture!!)

                holder.imgMessageSenderPicture?.setOnClickListener {
                    CommonFunction.showImage(messages.message,context,false)
                }
            } else {
                holder.imgMessageReceiverPicture?.visibility = View.VISIBLE
                Glide.with(context).load(messages.message).into(holder.imgMessageReceiverPicture!!)

                holder.imgMessageReceiverPicture?.setOnClickListener {
                    CommonFunction.showImage(messages.message,context,false)
                }
            }
        }
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvSenderMessageText: TextView? = null
        var tvReceiverMessageText: TextView? = null
        var tvReceiverDateTime: TextView? = null
        var tvSenderDateTime: TextView? = null
        var imgMessageSenderPicture: ImageView? = null
        var imgMessageReceiverPicture: ImageView? = null

        init {

            tvSenderMessageText = itemView.tvSenderMessageText
            tvReceiverMessageText = itemView.tvReceiverMessageText
            tvReceiverDateTime = itemView.tvTimeDateReceiver
            tvSenderDateTime = itemView.tvTimeDateSender
            imgMessageSenderPicture = itemView.imgMessageSenderImage
            imgMessageReceiverPicture = itemView.imgMessageReceiverImage

        }
    }
}