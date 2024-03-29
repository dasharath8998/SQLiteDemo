package com.dasharath.chittichat2.ui.request


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.models.ContactsModel
import com.dasharath.chittichat2.ui.findfriends.FindFriendsActivity
import com.dasharath.chittichat2.utils.CommonFunction
import com.dasharath.chittichat2.utils.CommonUtils
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_contacts.view.*
import kotlinx.android.synthetic.main.fragment_request.*
import kotlinx.android.synthetic.main.fragment_request.view.*
import kotlinx.android.synthetic.main.item_user_request_display.*
import kotlinx.android.synthetic.main.item_user_request_display.view.*
import kotlinx.android.synthetic.main.layout_no_internet.*

class RequestFragment : Fragment() {

    private var chatRequestRef: DatabaseReference? = null
    private var contactRef: DatabaseReference? = null
    private var userReference: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var currentuserId = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.rvChatRequest.layoutManager = LinearLayoutManager(context)
        init()
        onClick()
        if(CommonFunction.isOnline(context!!)) {
            relativeNoIternet.isVisible = false
            rvChatRequest.isVisible = true
            view.aviLoadingRequest.show()
            setAdapter(view)
        } else {
            view.aviLoadingRequest.hide()
            relativeNoIternet.isVisible = true
            rvChatRequest.isVisible = false
            CommonFunction.showSnackBar(view,activity!!)
        }
        setAdapter(view)
    }

    private fun onClick() {
        view?.btnFindNewFriendsRequest?.setOnClickListener {
            if(CommonFunction.isOnline(context!!)) {
                startActivity(Intent(activity, FindFriendsActivity::class.java))
            } else {
                CommonFunction.showSnackBar(view!!,activity!!)
            }
        }
    }

    private fun init() {
        chatRequestRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.CHAT_REQUEST)
        contactRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.CONTACTS)
        userReference = FirebaseDatabase.getInstance().reference.child(CommonUtils.USERS_DB_REF)
        mAuth = FirebaseAuth.getInstance()
        currentuserId = mAuth?.currentUser?.uid!!
    }

    private fun setAdapter(view: View) {

        checkRecordsFoundOrNot(view)

        val option = FirebaseRecyclerOptions.Builder<ContactsModel>()
            .setQuery(chatRequestRef?.child(currentuserId)!!, ContactsModel::class.java).build()

        val adapter = object : FirebaseRecyclerAdapter<ContactsModel, RequestViewHolder>(option) {

            override fun onBindViewHolder(
                holder: RequestViewHolder,
                position: Int,
                model: ContactsModel
            ) {

                val listUserId = getRef(position).getKey()
                val getTypeRef = getRef(position).child(CommonUtils.REQUEST_TYPE).ref


                getTypeRef.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val statusDB = dataSnapshot.exists()
                        if (statusDB) {
                            val type = dataSnapshot.value.toString()
                            if (type.equals(CommonUtils.RECEIVED)) {

                                userReference?.child(listUserId!!)
                                    ?.addValueEventListener(object : ValueEventListener {
                                        override fun onCancelled(p0: DatabaseError) {

                                        }

                                        @SuppressLint("SetTextI18n")
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                view.aviLoadingRequest.hide()
                                                val name = snapshot.child(CommonUtils.NAME)
                                                    .value.toString()
    //                                            val status =
    //                                                snapshot.child(CommonUtils.STATUS).value.toString()
                                                var image = ""
                                                if (snapshot.hasChild(CommonUtils.IMAGE)) {
                                                    image =
                                                        snapshot.child(CommonUtils.IMAGE)
                                                            .value.toString()
                                                }
                                                if (context != null) {
                                                    Glide.with(context!!).load(image)
                                                        .placeholder(R.drawable.profile_image)
                                                        .error(R.drawable.profile_image)
                                                        .into(holder.profile!!)
                                                }

                                                holder.profile?.setOnClickListener {
                                                    CommonFunction.showImage(image, context!!, true)
                                                }
                                                holder.userName?.text = name
                                                holder.userStatus?.setText("You have request Accept or Cancel")

                                                holder.itemView.setOnClickListener {
                                                    val itemOption: Array<CharSequence> =
                                                        arrayOf<CharSequence>(
                                                            "Accept",
                                                            "Delete",
                                                            "Cancel"
                                                        )

                                                    val builder = AlertDialog.Builder(context!!)
                                                    builder.setTitle("Chat Request From $name")
                                                    builder.setItems(itemOption) { dialog, which ->
                                                        if (which == 0) {
                                                            view.aviLoadingRequest.show()
                                                            contactRef?.child(currentuserId)
                                                                ?.child(listUserId)
                                                                ?.child(CommonUtils.CONTACTS)
                                                                ?.setValue("Saved")
                                                                ?.addOnCompleteListener {
                                                                    if (it.isSuccessful) {
                                                                        contactRef?.child(listUserId)
                                                                            ?.child(currentuserId)
                                                                            ?.child(CommonUtils.CONTACTS)
                                                                            ?.setValue("Saved")
                                                                            ?.addOnCompleteListener {
                                                                                if (it.isSuccessful) {
                                                                                    chatRequestRef?.child(
                                                                                        currentuserId
                                                                                    )?.child(
                                                                                        listUserId
                                                                                    )?.removeValue()
                                                                                        ?.addOnCompleteListener {
                                                                                            if (it.isSuccessful) {
                                                                                                chatRequestRef?.child(
                                                                                                    listUserId
                                                                                                )
                                                                                                    ?.child(
                                                                                                        currentuserId
                                                                                                    )
                                                                                                    ?.removeValue()
                                                                                                    ?.addOnCompleteListener {
                                                                                                        if (it.isSuccessful) {
                                                                                                            view.aviLoadingRequest.hide()
                                                                                                            Toast.makeText(
                                                                                                                context,
                                                                                                                "Contact Saved",
                                                                                                                Toast.LENGTH_LONG
                                                                                                            )
                                                                                                                .show()
                                                                                                        }
                                                                                                    }
                                                                                            }
                                                                                        }
                                                                                }
                                                                            }
                                                                    }
                                                                }
                                                        }
                                                        if (which == 1) {
                                                            view.aviLoadingRequest.show()
                                                            chatRequestRef?.child(currentuserId)
                                                                ?.child(listUserId)?.removeValue()
                                                                ?.addOnCompleteListener {
                                                                    if (it.isSuccessful) {
                                                                        chatRequestRef?.child(
                                                                            listUserId
                                                                        )?.child(currentuserId)
                                                                            ?.removeValue()
                                                                            ?.addOnCompleteListener {
                                                                                if (it.isSuccessful) {
                                                                                    view.aviLoadingRequest.hide()
                                                                                    Toast.makeText(
                                                                                        context,
                                                                                        "Request canceled",
                                                                                        Toast.LENGTH_LONG
                                                                                    ).show()
                                                                                }
                                                                            }
                                                                    }
                                                                }
                                                        }
                                                        if (which == 2) {
                                                            dialog.dismiss()
                                                        }
                                                    }
                                                    builder.show()
                                                }
                                            }
                                        }
                                    })
                            } else if (type.equals(CommonUtils.SENT)) {
                                view.aviLoadingRequest.hide()

                                userReference?.child(listUserId!!)
                                    ?.addValueEventListener(object : ValueEventListener {
                                        override fun onCancelled(p0: DatabaseError) {

                                        }

                                        @SuppressLint("SetTextI18n")
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val name =
                                                snapshot.child(CommonUtils.NAME).value.toString()
                                            var image = ""
                                            if (snapshot.hasChild(CommonUtils.IMAGE)) {
                                                image =
                                                    snapshot.child(CommonUtils.IMAGE)
                                                        .value.toString()
                                            }

                                            if (context != null) {
                                                Glide.with(context!!).load(image)
                                                    .placeholder(R.drawable.profile_image)
                                                    .error(R.drawable.profile_image)
                                                    .into(holder.profile!!)
                                            }

                                            holder.profile?.setOnClickListener {
                                                CommonFunction.showImage(image, context!!, true)
                                            }

                                            holder.userName?.text = name
                                            holder.userStatus?.setText("You have sent request to $name")

                                            holder.itemView.setOnClickListener {
                                                val itemOption: Array<CharSequence> =
                                                    arrayOf<CharSequence>("Delete", "Cancel")

                                                val builder = AlertDialog.Builder(context!!)
                                                builder.setTitle("You sent request $name")
                                                builder.setItems(itemOption) { dialog, which ->
                                                    if (which == 0) {
                                                        view.aviLoadingRequest.show()
                                                        chatRequestRef?.child(currentuserId)
                                                            ?.child(listUserId)?.removeValue()
                                                            ?.addOnCompleteListener {
                                                                if (it.isSuccessful) {
                                                                    chatRequestRef?.child(listUserId)
                                                                        ?.child(currentuserId)
                                                                        ?.removeValue()
                                                                        ?.addOnCompleteListener {
                                                                            if (it.isSuccessful) {
                                                                                view.aviLoadingRequest.hide()
                                                                                Toast.makeText(
                                                                                    context,
                                                                                    "Request Canceled",
                                                                                    Toast.LENGTH_LONG
                                                                                ).show()
                                                                            }
                                                                        }
                                                                }
                                                            }
                                                    }
                                                    if (which == 1) {
                                                        dialog.dismiss()
                                                    }
                                                }
                                                builder.show()
                                            }
                                        }
                                    })
                            }
                        }
                    }
                })
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
                var view = LayoutInflater.from(context!!)
                    .inflate(R.layout.item_user_request_display, parent, false)
                return RequestViewHolder(view)
            }
        }
        view.rvChatRequest.adapter = adapter
        adapter.startListening()
    }

    private fun checkRecordsFoundOrNot(view: View) {
        chatRequestRef?.child(currentuserId)!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshots: DataSnapshot) {
                if (!dataSnapshots.exists()) {
                    view.aviLoadingRequest.hide()
                    view.btnFindNewFriendsRequest.isVisible = true
                } else {
                    if (view.btnFindNewFriendsRequest.isVisible)
                        view.btnFindNewFriendsRequest.isVisible = false
                }
            }

        })
    }

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
