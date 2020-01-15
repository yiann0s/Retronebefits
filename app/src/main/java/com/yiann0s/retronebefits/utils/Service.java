package com.yiann0s.retronebefits.utils;

import com.yiann0s.retronebefits.model.Car;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface Service {
    @GET("sample_array.json")
    Call<List<Car>> getJson();
}
