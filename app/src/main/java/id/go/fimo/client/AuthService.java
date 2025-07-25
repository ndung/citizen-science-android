package id.go.fimo.client;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {

    @POST("credential/check-credential-id")
    Call<Response> checkPhoneNumber(@Body RequestBody s);

    @POST("credential/create-password")
    Call<Response> createPassword(@Body Map<String, String> map);

    @POST("credential/change-pwd")
    Call<Response> changePassword(@Body Map<String, String> map);

    @POST("credential/sign-in")
    Call<Response> signIn(@Body Map<String, String> map);

    @POST("credential/sign-up")
    Call<Response> signUp(@Body Map<String, String> map);

    @POST("credential/update-profile")
    Call<Response> updateProfile(@Body Map<String, String> map);
}
