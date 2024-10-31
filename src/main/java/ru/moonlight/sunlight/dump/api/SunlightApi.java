package ru.moonlight.sunlight.dump.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import ru.moonlight.sunlight.dump.model.api.OrderData;

public interface SunlightApi {

    @Headers(
            "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:131.0) Gecko/20100101 Firefox/131.0"
    )
    @GET("catalog/products/card/purchase/")
    Call<OrderData> fetchOrderData(@Query("article") long article, @Query("city_id") int cityId);

}
