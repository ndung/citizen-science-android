package id.go.fimo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Circle;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;
import com.mapbox.mapboxsdk.plugins.annotation.OnCircleClickListener;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import id.go.fimo.client.ApiUtils;
import id.go.fimo.client.RecordService;
import id.go.fimo.client.Response;
import id.go.fimo.fragment.MapFragment;
import id.go.fimo.model.Data;
import id.go.fimo.model.Image;
import id.go.fimo.model.SurveyResponse;
import id.go.fimo.model.User;
import id.go.fimo.util.GsonDeserializer;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class DataMapActivity extends BaseActivity {
    private static final String TAG = MapFragment.class.toString();

    private MapView mapView;
    private RecordService recordService;
    private User user;
    Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new GsonDeserializer()).create();
    private AppCompatSpinner appCompatSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_map);
        user = App.getInstance().getPreferenceManager().getUser(this);
        recordService = ApiUtils.RecordService(this);
        mapView = findViewById(R.id.mapView);
        appCompatSpinner = findViewById(R.id.sp_data);
        ArrayAdapter<String> data = new ArrayAdapter<>(this, R.layout.spinner_item,
                Arrays.asList("Rekaman data yang saya kirim", "Rekaman data saya yang terverifikasi", "Semua data"));
        data.setDropDownViewResource(R.layout.spinner_item);
        appCompatSpinner.setAdapter(data);
        appCompatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refresh(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {

                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        circleManager = new CircleManager(mapView, mapboxMap, style);
                        circleManager.addClickListener(new OnCircleClickListener() {
                            @Override
                            public void onAnnotationClick(Circle circle) {
                                annotate(circle);
                            }
                        });
                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                        //refresh(appCompatSpinner.getSelectedItemPosition());
                    }
                });

            }
        });
    }

    private void annotate(Circle circle){
        Data data = new Gson().fromJson(circle.getData(), Data.class);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_data);
        SliderLayout slider = dialog.findViewById(R.id.slider);
        slider.setPresetTransformer(SliderLayout.Transformer.Default);
        slider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        slider.setDuration(4000);
        slider.setVisibility(View.VISIBLE);
        //ImageView imageView = dialog.findViewById(R.id.image);
        //Picasso.with(getActivity()).load(App.IMAGE_URL+redTide.getAlboomImages().get(0)).into(imageView);
        if (data.getImages() != null) {
            for (Image image : data.getImages()) {
                DefaultSliderView sliderView = new DefaultSliderView(this);
                sliderView.image(App.IMAGE_URL + image.getId()).setScaleType(BaseSliderView.ScaleType.CenterCrop);
                sliderView.bundle(new Bundle());
                sliderView.getBundle().putString("extra", image.getSection().getName());
                sliderView.setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                    @Override
                    public void onSliderClick(BaseSliderView slider) {
                        showToast(slider);
                    }
                });
                slider.addSlider(sliderView);

            }
        }
        TextView notes = dialog.findViewById(R.id.tv_metadata);
        TextView submitted = dialog.findViewById(R.id.uploaded);
        Button button = dialog.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        StringBuilder sb = new StringBuilder();
        for (SurveyResponse s : data.getSurveyResponses()){
            sb.append(s.getQuestion().getAttribute()).append(":").append(s.getResponse()).append(";");
        }
        notes.setText(sb.toString());
        if (data.getCreateAt() != null) {
            submitted.setText(sdf.format(data.getCreateAt()));
        }
        dialog.show();
    }

    private void showToast(BaseSliderView slider){
        Toast.makeText(this, slider.getBundle().get("extra") + "", Toast.LENGTH_SHORT).show();
    }

    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    CircleManager circleManager;

    public void refresh(Integer type){

        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(type));

        recordService.list(body).enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                if (response.isSuccessful()) {
                    Response body = response.body();
                    JsonObject jsonObject = gson.toJsonTree(body).getAsJsonObject();

                    List<Data> list = gson.fromJson(jsonObject.getAsJsonArray("data"), new TypeToken<List<Data>>() {
                    }.getType());
                    circleManager.deleteAll();
                    String[] colors = new String[]{"rgba(136, 136, 136, 1)","rgba(255, 0, 0, 1)","rgba(101, 67, 33, 1)",
                            "rgba(181, 101, 29, 1)","rgba(255, 165, 0, 1)","rgba(181, 101, 29, 1)","rgba(255, 255, 0, 1)",
                            "rgba(1, 50, 32, 1)","rgba(144, 238, 144, 1)"};
                    Random random = new Random();
                    for (Data data : list){
                        String color = colors[random.nextInt(9)];
                        CircleOptions circleOptions = new CircleOptions()
                                .withLatLng(new LatLng(data.getLatitude(), data.getLongitude()))
                                .withCircleColor(color)
                                .withData(new Gson().toJsonTree(data))
                                .withCircleRadius(6f);
                        circleManager.create(circleOptions);
                    }
                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
