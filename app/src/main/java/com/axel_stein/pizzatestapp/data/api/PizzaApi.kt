package com.axel_stein.pizzatestapp.data.api

import com.axel_stein.pizzatestapp.data.model.PizzasListApiModel
import retrofit2.Response
import retrofit2.http.GET

interface PizzaApi {

    @GET("pizzas")
    suspend fun getPizzas(): Response<PizzasListApiModel>
}