package com.yiann0s.retronebefits.utils;

import com.yiann0s.retronebefits.model.Car;
import com.yiann0s.retronebefits.model.Dog;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface Service {
    @POST("breeds/image/random")
    Call<Dog> randomDog();

    @GET("sample_array.json")
    Call<List<Car>> getJson();
}
