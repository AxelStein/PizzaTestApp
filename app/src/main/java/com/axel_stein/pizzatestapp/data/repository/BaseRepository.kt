package com.axel_stein.pizzatestapp.data.repository

import com.axel_stein.pizzatestapp.data.model.ApiError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import kotlin.coroutines.cancellation.CancellationException

open class BaseRepository {

    suspend fun <T> result(block: suspend () -> Response<T>): Result<T> = withContext(Dispatchers.IO) {
        try {
            val response = block.invoke()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(handleError(response))
            }
        } catch (e: Throwable) {
            if (e is CancellationException) {
                throw e
            } else {
                Result.failure(e)
            }
        }
    }

    fun <T> handleError(response: Response<T>): ApiError {
        val code = response.code()
        val error = response.errorBody()?.string() ?: "Error with code $code"
        return ApiError(code, error)
    }
}