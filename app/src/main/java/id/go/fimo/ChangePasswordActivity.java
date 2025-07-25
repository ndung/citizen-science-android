package id.go.fimo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import id.go.fimo.client.ApiUtils;
import id.go.fimo.client.AuthService;
import id.go.fimo.client.Response;
import id.go.fimo.util.StringUtils;
import retrofit2.Call;
import retrofit2.Callback;

public class ChangePasswordActivity extends BaseActivity {

    private TextInputLayout etOldPassword;
    private TextInputLayout etNewPassword;
    private TextInputLayout etReNewPassword;
    private Button button;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_change_password);
        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etReNewPassword = findViewById(R.id.et_confirm_password);
        button = findViewById(R.id.bt_change);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
        authService = ApiUtils.AuthService(this);
    }

    private void changePassword(){
        String oldPassword = etOldPassword.getEditText().getText().toString();
        if (oldPassword.equals("")){
            etOldPassword.setError("Kata sandi lama tidak boleh kosong");
            return;
        }
        etOldPassword.setError("");
        String newPassword = etNewPassword.getEditText().getText().toString();
        if (newPassword.equals("")){
            etNewPassword.setError("Kata sandi baru tidak boleh kosong");
            return;
        }
        etNewPassword.setError("");
        String reNewPassword = etReNewPassword.getEditText().getText().toString();
        if (reNewPassword.equals("")){
            etReNewPassword.setError("Kata sandi baru tidak boleh kosong");
            return;
        }
        etReNewPassword.setError("");
        if (!reNewPassword.equals(newPassword)){
            etNewPassword.setError("Kata sandi baru harus sama");
            etReNewPassword.setError("Kata sandi baru harus sama");
            return;
        }
        etNewPassword.setError("");
        etReNewPassword.setError("");
        if (!StringUtils.isPasswordValid(newPassword)) {
            etNewPassword.setError("Kata sandi minimal terdiri dari 6 karakter");
            etReNewPassword.setError("Kata sandi minimal terdiri dari 6 karakter");
            return;
        }
        etNewPassword.setError("");
        etReNewPassword.setError("");
        Map<String,String> map = new HashMap<>();
        map.put("oldPassword", oldPassword);
        map.put("newPassword", newPassword);
        authService.changePassword(map).enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                dissmissPleaseWaitDialog();
                if (response.isSuccessful()) {
                    new AlertDialog.Builder(ChangePasswordActivity.this)
                            .setTitle("Berhasil!")
                            .setPositiveButton(android.R.string.yes, null)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
                } else if (response.errorBody() != null) {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string().trim());
                        etOldPassword.setError(jObjError.getString("message"));
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
