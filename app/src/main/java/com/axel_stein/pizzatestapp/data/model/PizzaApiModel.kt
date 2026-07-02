package com.axel_stein.pizzatestapp.data.model

data class PizzaApiModel(
    val id: String?,
    val name: String?,
    val description: String?,
    val imageUrl: String?,
    val variants: List<Variant>?,
    val defaultSize: String?,
) {
    data class Variant(
        val size: String?,
        val price: String?,
    )
}