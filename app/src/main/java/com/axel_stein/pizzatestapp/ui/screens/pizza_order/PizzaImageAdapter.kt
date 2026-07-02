package com.axel_stein.pizzatestapp.ui.screens.pizza_order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import coil.size.Precision
import com.axel_stein.pizzatestapp.databinding.ItemPizzaImageBinding
import com.axel_stein.pizzatestapp.ui.screens.pizza_order.model.PizzaOrder

class PizzaImageAdapter : ListAdapter<PizzaOrder, PizzaImageAdapter.ViewHolder>(DiffCallback) {

    private object DiffCallback : DiffUtil.ItemCallback<PizzaOrder>() {
        override fun areItemsTheSame(
            oldItem: PizzaOrder,
            newItem: PizzaOrder
        ): Boolean {
            return oldItem.pizza.id == newItem.pizza.id
        }

        override fun areContentsTheSame(
            oldItem: PizzaOrder,
            newItem: PizzaOrder
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = ViewHolder(
        ItemPizzaImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemPizzaImageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PizzaOrder) {
            binding.imageView.load(item.pizza.imageUrl) {
                allowHardware(false)
                precision(Precision.AUTOMATIC)
                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.ENABLED)
            }
            binding.imageView.pizzaSize = item.size
        }
    }
}