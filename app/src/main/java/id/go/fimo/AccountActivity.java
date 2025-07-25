package id.go.fimo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import id.go.fimo.client.ApiUtils;
import id.go.fimo.client.RecordService;
import id.go.fimo.client.Response;
import id.go.fimo.client.SurveyService;
import id.go.fimo.model.DataSummary;
import id.go.fimo.model.User;
import id.go.fimo.util.GsonDeserializer;
import retrofit2.Call;
import retrofit2.Callback;

public class AccountActivity extends BaseActivity {

    private TextView tvName;
    private TextView tvEmail;
    private TextView tvRank;
    private TextView tvSubmitted;
    private TextView tvVerified;
    private TextView tvTotal;
    private TextView tvVersion;
    private CircleImageView imageView;
    private User user;
    private RecordService recordService;
    private LinearLayout changeProfile, changePassword;

    Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new GsonDeserializer()).create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_account);
        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        tvRank = findViewById(R.id.tv_rank);
        tvSubmitted = findViewById(R.id.tv_submitted_records);
        tvVerified = findViewById(R.id.tv_verified_records);
        tvTotal = findViewById(R.id.tv_total_records);
        tvVersion = findViewById(R.id.tv_version);
        imageView = findViewById(R.id.iv_profile);
        changeProfile = findViewById(R.id.ll_change_profile);
        changePassword = findViewById(R.id.ll_change_password);
        //FirebaseAuth auth = FirebaseAuth.getInstance();
        user = App.getInstance().getPreferenceManager().getUser(this);
        tvVersion.setText(BuildConfig.VERSION_NAME);
        tvName.setText(user.getFullName());
        tvEmail.setText(user.getUsername());
        //Picasso.with(getActivity()).load(auth.getCurrentUser().getPhotoUrl()).into(imageView);

        recordService = ApiUtils.RecordService(this);
        changeProfile.setOnClickListener(v -> startActivity(new Intent(this, ChangeProfileActivity.class)));
        changePassword.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));

        refresh();

    }

    private void refresh(){
        recordService.summary().enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                if (response.isSuccessful()) {
                    Response body = response.body();
                    Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new GsonDeserializer()).create();
                    JsonObject jsonObject = gson.toJsonTree(body.getData()).getAsJsonObject();

                    DataSummary summary = gson.fromJson(jsonObject, DataSummary.class);
                    if (summary != null) {
                        //tvRank.setText(summary.getRank());
                        tvSubmitted.setText(String.valueOf(summary.getUploaded()));
                        tvVerified.setText(String.valueOf(summary.getVerified()));
                        tvTotal.setText(String.valueOf(summary.getTotal()));
                    } else {
                        showSnackbar(body.getMessage());
                    }
                } else if (response.errorBody() != null) {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string().trim());
                        showSnackbar(jObjError.getString("message"));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (response.body() != null && response.body().getMessage() != null) {
                    showSnackbar(response.body().getMessage());
                } else {
                    showSnackbar("Error!");
                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                showSnackbar("Error!");
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
