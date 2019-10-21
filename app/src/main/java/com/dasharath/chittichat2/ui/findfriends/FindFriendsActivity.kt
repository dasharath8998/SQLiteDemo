package com.dasharath.chittichat2.ui.findfriends

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.models.ContactsModel
import com.dasharath.chittichat2.ui.profile.ProfileActivity
import com.dasharath.chittichat2.utils.CommonFunction
import com.dasharath.chittichat2.utils.CommonFunction.visible
import com.dasharath.chittichat2.utils.CommonUtils
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_find_friends.*
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.item_user_request_display.view.*
import kotlinx.android.synthetic.main.layout_no_internet.*

class FindFriendsActivity : AppCompatActivity() {

    private var rootRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)
        init()
        listeners()
        if (CommonFunction.isOnline(this@FindFriendsActivity)) {
            aviLoadingFF.show()
            relativeNoIternet.isVisible = false
            rvFindFriends.isVisible = true
            setAdapter()
        } else {
            aviLoadingFF.hide()
            CommonFunction.showSnackBar(window.decorView.rootView, this@FindFriendsActivity)
            relativeNoIternet.isVisible = true
            rvFindFriends.isVisible = false
        }
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

                @SuppressLint("SetTextI18n")
                override fun onBindViewHolder(holder: FindFriendViewHolder, position: Int, model: ContactsModel) {
                    holder.userName?.text = model.name
                    holder.userStatus?.text = model.status
                    holder.tvCNC?.visibility = View.VISIBLE


                    holder.imgGender?.visibility = View.VISIBLE
                    if(model.gender == "Male"){
                        holder.imgGender?.setImageResource(R.drawable.ic_male)
                    } else {
                        holder.imgGender?.setImageResource(R.drawable.ic_female)
                    }
                    holder.imgGender?.setColorFilter(ContextCompat.getColor(this@FindFriendsActivity, R.color.colorPrimaryDark), android.graphics.PorterDuff.Mode.SRC_IN);

                    if(model.countryCode != "")
                        holder.tvCNC?.text = "(${model.countryCode})"

                    Glide.with(this@FindFriendsActivity).load(model.image).placeholder(R.drawable.profile_image).error(R.drawable.profile_image).into(holder.profile!!)
                    holder.itemView.setOnClickListener {
                        startActivity(Intent(this@FindFriendsActivity,ProfileActivity::class.java).putExtra(CommonUtils.UID,model.uid.toString()))
                    }
                    if(aviLoadingFF.isVisible) {
                        aviLoadingFF.hide()
                    }

                    holder.profile?.setOnClickListener {
                        CommonFunction.showImage(model.image,this@FindFriendsActivity,true)
                    }
                }
            }
        rvFindFriends.adapter = adapter
        adapter.startListening()
    }

    class FindFriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userName: TextView? = null
        var userStatus: TextView? = null
        var tvCNC: TextView? = null
        var profile: CircleImageView? = null
        var imgGender: ImageView? = null
        init {
            userName = itemView.tvRequestProfileName
            userStatus = itemView.tvItemRequestStatus
            tvCNC = itemView.tvCNC
            profile = itemView.imgItemUserRequestProfile
            imgGender = itemView.imgGender
        }
    }
}
