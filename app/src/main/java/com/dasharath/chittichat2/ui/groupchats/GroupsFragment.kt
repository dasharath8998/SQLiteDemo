package com.dasharath.chittichat2.ui.groupchats


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.dasharath.chittichat2.R
import com.dasharath.chittichat2.utils.CommonUtils
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_groups.view.*

class GroupsFragment : Fragment() {

    private var groupView: View? = null
    private var arrayAdapter: ArrayAdapter<String>? = null
    private var listOfGroup = ArrayList<String>()
    private var groupReference: DatabaseReference? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        groupView = inflater.inflate(R.layout.fragment_groups, container, false)
        return groupView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupReference = FirebaseDatabase.getInstance().reference.child("Groups")

        init(view)
        retriveAndDisplayGroups()
        view.listGroup.setOnItemClickListener { adapterView, view, position, id ->
            val currentGroupName = adapterView.getItemAtPosition(position).toString()
            startActivity(Intent(context!!,GroupChatActivity::class.java).putExtra(CommonUtils.GROUP_NAME,currentGroupName))
        }
    }

    private fun init(view: View) {
        arrayAdapter = ArrayAdapter(context!!,android.R.layout.simple_list_item_1,listOfGroup)

        view.listGroup.adapter = arrayAdapter
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
                arrayAdapter?.notifyDataSetChanged()
            }

        })
    }
}
