package com.axel_stein.pizzatestapp.ui.screens.pizza_order

import com.axel_stein.pizzatestapp.ui.screens.pizza_order.model.PizzaOrder

data class PizzaOrderUiState(
    val items: List<PizzaOrder> = listOf(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val currentItem: PizzaOrder?
        get() = items.getOrNull(currentIndex)
}