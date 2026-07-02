package com.axel_stein.pizzatestapp.data.repository

import com.axel_stein.pizzatestapp.data.api.PizzaApi
import com.axel_stein.pizzatestapp.data.model.PizzaApiModel
import com.axel_stein.pizzatestapp.domain.model.Pizza
import com.axel_stein.pizzatestapp.domain.model.toPizzaSize
import com.axel_stein.pizzatestapp.domain.repository.PizzaRepository

class PizzaRepositoryImpl(private val api: PizzaApi) : BaseRepository(), PizzaRepository {

    override suspend fun getPizzas(): Result<List<Pizza>> = result {
        api.getPizzas()
    }.map { data ->
        data.pizzas?.map { it.mapModel() } ?: listOf()
    }
}

private fun PizzaApiModel.mapModel() = Pizza(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl,
    variants = variants?.map { variant ->
        Pizza.Variant(
            size = variant.size?.toPizzaSize,
            price = variant.price?.toBigDecimalOrNull()
        )
    },
    defaultSize = defaultSize?.toPizzaSize
)