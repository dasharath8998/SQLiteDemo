package com.dasharath.chittichat2.ui.groupchats


import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.utils.CommonUtils
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.fragment_groups.*

class GroupsFragment : AppCompatActivity() {

    private var arrayAdapter: ArrayAdapter<String>? = null
    private var listOfGroup = ArrayList<String>()
    private var groupReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_groups)
        aviLoadingGroups?.smoothToShow()

        groupReference = FirebaseDatabase.getInstance().reference.child("Groups")

        init()
        retriveAndDisplayGroups()
        listeners()
    }

    private fun listeners() {

        listGroup.setOnItemClickListener { adapterView, view, position, id ->
            val currentGroupName = adapterView.getItemAtPosition(position).toString()
            startActivity(
                Intent(this@GroupsFragment, GroupChatActivity::class.java).putExtra(
                    CommonUtils.GROUP_NAME,
                    currentGroupName
                )
            )
        }

        imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun init() {
        setSupportActionBar(groupListBarLayout as Toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        tvTitle.text = "Groups"
        arrayAdapter = ArrayAdapter(this@GroupsFragment,android.R.layout.simple_list_item_1,listOfGroup)

        listGroup.adapter = arrayAdapter
    }

    private fun retriveAndDisplayGroups() {
        groupReference?.addValueEventListener(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val set = HashSet<String>()
                val iterator = snapshot.children.iterator()
                while (iterator.hasNext()){
                    set.add(iterator.next().key.toString())
                }
                listOfGroup.clear()
                listOfGroup.addAll(set)
                aviLoadingGroups?.smoothToHide()
                arrayAdapter?.notifyDataSetChanged()
            }

        })
    }
}
