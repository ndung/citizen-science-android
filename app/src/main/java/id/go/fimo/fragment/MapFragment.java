package id.go.fimo.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import androidx.appcompat.widget.AppCompatSpinner;
import id.go.fimo.App;
import id.go.fimo.R;
import id.go.fimo.client.ApiUtils;
import id.go.fimo.client.Response;
import id.go.fimo.client.RecordService;
import id.go.fimo.model.Image;
import id.go.fimo.model.Data;
import id.go.fimo.model.SurveyResponse;
import id.go.fimo.model.User;
import id.go.fimo.util.GsonDeserializer;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class MapFragment extends BaseFragment {

    private static final String TAG = MapFragment.class.toString();

    private MapView mapView;
    private RecordService recordService;
    private User user;
    Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new GsonDeserializer()).create();
    private AppCompatSpinner appCompatSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.layout_map, container, false);

        /**FloatingActionButton fab = rootView.findViewById(R.id.fab);
         fab.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View view) {
        startRecordActivity();
        }
        });*/
        user = App.getInstance().getPreferenceManager().getUser(getActivity());
        recordService = ApiUtils.RecordService(getActivity());
        mapView = rootView.findViewById(R.id.mapView);
        appCompatSpinner = rootView.findViewById(R.id.sp_data);
        ArrayAdapter<String> data = new ArrayAdapter<>(getActivity(), R.layout.spinner_item,
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
                                Data data = new Gson().fromJson(circle.getData(), Data.class);
                                final Dialog dialog = new Dialog(getActivity());
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
                                        DefaultSliderView sliderView = new DefaultSliderView(getActivity());
                                        sliderView.image(App.IMAGE_URL + image.getId()).setScaleType(BaseSliderView.ScaleType.CenterCrop);
                                        sliderView.bundle(new Bundle());
                                        sliderView.getBundle().putString("extra", image.getSection().getName());
                                        sliderView.setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                            @Override
                                            public void onSliderClick(BaseSliderView slider) {
                                                Toast.makeText(getActivity(), slider.getBundle().get("extra") + "", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        slider.addSlider(sliderView);

                                    }
                                }
                                TextView tvMetadata = dialog.findViewById(R.id.tv_metadata);
                                TextView submitted = dialog.findViewById(R.id.uploaded);
                                Button button = dialog.findViewById(R.id.button);
                                button.setOnClickListener(view -> dialog.dismiss());
                                StringBuilder sb = new StringBuilder();
                                for (SurveyResponse s : data.getSurveyResponses()) {
                                    sb.append(s.getQuestion().getAttribute()).append(":")
                                            .append(s.getResponse()).append("\n");
                                }
                                tvMetadata.setText(sb.toString());
                                if (data.getCreateAt() != null) {
                                    submitted.setText(sdf.format(data.getCreateAt()));
                                }
                                dialog.show();
                            }
                        });
                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                        //refresh(appCompatSpinner.getSelectedItemPosition());
                    }
                });

            }
        });

        return rootView;
    }

    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    CircleManager circleManager;

    public void refresh(Integer type) {

        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(type));

        recordService.list(body).enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                if (response.isSuccessful()) {
                    Response body = response.body();
                    JsonObject jsonObject = gson.toJsonTree(body).getAsJsonObject();

                    List<Data> list = gson.fromJson(jsonObject.getAsJsonArray("data"), new TypeToken<List<Data>>() {
                    }.getType());
                    //circleManager.deleteAll();
                    for (Data data : list) {
                        String color = "rgba(136, 136, 136, 1)";
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

}
