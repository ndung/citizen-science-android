package id.go.fimo.client;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface SurveyService {

    @GET("survey/list/")
    Call<Response> questionnaires(@Query("version") String version);

    @Multipart
    @POST("survey/upload/")
    Call<Response> upload(@Part MultipartBody.Part model);
}
