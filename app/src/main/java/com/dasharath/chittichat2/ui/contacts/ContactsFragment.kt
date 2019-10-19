package com.dasharath.chittichat2.ui.contacts


import android.content.Intent
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
import com.dasharath.chittichat2.ui.profile.ProfileActivity
import com.dasharath.chittichat2.utils.CommonFunction
import com.dasharath.chittichat2.utils.CommonUtils
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_contacts.*
import kotlinx.android.synthetic.main.fragment_contacts.view.*
import kotlinx.android.synthetic.main.fragment_contacts.view.rvContactList
import kotlinx.android.synthetic.main.item_user_request_display.view.*
import kotlinx.android.synthetic.main.layout_no_internet.*

class ContactsFragment : Fragment() {

    private var mAuth: FirebaseAuth? = null
    private var currentUserId: String = ""
    private var contactRef: DatabaseReference? = null
    private var usersRef: DatabaseReference? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(this.isVisible) {
            view.rvContactList.layoutManager = LinearLayoutManager(context)
            init()
            onClick()
            if(CommonFunction.isOnline(context!!)) {
                relativeNoIternet.isVisible = false
                rvContactList.isVisible = true
                view.aviLoadingContacts.show()
                setAdapter(view)
            } else {
                view.aviLoadingContacts.hide()
                relativeNoIternet.isVisible = true
                rvContactList.isVisible = false
                CommonFunction.showSnackBar(view,activity!!)
            }
        }
    }

    private fun init() {
        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth?.currentUser?.uid!!
        contactRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.CONTACTS)
            .child(currentUserId)
        usersRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.USERS_DB_REF)
    }

    private fun onClick() {
        view?.btnFindNewFriendsContacts?.setOnClickListener {
            if(CommonFunction.isOnline(context!!)) {
                startActivity(Intent(activity, FindFriendsActivity::class.java))
            } else {
                CommonFunction.showSnackBar(view!!,activity!!)
            }
        }
    }

    private fun setAdapter(view: View) {

        checkRecordsFoundOrNot(view)

        val option = FirebaseRecyclerOptions.Builder<ContactsModel>().setQuery(
            contactRef!!,
            ContactsModel::class.java
        ).build()

        val adapter =
            object : FirebaseRecyclerAdapter<ContactsModel, ContactViewHolder>(option) {

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): ContactViewHolder {
                    val viewItem = LayoutInflater.from(context)
                        .inflate(R.layout.item_user_request_display, parent, false)
                    return ContactViewHolder(viewItem)
                }

                override fun onBindViewHolder(
                    holder: ContactViewHolder,
                    position: Int,
                    model: ContactsModel
                ) {
                    val userId = getRef(position).key
                    usersRef?.child(userId!!)
                        ?.addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {

                            }

                            override fun onDataChange(dataSnapshot: DataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    if (dataSnapshot.child(CommonUtils.USER_STATE).hasChild(
                                            CommonUtils.STATE
                                        )
                                    ) {
                                        val state = dataSnapshot.child(CommonUtils.USER_STATE)
                                            .child(CommonUtils.STATE).value.toString()
                                        val time = dataSnapshot.child(CommonUtils.USER_STATE)
                                            .child(CommonUtils.TIME).value.toString()
                                        val date = dataSnapshot.child(CommonUtils.USER_STATE)
                                            .child(CommonUtils.DATE).value.toString()

                                        if (state == "online") {
                                            holder.imgOnlineStatus?.visibility = View.VISIBLE
                                        } else if (state == "offline") {
                                            holder.imgOnlineStatus?.visibility = View.GONE
                                        }

                                    } else {
                                        holder.imgOnlineStatus?.visibility = View.GONE
                                    }

                                    val name =
                                        dataSnapshot.child(CommonUtils.NAME).value.toString()
                                    val status =
                                        dataSnapshot.child(CommonUtils.STATUS).value.toString()
                                    var image = ""

                                    if (dataSnapshot.hasChild(CommonUtils.IMAGE)) {
                                        image = dataSnapshot.child(CommonUtils.IMAGE)
                                            .value.toString()
                                    }

                                    holder.userName?.text = name
                                    holder.userStatus?.text = status
                                    if (context != null) {
                                        Glide.with(context!!).load(image)
                                            .placeholder(R.drawable.profile_image)
                                            .into(holder.profile!!)
                                    }
                                    view.aviLoadingContacts.hide()
                                }

                            }

                        })
                    holder.itemView.setOnClickListener {
                        startActivity(
                            Intent(context!!, ProfileActivity::class.java).putExtra(
                                CommonUtils.UID,
                                getRef(position).key
                            )
                        )
                    }
                }
            }
        view.rvContactList.adapter = adapter
        adapter.startListening()
    }


    private fun checkRecordsFoundOrNot(view: View) {
        contactRef?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshots: DataSnapshot) {
                if (!dataSnapshots.exists()) {
                    view.aviLoadingContacts.hide()
                    view.btnFindNewFriendsContacts.isVisible = true
                } else {
                    if (view.btnFindNewFriendsContacts.isVisible)
                        view.btnFindNewFriendsContacts.isVisible = false
                }
            }

        })
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userName: TextView? = null
        var userStatus: TextView? = null
        var profile: CircleImageView? = null
        var imgOnlineStatus: ImageView? = null
        init {
            userName = itemView.tvRequestProfileName
            userStatus = itemView.tvItemRequestStatus
            profile = itemView.imgItemUserRequestProfile
            imgOnlineStatus = itemView.imgOnlineStatus!!
        }
    }
}
