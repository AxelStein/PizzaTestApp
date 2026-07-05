package com.axel_stein.pizzatestapp.ui.screens.pizza_order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.axel_stein.pizzatestapp.R
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

    var onItemClickAt: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val vh = ViewHolder(
            ItemPizzaImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        vh.binding.imageView.setOnClickListener {
            val pos = vh.bindingAdapterPosition
            if (pos in 0 until itemCount) {
                onItemClickAt?.invoke(pos)
            }
        }
        return vh
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class ViewHolder(val binding: ItemPizzaImageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PizzaOrder) {
            binding.imageView.tag =  item.pizza.imageUrl
            binding.imageView.load(item.pizza.imageUrl) {
                placeholder(R.drawable.pizza_placeholder)
                error(R.drawable.pizza_placeholder)
                crossfade(true)
                crossfade(600)
                allowHardware(false)
                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.ENABLED)
            }
            binding.imageView.pizzaSize = item.size
        }
    }
}