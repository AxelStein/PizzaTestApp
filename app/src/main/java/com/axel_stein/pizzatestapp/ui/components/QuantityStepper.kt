package com.axel_stein.pizzatestapp.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.axel_stein.pizzatestapp.databinding.LayoutQuantityStepperBinding

class QuantityStepper @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    interface EventListener {
        fun onIncrementQuantityClick()
        fun onDecrementQuantityClick()
        fun onAddClick()
    }

    private val binding = LayoutQuantityStepperBinding.inflate(LayoutInflater.from(context), this, false)

    var eventListener: EventListener? = null

    init {
        addView(binding.root)
        binding.btnIncrement.setOnClickListener {
            eventListener?.onIncrementQuantityClick()
        }
        binding.btnDecrement.setOnClickListener {
            eventListener?.onDecrementQuantityClick()
        }
        binding.btnAdd.setOnClickListener {
            eventListener?.onAddClick()
        }
    }

    fun setQuantity(quantity: String) {
        binding.tvQuantity.text = quantity
    }

    fun setPrice(price: String) {
        binding.tvPrice.text = price
    }
}