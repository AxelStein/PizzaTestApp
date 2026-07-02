package com.axel_stein.pizzatestapp.ui.screens.pizza_order

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.axel_stein.pizzatestapp.R
import com.axel_stein.pizzatestapp.databinding.FragmentPizzaOrderBinding
import kotlinx.coroutines.launch

class PizzaOrderFragment : Fragment(R.layout.fragment_pizza_order) {
    private val viewModel by viewModels<PizzaOrderViewModel>()
    private val productImageAdapter = PizzaImageAdapter()
    private var binding: FragmentPizzaOrderBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPizzaOrderBinding.bind(view)
        this.binding = binding

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.setPadding(
                binding.toolbar.paddingLeft,
                statusBars.top + resources.getDimensionPixelSize(R.dimen.toolbar_vertical_padding),
                binding.toolbar.paddingRight,
                binding.toolbar.paddingBottom
            )
            view.setPadding(0, 0, 0, statusBars.bottom + resources.getDimensionPixelSize(R.dimen.pizza_order_bottom_padding))
            insets
        }

        binding.pagerProductImages.apply {
            (getChildAt(0) as? ViewGroup)?.let {
                it.clipChildren = false
            }
            setPageTransformer(CenterScalePageTransformer())
            offscreenPageLimit = 3
            adapter = productImageAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    viewModel.onPizzaSwiped(position)
                }
            })
        }

        binding.pizzaSizeLayout.onSizeChanged = viewModel::setPizzaSize
        binding.quantityStepper.eventListener = viewModel

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderUi(state)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this.binding = null
    }

    private fun renderUi(state: PizzaOrderUiState) = binding?.run {
        productImageAdapter.submitList(state.items)

        state.currentItem?.let { currentItem ->
            quantityStepper.setQuantity(currentItem.quantity.toString())
            quantityStepper.setPrice(currentItem.price)

            tvPizzaName.text = currentItem.pizza.name
            tvDescription.text = currentItem.pizza.description

            pizzaSizeLayout.pizzaSize = currentItem.size
        }
    }
}