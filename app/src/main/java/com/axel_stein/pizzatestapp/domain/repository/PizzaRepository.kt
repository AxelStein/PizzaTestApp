package com.axel_stein.pizzatestapp.domain.repository

import com.axel_stein.pizzatestapp.domain.model.Pizza

interface PizzaRepository {

    suspend fun getPizzas(): Result<List<Pizza>>
}