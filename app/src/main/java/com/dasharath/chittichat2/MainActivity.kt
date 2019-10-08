package com.dasharath.chittichat2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.dasharath.chittichat2.ui.findfriends.FindFriendsActivity
import com.dasharath.chittichat2.ui.groupchats.GroupsFragment
import com.dasharath.chittichat2.ui.login.LoginActivity
import com.dasharath.chittichat2.ui.settings.SettingsActivity
import com.dasharath.chittichat2.utils.CommonFunction.gone
import com.dasharath.chittichat2.utils.CommonFunction.visible
import com.dasharath.chittichat2.utils.CommonUtils
import com.dasharath.chittichat2.viewpager.TabsAccessorAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_layout.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var rootRef: DatabaseReference? = null
    private var currentUserId: String? = ""
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        rootRef = FirebaseDatabase.getInstance().reference

        setSupportActionBar(mainPageToolbar as Toolbar)
        tvTitle.text = "Chitti Chat"

        imgBack.gone()
        imgFindFriend.visible()
        imgFindFriend.setOnClickListener {
            startActivity(Intent(this@MainActivity, FindFriendsActivity::class.java))
        }

        viewPagerMainTab.adapter =
            TabsAccessorAdapter(supportFragmentManager)
        mainTab.setupWithViewPager(viewPagerMainTab)
    }

    override fun onStart() {
        super.onStart()

        val currentUser = mAuth?.currentUser
        if (currentUser == null) {
            sendUserToLogInActivity()
        } else {
            updateUserStatus("online")
            verifyUserExistance()
        }
    }

    override fun onStop() {
        super.onStop()
        val currentUser = mAuth?.currentUser
        if (currentUser != null) {
            updateUserStatus("offline")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val currentUser = mAuth?.currentUser
        if (currentUser != null) {
            updateUserStatus("offline")
        }
    }

    private fun verifyUserExistance() {
        var currentUserId = mAuth?.currentUser?.uid
        rootRef?.child("Users")?.child(currentUserId!!)
            ?.addValueEventListener(object : ValueEventListener {

                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child("name").exists()) {

                    } else {
                        startActivity(
                            Intent(
                                this@MainActivity,
                                SettingsActivity::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                        finish()
                    }
                }

            })
    }

    private fun sendUserToLogInActivity() {
        startActivity(
            Intent(
                this@MainActivity,
                LoginActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onOptionsItemSelected(item)
        when (item?.itemId) {

            R.id.menuSettings -> {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }

            R.id.menuLogOut -> {
                updateUserStatus("offline")
                mAuth?.signOut()
                sendUserToLogInActivity()
            }

            R.id.menuFindGroup -> {
                startActivity(Intent(this@MainActivity, GroupsFragment::class.java))
            }

            R.id.menuTermAndConditions -> {

            }
        }
        return true
    }

    private fun requestNewGroup() {
        val alertDialog = AlertDialog.Builder(this@MainActivity, R.style.AlertDialog)
        alertDialog.setTitle("Enter Group Name")
        val etGroupName = EditText(this@MainActivity)
        etGroupName.hint = "e.g. Kings Groups"
        etGroupName.maxLines = 1
        val layout = FrameLayout(this@MainActivity)
        layout.setPaddingRelative(45,15,45,0)
        layout.addView(etGroupName)
        alertDialog.setView(layout)

        alertDialog.setPositiveButton("Create") { dialog, which ->
            val groupName = etGroupName.text.toString()
            if(TextUtils.isEmpty(groupName)){
                Toast.makeText(this@MainActivity,"Please provide group name...",Toast.LENGTH_LONG).show()
            }else{
                createNewGroup(groupName)
            }
        }

        alertDialog.setNegativeButton("Cancel") { dialog, which ->
            dialog?.cancel()
        }

        alertDialog.show()
    }

    private fun createNewGroup(groupName: String) {
        rootRef?.child("Groups")?.child(groupName)?.setValue("")
            ?.addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(this@MainActivity,groupName+" Group is Created Successfully",Toast.LENGTH_LONG).show()
                }
            }
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateUserStatus(state: String){
        val cal = Calendar.getInstance()
        val currentDate = SimpleDateFormat("MMM dd, yyyy")
        val saveCurrentDate = currentDate.format(cal.time)

        val currentTimeFormat = SimpleDateFormat("hh:mm a")
        val currentTime = currentTimeFormat.format(cal.time)

        val onLineState = HashMap<String, Any>()
        onLineState.put(CommonUtils.TIME,saveCurrentDate)
        onLineState.put(CommonUtils.DATE,currentTime)
        onLineState.put(CommonUtils.STATE,state)

        currentUserId = mAuth?.currentUser?.uid!!
        rootRef?.child(CommonUtils.USERS_DB_REF)?.child(currentUserId!!)?.child(CommonUtils.USER_STATE)?.updateChildren(onLineState)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }
}
