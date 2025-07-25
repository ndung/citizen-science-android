package id.go.fimo;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import id.go.fimo.client.ApiUtils;
import id.go.fimo.client.AuthService;
import id.go.fimo.client.Response;
import id.go.fimo.model.User;
import id.go.fimo.util.GsonDeserializer;
import id.go.fimo.util.StringUtils;
import retrofit2.Call;
import retrofit2.Callback;

public class ChangeProfileActivity extends BaseActivity {

    private TextView tvTitle;
    private TextInputLayout etName, etEmail, etAge, etAddress, etZipCode, etJob, etAffiliation;
    private AppCompatSpinner spGender, spEducation, spDistance;
    private RelativeLayout pwdLayout;
    private LinearLayout tncLayout;
    private AuthService authService;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_signup);
        tvTitle = findViewById(R.id.tv_title);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        pwdLayout = findViewById(R.id.layout_pwd);
        //etAge = findViewById(R.id.et_age);
        spGender = findViewById(R.id.sp_gender);
        spEducation = findViewById(R.id.sp_education);
        //spDistance = findViewById(R.id.sp_distance);
        //etDob = view.findViewById(R.id.et_bod);
        etAddress = findViewById(R.id.et_address);
        etZipCode = findViewById(R.id.et_zip_code);
        etJob = findViewById(R.id.et_job);
        //etAffiliation = findViewById(R.id.et_affiliation);
        tncLayout = findViewById(R.id.layout_tnc);

        pwdLayout.setVisibility(View.GONE);
        tncLayout.setVisibility(View.GONE);

        ArrayAdapter<String> gender = new ArrayAdapter<>(this, R.layout.spinner_item, Arrays.asList("Laki-laki", "Perempuan"));
        gender.setDropDownViewResource(R.layout.spinner_item);
        spGender.setAdapter(gender);

        ArrayAdapter<String> education = new ArrayAdapter<>(this, R.layout.spinner_item, Arrays.asList("Dibawah SMA", "SMA", "S1", "S2", "S3"));
        education.setDropDownViewResource(R.layout.spinner_item);
        spEducation.setAdapter(education);

        //ArrayAdapter<String> distance = new ArrayAdapter<>(this, R.layout.spinner_item, Arrays.asList("kurang dari 1 kilometer", "antara 1 kilometer sampai 10 kilometer", "diatas 10 kilometer"));
        //distance.setDropDownViewResource(R.layout.spinner_item);
        //spDistance.setAdapter(distance);

        user = App.getInstance().getPreferenceManager().getUser(this);
        etAddress.getEditText().setText(user.getAddress());
        //etAge.getEditText().setText(String.valueOf(user.getAge()));
        etEmail.getEditText().setText(user.getEmail());
        etName.getEditText().setText(user.getFullName());
        selectSpinnerItemByValue(spGender, user.getGender());
        selectSpinnerItemByValue(spEducation, user.getEducation());
        //selectSpinnerItemByValue(spDistance, user.getDistance());
        //etAffiliation.getEditText().setText(user.getInstitution());
        etZipCode.getEditText().setText(user.getPostalCode());
        etJob.getEditText().setText(user.getProfession());

        authService = ApiUtils.AuthService(this);
        tvTitle.setText("Ubah profil");
        Button button = findViewById(R.id.bt_change);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeProfile();
            }
        });
    }

    public static void selectSpinnerItemByValue(AppCompatSpinner spnr, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spnr.getAdapter();
        for (int position = 0; position < adapter.getCount(); position++) {
            if(((String)adapter.getItem(position)).equalsIgnoreCase(value)) {
                spnr.setSelection(position);
                return;
            }
        }
    }

    private void changeProfile(){
        String name = etName.getEditText().getText().toString();
        if (name.isEmpty()){
            etName.setError("Nama tidak boleh kosong");
            return;
        }
        etName.setError("");
        String email = etEmail.getEditText().getText().toString();
        if (email.isEmpty()){
            etEmail.setError("Email tidak boleh kosong");
            return;
        }
        if (!StringUtils.isEmailValid(email)){
            etEmail.setError("Email harus valid");
            return;
        }
        etEmail.setError("");
        /**String age = etAge.getEditText().getText().toString();
        if (age.isEmpty()){
            etAge.setError("Usia tidak boleh kosong");
            return;
        }
        try{
            int num = Integer.parseInt(age);
            if (num<0 || num>99){
                etAge.setError("Usia harus lebih besar dari 0 dan lebih kecil dari 99");
                return;
            }
        } catch (NumberFormatException e) {
            etAge.setError("Usia harus valid");
            return;
        }
        etAge.setError("");*/
        String address = etAddress.getEditText().getText().toString();
        if (address.isEmpty()){
            etAddress.setError("Alamat tidak boleh kosong");
            return;
        }
        etAddress.setError("");
        String zipCode = etZipCode.getEditText().getText().toString();
        if (zipCode.isEmpty()){
            etZipCode.setError("Kode pos tidak boleh kosong");
            return;
        }
        if (zipCode.length()!=5){
            etZipCode.setError("Kode pos harus terdiri dari 5 angka");
            return;
        }
        etZipCode.setError("");
        String job = etJob.getEditText().getText().toString();
        if (job.isEmpty()){
            etJob.setError("Pekerjaan tidak boleh kosong");
            return;
        }
        etJob.setError("");
        //String affiliation = etAffiliation.getEditText().getText().toString();
        String gender = spGender.getSelectedItem().toString();
        String education = spEducation.getSelectedItem().toString();
        //String distance = spDistance.getSelectedItem().toString();

        Map<String,String> map = new HashMap<>();
        map.put("fullName", name);
        map.put("email", email);
        map.put("gender", gender);
        map.put("age", "0");
        map.put("address", address);
        map.put("postalCode", zipCode);
        map.put("distance", "");
        map.put("profession", job);
        map.put("institution", "");
        map.put("education", education);
        authService.updateProfile(map).enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                dissmissPleaseWaitDialog();
                if (response.isSuccessful()) {
                    Response body = response.body();
                    Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new GsonDeserializer()).create();
                    JsonObject jsonObject = gson.toJsonTree(body.getData()).getAsJsonObject();
                    User user = gson.fromJson(jsonObject, User.class);
                    if (user != null) {
                        new AlertDialog.Builder(ChangeProfileActivity.this)
                                .setTitle("Berhasil!")
                                .setPositiveButton(android.R.string.yes, null)
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .show();
                    } else {
                        showSnackbar(body.getMessage());
                    }
                } else if (response.errorBody() != null) {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string().trim());
                        etZipCode.setError(jObjError.getString("message"));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    showSnackbar(ApiUtils.SOMETHING_WRONG);
                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                dissmissPleaseWaitDialog();
                showSnackbar(ApiUtils.SOMETHING_WRONG);

            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
