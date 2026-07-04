package com.axel_stein.pizzatestapp.ui.screens.pizza_order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axel_stein.pizzatestapp.domain.model.PizzaSize
import com.axel_stein.pizzatestapp.domain.repository.PizzaRepository
import com.axel_stein.pizzatestapp.ui.components.QuantityStepper
import com.axel_stein.pizzatestapp.ui.screens.pizza_order.model.PizzaOrder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PizzaOrderViewModel : ViewModel(), KoinComponent, QuantityStepper.EventListener {

    companion object {
        private const val MAX_QUANTITY = 99
    }

    private val repository by inject<PizzaRepository>()

    private val _uiState = MutableStateFlow(PizzaOrderUiState())
    val uiState: StateFlow<PizzaOrderUiState> = _uiState.asStateFlow()

    init {
        loadPizzas()
    }

    private fun loadPizzas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            delay(2000)

            repository.getPizzas()
                .onSuccess { data ->
                    _uiState.update { state ->
                        state.copy(
                            items = data.map {
                                PizzaOrder(
                                    pizza = it,
                                    quantity = 1,
                                    size = it.defaultSize ?: PizzaSize.Medium
                                )
                            },
                            isLoading = false,
                            currentIndex = 0
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.message
                        )
                    }
                }
        }
    }

    fun onPizzaSwiped(newIndex: Int) {
        if (newIndex in _uiState.value.items.indices) {
            _uiState.update {
                it.copy(currentIndex = newIndex)
            }
        }
    }

    fun setPizzaSize(size: PizzaSize) {
        updateCurrentItem { it.copy(size = size) }
    }

    private fun incrementQuantity() {
        _uiState.value.currentItem?.let { item ->
            if (item.quantity < MAX_QUANTITY) {
                updateCurrentItem {
                    it.copy(quantity = item.quantity + 1)
                }
            }
        }
    }

    private fun decrementQuantity() {
        _uiState.value.currentItem?.let { item ->
            if (item.quantity > 1) {
                updateCurrentItem {
                    it.copy(quantity = item.quantity - 1)
                }
            }
        }
    }

    private fun updateCurrentItem(transform: (PizzaOrder) -> PizzaOrder) {
        _uiState.update { state ->
            val current = state.currentItem

            val updatedList = state.items.map { item ->
                if (item.pizza.id == current?.pizza?.id) {
                    transform(item)
                } else {
                    item
                }
            }
            state.copy(items = updatedList)
        }
    }

    override fun onIncrementQuantityClick() {
        incrementQuantity()
    }

    override fun onDecrementQuantityClick() {
        decrementQuantity()
    }

    override fun onAddClick() {}
}