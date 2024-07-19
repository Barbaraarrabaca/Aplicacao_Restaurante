package com.example.comidasaborosa.fragmentos


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.comidasaborosa.R
import com.example.comidasaborosa.databinding.FragmentoMenuBinding

class FragmentoMenu : Fragment() {

    private lateinit var binding: FragmentoMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentoMenuBinding.inflate(inflater,container,false)

        val menuNomeComida = listOf("Burger", "Sandwich", "Mono", "Item", "Sandwich", "Mono")
        val menuPreco = listOf("5", "6", "8", "10", "7", "4")
        val menuImagem = listOf(
            R.drawable.menu1,
            R.drawable.menu2,
            R.drawable.menu3,
            R.drawable.menu4,
            R.drawable.menu5,
            R.drawable.menu6
        )

        val adapter = MenuAdapter(ArrayList(menuNomeComida), ArrayList(menuPreco), ArrayList(menuImagem))
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRecyclerView.adapter = adapter
        return binding.root
    }
        companion object {
    }
}