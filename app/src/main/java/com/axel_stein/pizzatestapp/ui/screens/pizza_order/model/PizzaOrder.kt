package com.axel_stein.pizzatestapp.ui.screens.pizza_order.model

import com.axel_stein.pizzatestapp.domain.model.Pizza
import com.axel_stein.pizzatestapp.domain.model.PizzaSize
import java.text.NumberFormat
import java.util.Locale

data class PizzaOrder(
    val pizza: Pizza,
    val quantity: Int,
    val size: PizzaSize
) {

    val price: String
        get() = pizza
            .variants
            ?.find { it.size == size }
            ?.price
            ?.multiply(quantity.toBigDecimal())
            ?.let {
                NumberFormat
                    .getCurrencyInstance(Locale.US)
                    .format(it)
            }
            .orEmpty()
}