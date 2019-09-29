package com.dasharath.chittichat2.ui.chats


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.ui.chatactivity.ChatActivity
import com.dasharath.chittichat2.ui.findfriends.ContactsModel
import com.dasharath.chittichat2.utils.CommonUtils
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_chats.view.*
import kotlinx.android.synthetic.main.item_user_request_display.view.*

class ChatFragment : Fragment() {

    private var chatRef: DatabaseReference? = null
    private var userRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var currentUserId: String = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_chats, container, false)
        view.rvChatsFragment.layoutManager = LinearLayoutManager(context)
        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth?.currentUser?.uid!!
        chatRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.CONTACTS).child(currentUserId)
        userRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.USERS_DB_REF)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter(view)
    }

    private fun setAdapter(view: View) {
        val option = FirebaseRecyclerOptions.Builder<ContactsModel>()
            .setQuery(chatRef!!, ContactsModel::class.java).build()

        val adapter = object : FirebaseRecyclerAdapter<ContactsModel, ChatsViewHolder>(option) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_user_request_display, parent, false)
                return ChatsViewHolder(view)
            }

            override fun onBindViewHolder(holder: ChatsViewHolder, position: Int, model: ContactsModel) {

                val userId = getRef(position).key
                userRef?.child(userId!!)?.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    @SuppressLint("SetTextI18n")
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val name = dataSnapshot.child(CommonUtils.NAME).value.toString()
                            val status = dataSnapshot.child(CommonUtils.STATUS).value.toString()
                            var image = ""
                            if (dataSnapshot.hasChild(CommonUtils.IMAGE)) {
                                image = dataSnapshot.child(CommonUtils.IMAGE).value.toString()
                            }
                            holder.userName?.text = name
                            holder.userStatus?.text = "Last Seen: " + "\n" + "Date" + " Time"
                            Glide.with(context!!).load(image).placeholder(R.drawable.profile_image)
                                .into(holder.profile!!)

                            holder.itemView.setOnClickListener {
                                startActivity(Intent(context,ChatActivity::class.java)
                                    .putExtra(CommonUtils.UID,userId)
                                    .putExtra(CommonUtils.NAME,name)
                                    .putExtra(CommonUtils.IMAGE,image))
                            }
                        }
                    }

                })
            }
        }
        view.rvChatsFragment.adapter = adapter
        adapter.startListening()
    }

    class ChatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userName: TextView? = null
        var userStatus: TextView? = null
        var profile: CircleImageView? = null
        init {
            userName = itemView.tvRequestProfileName
            userStatus = itemView.tvItemRequestStatus
            profile = itemView.imgItemUserRequestProfile
        }
    }
}


