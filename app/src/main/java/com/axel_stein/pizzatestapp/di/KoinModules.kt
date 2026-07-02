package com.axel_stein.pizzatestapp.di

import com.axel_stein.pizzatestapp.data.api.PizzaApi
import com.axel_stein.pizzatestapp.data.repository.PizzaRepositoryImpl
import com.axel_stein.pizzatestapp.domain.repository.PizzaRepository
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {
    single {
        val httpClient = OkHttpClient.Builder()
            .connectTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://oursongapp.com/api/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(get()))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }

    factory {
        GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
    }

    factory<PizzaRepository> {
        PizzaRepositoryImpl(
            get<Retrofit>().create(PizzaApi::class.java)
        )
    }
}