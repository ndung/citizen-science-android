package id.go.fimo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

import id.go.fimo.client.ApiUtils;
import id.go.fimo.client.Response;
import id.go.fimo.client.AuthService;
import retrofit2.Call;
import retrofit2.Callback;

public class WelcomeActivity extends BaseActivity {

    TextView tv;

    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_welcome);
        tv = findViewById(R.id.tv);
        authService = ApiUtils.AuthService(this);

        Button button = findViewById(R.id.btn_login);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSignIn();
            }
        });
        activity = this;
        //FirebaseAuth auth = FirebaseAuth.getInstance();
        //if (auth.getCurrentUser() != null) {
        //    refresh();
        //}
        boolean isLoggedIn = App.getInstance().getPreferenceManager().getLoginFlag(this);
        if (isLoggedIn){
            startActivity(new Intent(getApplicationContext(), MainHomeActivity.class));
            finish();
        }
    }

    private static final int RC_SIGN_IN = 9001;

    private void startSignIn() {
        // Build FirebaseUI sign in intent. For documentation on this operation and all
        // possible customization see: https://github.com/firebase/firebaseui-android
        /**Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.GoogleBuilder().build()))//,
                //new AuthUI.IdpConfig.FacebookBuilder().build(),
                //new AuthUI.IdpConfig.EmailBuilder().build(),
                //new AuthUI.IdpConfig.PhoneBuilder().build()))
                .setLogo(R.mipmap.ic_launcher)
                .build();

        startActivityForResult(intent, RC_SIGN_IN);*/
        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            IdpResponse response = IdpResponse.fromResultIntent(data);
            tv.setText("resultCode:"+resultCode);
            if (resultCode == RESULT_OK) {
                // Sign in succeeded
                refresh();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(getResources().getString(R.string.no_internet_connection));
                    return;
                }
                tv.setText("error:"+response.getError());
                showSnackbar(getResources().getString(R.string.sign_in_error)+response.getError());
            }
        }
    }

    AuthService authService;

    private void refresh(){
        showPleaseWaitDialog();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        tv.setText("auth:"+auth);
        if (auth.getCurrentUser()!=null) {
            Map<String,String> map = new HashMap();
            map.put("name", auth.getCurrentUser().getDisplayName());
            map.put("email", auth.getCurrentUser().getEmail());
            map.put("uid", auth.getCurrentUser().getUid());
            map.put("photo", auth.getCurrentUser().getPhotoUrl().toString());
            authService.signIn(map).enqueue(new Callback<Response>() {
                @Override
                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                    tv.setText("response:"+response);
                    startActivity(new Intent(getApplicationContext(), MainHomeActivity.class));
                    dissmissPleaseWaitDialog();
                    finish();
                }

                @Override
                public void onFailure(Call<Response> call, Throwable t) {
                    tv.setText("throwable:"+t);
                    dissmissPleaseWaitDialog();
                    showSnackbar("Error!");
                }
            });
        }
    }
}
