package com.dasharath.chittichat2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.dasharath.chittichat2.ui.findfriends.FindFriendsActivity
import com.dasharath.chittichat2.ui.settings.SettingsActivity
import com.dasharath.chittichat2.ui.login.LoginActivity
import com.dasharath.chittichat2.utils.CommonUtils
import com.dasharath.chittichat2.viewpager.TabsAccessorAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var currentUser: FirebaseUser? = null
    private var mAuth: FirebaseAuth? = null
    private var rootRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth?.currentUser
        rootRef = FirebaseDatabase.getInstance().reference

        setSupportActionBar(mainPageToolbar as Toolbar)
        supportActionBar?.title = "Chitti Chat"

        viewPagerMainTab.adapter =
            TabsAccessorAdapter(supportFragmentManager)
        mainTab.setupWithViewPager(viewPagerMainTab)
    }

    override fun onStart() {
        super.onStart()
        if (currentUser == null) {
            sendUserToLogInActivity()
        } else {
            verifyUserExistance()
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
                        Toast.makeText(this@MainActivity, "Welcome", Toast.LENGTH_LONG).show()
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
            R.id.menuFindFriends -> {
                startActivity(Intent(this@MainActivity, FindFriendsActivity::class.java))
            }
            R.id.menuSettings -> {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
            R.id.menuLogOut -> {
                mAuth?.signOut()
                sendUserToLogInActivity()
            }
            R.id.menuCreateGroup -> {
                //requestNewGroup()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }
}
