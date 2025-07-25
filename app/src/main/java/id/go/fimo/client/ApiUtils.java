package id.go.fimo.client;

import android.content.Context;

public class ApiUtils {

    public final static String SOMETHING_WRONG = "Gagal, mohon cek koneksi internet Anda";

    public static AuthService AuthService(Context context){
        return RetrofitClient.getClient(context).create(AuthService.class);
    }
    public static RecordService RecordService(Context context){
        return RetrofitClient.getClient(context).create(RecordService.class);
    }
    public static SurveyService SurveyService(Context context){
        return RetrofitClient.getClient(context).create(SurveyService.class);
    }
}
