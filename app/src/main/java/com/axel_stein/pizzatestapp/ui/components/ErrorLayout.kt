package com.axel_stein.pizzatestapp.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.axel_stein.pizzatestapp.R
import com.axel_stein.pizzatestapp.databinding.LayoutErrorBinding
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutErrorBinding.inflate(
        LayoutInflater.from(context),
        this,
        false
    )

    var onRetryClick: (() -> Unit)? = null

    init {
        binding.btnRetry.setOnClickListener {
            onRetryClick?.invoke()
        }
        addView(binding.root)
    }

    fun setError(error: Throwable?) {
        isVisible = error != null
        if (error != null) {
            binding.errMessage.text = if (error.isNetworkError()) {
                context.getString(R.string.error_network)
            } else {
                error.message ?: context.getString(R.string.error_base)
            }
        }
    }
}

fun Throwable.isNetworkError(): Boolean {
    return this is UnknownHostException ||
        this is ConnectException ||
        this is SocketTimeoutException
}