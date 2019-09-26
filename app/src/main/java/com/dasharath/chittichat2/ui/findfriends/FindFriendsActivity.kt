package com.dasharath.chittichat2.ui.findfriends

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dasharath.chittichat2.BR
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.databinding.ItemUserDisplayBinding
import com.dasharath.chittichat2.ui.profile.ProfileActivity
import com.dasharath.chittichat2.utils.CommonUtils
import com.github.nitrico.lastadapter.LastAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_find_friends.*

class FindFriendsActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var rootRef: DatabaseReference? = null
    private var cuurentUserId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)
        setSupportActionBar(appBarFindFriend as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Find Friends"

        mAuth = FirebaseAuth.getInstance()
        cuurentUserId = mAuth?.currentUser?.uid!!
        rootRef = FirebaseDatabase.getInstance().reference

        val userList = ArrayList<ContactsModel>()

        val database = rootRef?.child(CommonUtils.USERS_DB_REF)
        database?.addValueEventListener(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for(sna in snapshot.children){
                    var users = sna.getValue(ContactsModel::class.java)
                    userList.add(users!!)
                    Log.d("data",users.toString())
                }
                setAdapter(userList)
            }

        })
    }

    private fun setAdapter(list: ArrayList<ContactsModel>) {

        rvFindFriends.layoutManager = LinearLayoutManager(this@FindFriendsActivity)
        LastAdapter(list, BR.contactModel).map<ContactsModel, ItemUserDisplayBinding>(R.layout.item_user_display) {
            onBind {
                Glide.with(this@FindFriendsActivity).load(list[it.adapterPosition].image.toString()).placeholder(R.drawable.profile_image).into(it.binding.imgItemUserProfile)
            }
            onClick {
                val visitUserId = list[it.adapterPosition].uid
                startActivity(Intent(this@FindFriendsActivity,ProfileActivity::class.java).putExtra(CommonUtils.UID,visitUserId))
            }
        }.into(rvFindFriends)

    }
}
