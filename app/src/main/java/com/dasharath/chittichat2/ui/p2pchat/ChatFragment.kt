package com.dasharath.chittichat2.ui.p2pchat


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.models.ContactsModel
import com.dasharath.chittichat2.ui.findfriends.FindFriendsActivity
import com.dasharath.chittichat2.utils.CommonUtils
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_chats.view.*
import kotlinx.android.synthetic.main.fragment_chats.view.aviLoading
import kotlinx.android.synthetic.main.item_user_request_display.view.*
import com.dasharath.chittichat2.utils.CommonFunction
import kotlinx.android.synthetic.main.fragment_chats.*
import kotlinx.android.synthetic.main.layout_no_internet.*


class ChatFragment : Fragment() {

    private var chatRef: DatabaseReference? = null
    private var userRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var currentUserId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_chats, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.rvChatsFragment.layoutManager = LinearLayoutManager(context)
        init()
        if (CommonFunction.isOnline(context!!)) {
            relativeNoIternet.isVisible = false
            rvChatsFragment.isVisible = true
            setAdapter(view)
        } else {
            view.aviLoading.hide()
            CommonFunction.showSnackBar(view, activity!!)
            relativeNoIternet.isVisible = true
            rvChatsFragment.isVisible = false
        }
        onClick()
    }

    private fun onClick() {
        view?.btnFindNewFriends?.setOnClickListener {
            if(CommonFunction.isOnline(context!!)) {
                startActivity(Intent(activity, FindFriendsActivity::class.java))
            } else {
                CommonFunction.showSnackBar(view!!,activity!!)
            }
        }
    }

    private fun init() {
        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth?.currentUser?.uid!!
        chatRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.CONTACTS)
            .child(currentUserId)
        userRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.USERS_DB_REF)
    }

    private fun setAdapter(view: View) {

        checkRecordsFoundOrNot(view)

        val option = FirebaseRecyclerOptions.Builder<ContactsModel>()
            .setQuery(chatRef!!, ContactsModel::class.java).build()

        val adapter = object : FirebaseRecyclerAdapter<ContactsModel, ChatsViewHolder>(option) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
                val viewItem = LayoutInflater.from(context)
                    .inflate(R.layout.item_user_request_display, parent, false)
                return ChatsViewHolder(viewItem)
            }

            override fun onBindViewHolder(
                holder: ChatsViewHolder,
                position: Int,
                model: ContactsModel
            ) {
                val userId = getRef(position).key
                dataChangeListener(userId, holder, view)
            }
        }

        view.rvChatsFragment.adapter = adapter
        adapter.startListening()

    }

    private fun checkRecordsFoundOrNot(view: View) {
        chatRef?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshots: DataSnapshot) {
                if (!dataSnapshots.exists()) {
                    view.aviLoading.hide()
                    view.btnFindNewFriends.isVisible = true
                } else {
                    if (view.btnFindNewFriends.isVisible)
                        view.btnFindNewFriends.isVisible = false
                }
            }

        })
    }

    private fun dataChangeListener(
        userId: String?,
        holder: ChatsViewHolder,
        view: View
    ) {
        userRef?.child(userId!!)?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {

                    val name = dataSnapshot.child(CommonUtils.NAME).value.toString()
                    var image = ""

                    if (dataSnapshot.hasChild(CommonUtils.IMAGE)) {
                        image = dataSnapshot.child(CommonUtils.IMAGE).value.toString()
                    }

                    val rootRef = FirebaseDatabase.getInstance().reference

                    rootRef.child(CommonUtils.MESSAGES).child(currentUserId).child(userId)
                        .addValueEventListener(
                            object : ValueEventListener {

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val iterator = snapshot.children.last()
                                        val type = iterator.child("type").value.toString()
                                        val status =
                                            iterator.child(CommonUtils.STATUS).value.toString()


                                        if (status == CommonUtils.SENT) {
                                            holder.userStatus?.setTextColor(Color.parseColor("#E4394F"))
                                            holder.tvNewMessage?.visibility = View.VISIBLE
                                        } else {
                                            holder.userStatus?.setTextColor(Color.parseColor("#3C3F41"))
                                            holder.tvNewMessage?.visibility = View.GONE
                                        }

                                        if (type == CommonUtils.IMAGE) {
                                            holder.imgPhotoMessage?.visibility = View.VISIBLE
                                            holder.userStatus?.text = "Photo"
                                        } else {
                                            holder.imgPhotoMessage?.visibility = View.GONE
                                            holder.userStatus?.text =
                                                iterator.child(CommonUtils.MESSAGE).value.toString()
                                        }
                                    }
                                }

                                override fun onCancelled(p0: DatabaseError) {

                                }

                            })

                    holder.userName?.text = name


                    if (context != null) {
                        Glide.with(context!!).load(image)
                            .placeholder(R.drawable.profile_image)
                            .into(holder.profile!!)
                    }

                    holder.itemView.setOnClickListener {
                        startActivity(
                            Intent(
                                context,
                                ChatActivity::class.java
                            )
                                .putExtra(CommonUtils.UID, userId)
                                .putExtra(CommonUtils.NAME, name)
                                .putExtra(CommonUtils.IMAGE, image)
                        )
                    }

                    holder.profile?.setOnClickListener {
                        CommonFunction.showImage(image, context!!, true)
                    }

                    view.aviLoading.hide()
                }
            }

        })
    }

    class ChatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userName: TextView? = null
        var userStatus: TextView? = null
        var tvNewMessage: TextView? = null
        var imgPhotoMessage: ImageView? = null
        var profile: CircleImageView? = null

        init {
            userName = itemView.tvRequestProfileName
            userStatus = itemView.tvItemRequestStatus
            profile = itemView.imgItemUserRequestProfile
            tvNewMessage = itemView.tvNewMessage
            imgPhotoMessage = itemView.imgPhotoMessage
        }
    }
}


