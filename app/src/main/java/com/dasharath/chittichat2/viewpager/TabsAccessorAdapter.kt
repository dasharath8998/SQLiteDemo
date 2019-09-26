package com.dasharath.chittichat2.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.dasharath.chittichat2.RequestFragment
import com.dasharath.chittichat2.ui.chats.ChatFragment
import com.dasharath.chittichat2.ui.contacts.ContactsFragment
import com.dasharath.chittichat2.ui.groupchats.GroupsFragment

class TabsAccessorAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        when(position){
            0 ->{
                 return ChatFragment()
            }
            1 ->{
                return GroupsFragment()
            }
            2 ->{
                return ContactsFragment()
            }
            3 ->{
                return RequestFragment()
            }
            else -> {
                return Fragment()
            }
        }
    }

    override fun getCount(): Int = 4

    override fun getPageTitle(position: Int): CharSequence? {
        when(position){
            0 ->{
                return "Chats"
            }
            1 ->{
                return "Groups"
            }
            2 ->{
                return "Contacts"
            }
            3 ->{
                return "Request"
            }
            else -> {
                return ""
            }
        }
    }
}