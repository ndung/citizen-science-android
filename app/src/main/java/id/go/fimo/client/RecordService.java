package id.go.fimo.client;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RecordService {

    @Multipart
    @POST("record/upload/")
    Call<Response> upload(@Part MultipartBody.Part model,
                          @Part MultipartBody.Part[] images,
                          @Part MultipartBody.Part survey);

    @POST("record/list/")
    Call<Response> list(@Body RequestBody requestBody);

    @GET("record/summary/")
    Call<Response> summary();

}
