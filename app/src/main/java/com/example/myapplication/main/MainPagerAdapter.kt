package com.example.myapplication.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.myapplication.post.PostFragment

class MainPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val tabs: Array<Fragment> = arrayOf(
        PostFragment()
    )
    private val tabsTitle: Array<String> = arrayOf(
        "Post"
    )

    override fun getItem(position: Int): Fragment {
        return if (position < tabs.count()) {
            tabs[position]
        } else {
            Fragment()
        }
    }

    override fun getCount(): Int {
        return tabs.count()
    }

    override fun getPageTitle(position: Int): CharSequence {
        return if (position < tabsTitle.count()) {
            tabsTitle[position]
        } else {
            "-"
        }
    }

}