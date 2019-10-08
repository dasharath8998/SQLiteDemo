package com.dasharath.chittichat2.ui.findfriends

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dasharath.chittichat2.BR
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.databinding.ItemUserDisplayBinding
import com.dasharath.chittichat2.ui.profile.ProfileActivity
import com.dasharath.chittichat2.utils.CommonFunction
import com.dasharath.chittichat2.utils.CommonFunction.visible
import com.dasharath.chittichat2.utils.CommonUtils
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.github.nitrico.lastadapter.LastAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_find_friends.*
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.item_user_request_display.view.*

class FindFriendsActivity : AppCompatActivity() {

    private var rootRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)
        aviLoadingFF.show()
        init()
        listeners()
        setAdapter()
    }

    private fun listeners() {
        imgBack.setOnClickListener {
            CommonFunction.hideKeyboard(this@FindFriendsActivity)
            onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        imgBack.visible()
        setSupportActionBar(appBarFindFriend as Toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        tvTitle.text = "Find Friends"
        rootRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.USERS_DB_REF)
        rvFindFriends.layoutManager = LinearLayoutManager(this)
    }

    private fun setAdapter() {
        val option = FirebaseRecyclerOptions.Builder<ContactsModel>()
            .setQuery(rootRef!!, ContactsModel::class.java).build()

        val adapter =
            object : FirebaseRecyclerAdapter<ContactsModel, FindFriendViewHolder>(option) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FindFriendViewHolder {
                    val view = LayoutInflater.from(this@FindFriendsActivity)
                        .inflate(R.layout.item_user_request_display, parent, false)
                    return FindFriendViewHolder(view)
                }

                override fun onBindViewHolder(holder: FindFriendViewHolder, position: Int, model: ContactsModel) {
                    holder.userName?.text = model.name
                    holder.userStatus?.text = model.status
                    Glide.with(this@FindFriendsActivity).load(model.image).placeholder(R.drawable.profile_image).error(R.drawable.profile_image).into(holder.profile!!)
                    holder.itemView.setOnClickListener {
                        startActivity(Intent(this@FindFriendsActivity,ProfileActivity::class.java).putExtra(CommonUtils.UID,getRef(position).key))
                    }
                    if(aviLoadingFF.isVisible) {
                        aviLoadingFF.hide()
                    }
                }
            }
        rvFindFriends.adapter = adapter
        adapter.startListening()
    }

    class FindFriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
