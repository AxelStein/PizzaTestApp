package com.axel_stein.pizzatestapp.domain.model

import java.math.BigDecimal

data class Pizza(
    val id: String?,
    val name: String?,
    val description: String?,
    val imageUrl: String?,
    val variants: List<Variant>?,
    val defaultSize: PizzaSize?
) {
    data class Variant(
        val size: PizzaSize?,
        val price: BigDecimal?
    )
}