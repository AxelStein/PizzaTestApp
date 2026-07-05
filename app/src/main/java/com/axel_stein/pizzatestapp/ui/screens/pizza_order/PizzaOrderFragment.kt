package com.axel_stein.pizzatestapp.ui.screens.pizza_order

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.axel_stein.pizzatestapp.R
import com.axel_stein.pizzatestapp.databinding.FragmentPizzaOrderBinding
import kotlinx.coroutines.launch

class PizzaOrderFragment : Fragment(R.layout.fragment_pizza_order) {
    private val viewModel by viewModels<PizzaOrderViewModel>()
    private var binding: FragmentPizzaOrderBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPizzaOrderBinding.bind(view)
        this.binding = binding

        binding.run {
            orderLayout.onPizzaSwiped = viewModel::swipePizza
            orderLayout.onPizzaSizeChanged = viewModel::setPizzaSize
            orderLayout.onQuantityIncremented = viewModel::incrementQuantity
            orderLayout.onQuantityDecremented = viewModel::decrementQuantity

            splashView.onDisappeared = {
                orderLayout.shouldAppear = viewModel.uiState.value.currentItem != null
            }

            errorLayout.onRetryClick = viewModel::refresh
        }

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
        splashView.isAppeared = state.isLoading
        errorLayout.setError(state.error)

        orderLayout.setItems(state.items)
        state.currentItem?.let { item ->
            orderLayout.setCurrentItem(item)
        }
    }
}