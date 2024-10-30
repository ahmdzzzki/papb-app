package com.example.restaurantsapp

import androidx.room.*

@Dao
interface RestaurantsDao {
    @Query("SELECT * FROM Restaurant")
    fun getAll(): List<Restaurant>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAll(restaurants: List<Restaurant>)

    @Update(entity = Restaurant::class)
    fun update(partialRestaurant: PartialRestaurant)

    @Update(entity = Restaurant::class)
    fun updateAll(partialRestaurants: List<PartialRestaurant>)

    @Query("SELECT * FROM restaurant WHERE is_favorite = 1")
    fun getAllFavorited(): List<Restaurant>

}