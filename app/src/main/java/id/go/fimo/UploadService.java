package id.go.fimo;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import id.go.fimo.client.ApiUtils;
import id.go.fimo.client.Response;
import id.go.fimo.client.RecordService;
import id.go.fimo.model.Data;
import id.go.fimo.model.Image;
import id.go.fimo.model.QuestionParameter;
import id.go.fimo.model.SurveyQuestion;
import id.go.fimo.util.Sequence;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class UploadService extends Service {

    private static final String TAG = UploadService.class.toString();

    private final IBinder binder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder
    {
        public UploadService getService()
        {
            return UploadService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String id = null;
        Bundle extras = intent.getExtras();
        if(extras != null){
            id = (String) extras.get("id");
        }

        int notificationId = Sequence.nextValue();
        Notification notification = new NotificationCompat.Builder(this, App.NOTIF_CHANNEL_ID)
                .setContentTitle("FiMo")
                .setContentText("Mengunggah data "+id.split("_")[1])
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
                .build();
        startForeground(notificationId, notification);
        upload(id);

        return super.onStartCommand(intent, flags, startId);
    }

    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();

    public void upload(String id){
        final Data data = App.getInstance().getPreferenceManager().getData(id);
        try {
            RecordService recordService = ApiUtils.RecordService(this);
            //MultipartBody.Part image = MultipartBody.Part.createFormData("image", file.getName(), RequestBody.create(MediaType.parse("*"), file));
            MultipartBody.Part model = MultipartBody.Part.createFormData("model", gson.toJson(data));
            List<MultipartBody.Part> list = new ArrayList<>();
            for (String section : data.getImagePaths().keySet()) {
                for  (String image : data.getImagePaths().get(section)) {
                    File file = new File(image);
                    if (file.exists()) {
                        RequestBody body = RequestBody.create(MediaType.parse("image/*"), file);
                        list.add(MultipartBody.Part.createFormData("images", section+"-"+file.getName(), body));
                    }
                }
            }
            MultipartBody.Part[] images = new MultipartBody.Part[list.size()];
            Map<Integer,Object> map = new LinkedHashMap<>();
            for (String section : App.getInstance().getPreferenceManager().getSurveyQuestions().keySet()){
                List<SurveyQuestion> questions = App.getInstance().getPreferenceManager().getSurveyQuestions(section);
                for (SurveyQuestion s : questions) {
                    List<QuestionParameter> pars = data.getSurvey().get(s.getId());
                    if (pars != null && !pars.isEmpty() && pars.size() == 1) {
                        map.put(s.getId(), pars.get(0).getDescription());
                    } else if (pars == null || pars.isEmpty()) {
                        //map.put(s.getId(), "");
                    } else {
                        List<String> ansList = new ArrayList<>();
                        for (QuestionParameter ans : pars) {
                            ansList.add(ans.getDescription());
                        }
                        map.put(s.getId(), ansList);
                    }
                }
            }
            MultipartBody.Part results = MultipartBody.Part.createFormData("results", gson.toJson(map));
            recordService.upload(model, list.toArray(images), results).enqueue(new Callback<Response>() {
                @Override
                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                    if (response.isSuccessful() && response.body().getData() == Boolean.TRUE) {
                        onUploadSuccessful(data);
                    } else {
                        onUploadFailed(data);
                    }
                }

                @Override
                public void onFailure(Call<Response> call, Throwable t) {
                    onUploadFailed(data);
                }
            });
        }catch (Exception ex){
            Log.e(TAG, "upload exception", ex);
            onUploadFailed(data);
        }
    }

    private void onUploadSuccessful(Data data){
        data.setUploaded(true);
        data.setUploading(false);
        App.getInstance().getPreferenceManager().putData(data.getId(), data);
        Intent uploadNotification = new Intent(App.UPLOAD_NOTIFICATION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(uploadNotification);
        stopForeground(true);
        stopSelf();
        //notificationManager.cancel(notificationId);
    }

    private void onUploadFailed(Data data){
        data.setUploading(false);
        App.getInstance().getPreferenceManager().putData(data.getId(), data);
        Intent uploadNotification = new Intent(App.UPLOAD_NOTIFICATION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(uploadNotification);
        Toast.makeText(this, getResources().getString(R.string.upload_failed_please_retry), Toast.LENGTH_LONG);
        stopForeground(true);
        stopSelf();
        //notificationManager.cancel(notificationId);
    }

}
