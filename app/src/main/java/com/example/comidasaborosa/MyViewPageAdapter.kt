package com.example.comidasaborosa

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.comidasaborosa.fragmentos.FragmentoPerfil
import com.example.comidasaborosa.fragmentos.FragmentoMenu
import com.example.comidasaborosa.fragmentos.FragmentoCarro
import com.example.comidasaborosa.fragmentos.FragmentoLocalizacao

class MyViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        when(position) {
            0 -> return FragmentoPerfil()
            1 -> return FragmentoMenu()
            2 -> return FragmentoCarro()
            3 -> return FragmentoLocalizacao()

            else -> return FragmentoPerfil()
        }
    }

}