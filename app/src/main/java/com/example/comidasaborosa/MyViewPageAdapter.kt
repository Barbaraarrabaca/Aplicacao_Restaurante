package com.example.comidasaborosa

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.comidasaborosa.fragmentos.fragmento_1
import com.example.comidasaborosa.fragmentos.fragmento_2
import com.example.comidasaborosa.fragmentos.fragmento_3
import com.example.comidasaborosa.fragmentos.fragmento_4

class MyViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        when(position) {
            0 -> return fragmento_1()
            1 -> return fragmento_2()
            2 -> return fragmento_3()
            3 -> return fragmento_4()

            else -> return fragmento_1()
        }
    }

}