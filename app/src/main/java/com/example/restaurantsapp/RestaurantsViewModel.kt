package com.example.restaurantsapp

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestaurantsViewModel(
    private val stateHandle: SavedStateHandle
) : ViewModel() {
    private val errorHandler =
        CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
        }

    private var restInterface: RestaurantsApiService
    val state = mutableStateOf(emptyList<Restaurant>())

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://papb-restaurantsapp-70b18-default-rtdb.firebaseio.com/")
            .build()
                restInterface = retrofit.create(
                    RestaurantsApiService::class.java
                )
            getRestaurants()
    }

    fun toggleFavorite(id: Int) {
        val restaurants = state.value.toMutableList()
        val itemIndex = restaurants.indexOfFirst { it.id == id }
        val item = restaurants[itemIndex]
        restaurants[itemIndex] = item.copy(isFavorite = !item.isFavorite)
        storeSelection(restaurants[itemIndex])
        state.value = restaurants
    }

    private fun storeSelection(item: Restaurant) {
        val savedToggled = stateHandle.get<List<Int>?>(FAVORITES).orEmpty().toMutableList()
        if (item.isFavorite) savedToggled.add(item.id) else savedToggled.remove(item.id)
        stateHandle[FAVORITES] = savedToggled
    }

    companion object {
        const val FAVORITES = "favorites"
    }

    private fun List<Restaurant>.restoreSelections(): List<Restaurant> {
        stateHandle.get<List<Int>?>(FAVORITES)?.let { selectedIds ->
            val restaurantsMap = this.associateBy { it.id }
            selectedIds.forEach { id ->
                restaurantsMap[id]?.isFavorite = true
            }
            return restaurantsMap.values.toList()
        }
        return this
    }

    private fun getRestaurants() {
        viewModelScope.launch(errorHandler) {
            val restaurants = getRemoteRestaurants()
            withContext(Dispatchers.Main){
                state.value = restaurants.restoreSelections()
            }
            }
        }

    private suspend fun getRemoteRestaurants():List<Restaurant>{
        return withContext(Dispatchers.IO){
            restInterface.getRestaurants()
        }
    }
}


