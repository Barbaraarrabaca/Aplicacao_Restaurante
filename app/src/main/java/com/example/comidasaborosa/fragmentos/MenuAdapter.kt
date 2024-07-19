package com.example.comidasaborosa.fragmentos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.comidasaborosa.databinding.MenuItemBinding


class MenuAdapter (private val menuItensNome:MutableList<String>,
                   private val menuItensPreco:MutableList<String>,
                   private val menuItensImagem:MutableList<Int>,
): RecyclerView.Adapter<MenuAdapter.menuViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): menuViewHolder {
        val binding = MenuItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return menuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: menuViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = menuItensNome.size
    inner class menuViewHolder (private val binding:MenuItemBinding):
        RecyclerView.ViewHolder(binding.root) {


        fun bind(position: Int) {
            binding.apply{
                menuNomeComida.text=menuItensNome[position]
                menuPreco.text=menuItensPreco[position]
                menuImagem.setImageResource(menuItensImagem[position])

            }

        }

    }


}