package com.yiann0s.retronebefits.utils

import com.yiann0s.retronebefits.model.Car
import retrofit2.Call
import retrofit2.http.GET

interface Service {
    @GET("sample_array.json")
    fun getJson() : Call<MutableList<Car>>
}