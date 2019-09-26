package com.dasharath.chittichat2.ui.contacts


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dasharath.chittichat2.BR
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.databinding.ItemUserDisplayBinding
import com.dasharath.chittichat2.ui.findfriends.ContactsModel
import com.dasharath.chittichat2.utils.CommonUtils
import com.github.nitrico.lastadapter.LastAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_find_friends.*
import kotlinx.android.synthetic.main.fragment_contacts.*
import kotlinx.android.synthetic.main.fragment_contacts.view.*
import kotlinx.android.synthetic.main.fragment_contacts.view.rvContactList

class ContactsFragment : Fragment() {

    private var contactRef: DatabaseReference? = null
    private var userRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var currentUserId: String = ""
    private val contactList: ArrayList<String> = ArrayList()
    private val userList = ArrayList<ContactsModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contactRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.CONTACTS).child(currentUserId)
        userRef = FirebaseDatabase.getInstance().reference.child(CommonUtils.USERS_DB_REF)
        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth?.currentUser?.uid!!.toString()
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.rvContactList.layoutManager = LinearLayoutManager(context)
        setAdapter(userList)
        getContactList()
    }

    private fun getContactList() {
        contactRef?.child(currentUserId)?.addValueEventListener(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                contactList.clear()
                for(sna in snapshot.children){
                    contactList.add(sna.key.toString())
                    Log.d("data",sna.key.toString())
                }
                getUserList(contactList)
            }
        })
    }

    private fun getUserList(contactList: java.util.ArrayList<String>) {
        contactList.forEachIndexed { index, userId ->
            userRef?.child(userId)?.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child(CommonUtils.NAME).value.toString()
                    val status = snapshot.child(CommonUtils.STATUS).value.toString()
                    var imageUrl = ""
                    var uid = snapshot.child(CommonUtils.UID).value.toString()
                    if(snapshot.exists() && snapshot.hasChild(CommonUtils.IMAGE)){
                        imageUrl = snapshot.child(CommonUtils.IMAGE).value.toString()
                    }

                    userList.add(ContactsModel(name,status,imageUrl,uid))
                    rvContactList.adapter?.notifyDataSetChanged()
                }
            })
        }
    }

    private fun setAdapter(contactList: ArrayList<ContactsModel>) {

        LastAdapter(userList, BR.contactModel).map<ContactsModel, ItemUserDisplayBinding>(R.layout.item_user_display) {
            onBind {
                Glide.with(context!!).load(userList[it.adapterPosition].image.toString()).placeholder(R.drawable.profile_image).into(it.binding.imgItemUserProfile)
            }
//            onClick {
//                val visitUserId = list[it.adapterPosition].uid
//                startActivity(
//                    Intent(context!!, ProfileActivity::class.java).putExtra(
//                        CommonUtils.UID,visitUserId))
//            }
        }.into(rvContactList)

    }
}
