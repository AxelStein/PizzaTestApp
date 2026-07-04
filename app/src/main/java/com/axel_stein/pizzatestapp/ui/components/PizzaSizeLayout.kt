package com.axel_stein.pizzatestapp.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.axel_stein.pizzatestapp.databinding.LayoutPizzaSizeBinding
import com.axel_stein.pizzatestapp.domain.model.PizzaSize
import com.axel_stein.pizzatestapp.ext.dpToPx
import com.axel_stein.pizzatestapp.ext.loadAsset

class PizzaSizeLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutPizzaSizeBinding.inflate(LayoutInflater.from(context), this, false)

    var onSizeChanged: ((PizzaSize) -> Unit)? = null

    var availableSizes: List<PizzaSize>? = null
        set(value) {
            field = value
            val set = value?.toSet()
            binding.fabSizeSmall.isEnabled = set?.contains(PizzaSize.Small) == true
            binding.fabSizeMedium.isEnabled = set?.contains(PizzaSize.Medium) == true
            binding.fabSizeLarge.isEnabled = set?.contains(PizzaSize.Large) == true
        }

    var currentSize: PizzaSize? = null
        set(value) {
            if (field == value) return
            field = value
            binding.fabSizeSmall.setFabSelected(value == PizzaSize.Small)
            binding.fabSizeMedium.setFabSelected(value == PizzaSize.Medium)
            binding.fabSizeLarge.setFabSelected(value == PizzaSize.Large)
        }

    init {
        addView(binding.root)

        binding.fabSizeSmall.tag = PizzaSize.Small
        binding.fabSizeSmall.setOnClickListener(::onFabClick)

        binding.fabSizeMedium.tag = PizzaSize.Medium
        binding.fabSizeMedium.setOnClickListener(::onFabClick)

        binding.fabSizeLarge.tag = PizzaSize.Large
        binding.fabSizeLarge.setOnClickListener(::onFabClick)

        binding.imageForScale.loadAsset("banana.png")
    }

    private fun onFabClick(fab: View) {
        val tag = fab.tag as? PizzaSize ?: return
        onSizeChanged?.invoke(tag)
    }
}

private fun View.setFabSelected(selected: Boolean) {
    isSelected = selected
    elevation = if (selected || !isEnabled) 0f else 4f.dpToPx(context)
}