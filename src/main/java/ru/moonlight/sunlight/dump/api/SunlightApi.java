package ru.moonlight.sunlight.dump.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import ru.moonlight.sunlight.dump.model.OrderData;

import static ru.moonlight.sunlight.dump.Constants.USER_AGENT_HEADER;

public interface SunlightApi {

    @Headers(USER_AGENT_HEADER)
    @GET("catalog/products/card/purchase/")
    Call<OrderData> fetchOrderData(@Query("article") long article, @Query("city_id") int cityId);

}
