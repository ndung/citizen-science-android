package id.go.fimo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import id.go.fimo.client.ApiUtils;
import id.go.fimo.client.AuthService;
import id.go.fimo.client.Response;
import id.go.fimo.fragment.CreatePasswordFragment;
import id.go.fimo.fragment.EnterPasswordFragment;
import id.go.fimo.fragment.InputPhoneFragment;
import id.go.fimo.fragment.SignUpFragment;
import id.go.fimo.fragment.VerifyPhoneFragment;
import id.go.fimo.model.LoginDetails;
import id.go.fimo.model.Section;
import id.go.fimo.model.SurveyQuestion;
import id.go.fimo.util.ChipperUtils;
import id.go.fimo.util.GsonDeserializer;
import id.go.fimo.util.StringUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class SignInActivity extends BaseActivity {

    private FrameLayout mainFrame;
    private Button btNext;
    private FragmentManager fragmentManager;

    private static int STEP_INPUT_PHONE = 1;
    private static int STEP_VERIFY_PHONE = 2;
    private static int STEP_INPUT_PASSWORD = 3;
    private static int STEP_SIGN_UP = 4;
    private static int STEP_CREATE_PASSWORD = 5;

    private static int STEP = 1;

    private AuthService authService;

    private InputPhoneFragment inputPhoneFragment;
    private VerifyPhoneFragment verifyPhoneFragment;
    private EnterPasswordFragment enterPasswordFragment;
    private CreatePasswordFragment createPasswordFragment;
    private SignUpFragment signUpFragment;

    private String phoneNumber = null;

    private static final String TAG = "PhoneAuthActivity";

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_signin);
        mainFrame = findViewById(R.id.main_frame);
        authService = ApiUtils.AuthService(this);

        fragmentManager = getSupportFragmentManager();

        inputPhoneFragment = new InputPhoneFragment();
        verifyPhoneFragment = new VerifyPhoneFragment();
        enterPasswordFragment = new EnterPasswordFragment();
        createPasswordFragment = new CreatePasswordFragment();
        signUpFragment = new SignUpFragment();
        setFragment(inputPhoneFragment);

        STEP = STEP_INPUT_PHONE;
        btNext = findViewById(R.id.bt_next);
        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (STEP == STEP_INPUT_PHONE) {
                    checkPhoneNumber();
                } else if (STEP == STEP_VERIFY_PHONE) {
                    verifyPhoneNumberWithCode(mVerificationId,
                            verifyPhoneFragment.getEtVerificationCode().getEditText().getText().toString());
                } else if (STEP == STEP_INPUT_PASSWORD) {
                    signIn();
                } else if (STEP == STEP_CREATE_PASSWORD) {
                    createPassword();
                } else if (STEP == STEP_SIGN_UP) {
                    signUp();
                }
            }

        });

        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }
                verifyPhoneFragment.getEtVerificationCode().setError("SMS kode verifikasi gagal dikirimkan. "+e.getLocalizedMessage());
                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);
                verifyPhoneFragment.getEtVerificationCode().setError("SMS kode verifikasi telah dikirimkan ke nomor handphone Anda");
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
        // [END phone_auth_callbacks]
    }

    Double status = 0d;

    private void checkPhoneNumber() {
        phoneNumber = inputPhoneFragment.getEtPhoneNumber().getEditText().getText().toString();
        if (!phoneNumber.isEmpty() && StringUtils.isMobilePhoneNumberValid(phoneNumber)) {
            showPleaseWaitDialog();
            String params = "phoneNumber";
            Map<String, String> map = new HashMap<>();
            map.put(params, phoneNumber);
            RequestBody body = RequestBody.create(MediaType.parse("text/plain"), phoneNumber);

            authService.checkPhoneNumber(body).enqueue(new Callback<Response>() {
                @Override
                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                    dissmissPleaseWaitDialog();
                    if (response.isSuccessful()) {
                        Response body = response.body();
                        status = (double) body.getData();
                        if (status > 0) {
                            STEP = STEP_INPUT_PASSWORD;
                            setFragment(enterPasswordFragment);
                        } else if (status <= 0) {
                            verifyPhoneNumber();
                            //STEP = STEP_CREATE_PASSWORD;
                            //setFragment(createPasswordFragment);
                        }
                    } else if (response.errorBody() != null) {
                        try {
                            JSONObject jObjError = new JSONObject(response.errorBody().string().trim());
                            inputPhoneFragment.getEtPhoneNumber().setError(jObjError.getString("message"));
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
                    Log.e(TAG, "error", t);
                    showSnackbar(ApiUtils.SOMETHING_WRONG);

                }
            });
        } else {
            inputPhoneFragment.getEtPhoneNumber().setError("Nomor handphone harus diisi dan valid");
        }
    }

    private void signIn() {
        final String pwd = enterPasswordFragment.getEtPass().getEditText().getText().toString();
        if (!pwd.isEmpty()) {
            if (!phoneNumber.isEmpty()) {
                showPleaseWaitDialog();
                Map<String, String> map = new HashMap<>();
                final String publicKey = ChipperUtils.getPublicKey(phoneNumber, pwd);
                map.put("username", phoneNumber);
                map.put("publicKey", publicKey);
                authService.signIn(map).enqueue(new Callback<Response>() {
                    @Override
                    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                        dissmissPleaseWaitDialog();
                        if (response.isSuccessful()) {
                            onSuccessfulLogin(response);
                        } else if (response.errorBody() != null) {
                            try {
                                JSONObject jObjError = new JSONObject(response.errorBody().string().trim());
                                enterPasswordFragment.getEtPass().setError(jObjError.getString("message"));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else if (response.body() != null && response.body().getMessage() != null) {
                            showSnackbar(response.body().getMessage());
                        } else {
                            showSnackbar(ApiUtils.SOMETHING_WRONG);
                        }
                    }

                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {
                        dissmissPleaseWaitDialog();
                        Log.e(TAG, "error", t);
                        showSnackbar(ApiUtils.SOMETHING_WRONG);

                    }
                });
            }
        } else {
            enterPasswordFragment.getEtPass().setError("Kata sandi tidak boleh kosong");
        }
    }

    private void createPassword() {
        String pwd = createPasswordFragment.getEtPass().getEditText().getText().toString();
        if (!pwd.isEmpty()) {
            if (StringUtils.isPasswordValid(pwd)) {
                if (!phoneNumber.isEmpty()) {
                    showPleaseWaitDialog();
                    Map<String, String> map = new HashMap<>();
                    map.put("username", phoneNumber);
                    map.put("password", pwd);
                    authService.createPassword(map).enqueue(new Callback<Response>() {
                        @Override
                        public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                            dissmissPleaseWaitDialog();
                            if (response.isSuccessful()) {
                                onSuccessfulLogin(response);
                            } else if (response.errorBody() != null) {
                                try {
                                    JSONObject jObjError = new JSONObject(response.errorBody().string().trim());
                                    createPasswordFragment.getEtPass().setError(jObjError.getString("message"));
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
                            Log.e(TAG, "error", t);
                            showSnackbar(ApiUtils.SOMETHING_WRONG);

                        }
                    });
                }
            } else {
                createPasswordFragment.getEtPass().setError("Kata sandi minimal terdiri dari 6 karakter");
            }
        } else {
            createPasswordFragment.getEtPass().setError("Kata sandi tidak boleh kosong");
        }
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        // [END start_phone_auth]
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        if (verificationId==null||verificationId.isEmpty()){
            verifyPhoneFragment.getEtVerificationCode().setError("SMS kode verifikasi gagal dikirimkan");
            return;
        }
        if (code.isEmpty()){
            verifyPhoneFragment.getEtVerificationCode().setError("Kode verifikasi (OTP) tidak boleh kosong");
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(token)     // ForceResendingToken from callbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // Update UI
                            if (user!=null){
                                if (status==-1) {
                                    STEP = STEP_SIGN_UP;
                                    setFragment(signUpFragment);
                                }else{
                                    STEP = STEP_CREATE_PASSWORD;
                                    setFragment(createPasswordFragment);
                                }
                            }
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                            verifyPhoneFragment.getEtVerificationCode().setError("Verifikasi nomor handphone Anda gagal. "+task.getException().getLocalizedMessage());
                        }
                    }
                });
    }
    // [END sign_in_with_phone]

    public void verifyPhoneNumber(){
        STEP = STEP_VERIFY_PHONE;
        startPhoneNumberVerification(phoneNumber.replaceFirst("0","+62"));
        setFragment(verifyPhoneFragment);
    }

    private void setFragment(Fragment fragment) {
        fragmentManager.beginTransaction().replace(mainFrame.getId(), fragment).commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if (STEP==STEP_VERIFY_PHONE||STEP==STEP_CREATE_PASSWORD||STEP==STEP_INPUT_PASSWORD||STEP==STEP_SIGN_UP){
            STEP = STEP_INPUT_PHONE;
            setFragment(inputPhoneFragment);
        }else{
            finish();
        }
    }

    private void signUp(){
        String name = signUpFragment.getEtName().getEditText().getText().toString();
        if (name.isEmpty()){
            signUpFragment.getEtName().setError("Nama tidak boleh kosong");
            return;
        }
        signUpFragment.getEtName().setError("");
        String email = signUpFragment.getEtEmail().getEditText().getText().toString();
        if (email.isEmpty()){
            signUpFragment.getEtEmail().setError("Email tidak boleh kosong");
            return;
        }
        if (!StringUtils.isEmailValid(email)){
            signUpFragment.getEtEmail().setError("Email harus valid");
            return;
        }
        signUpFragment.getEtEmail().setError("");
        String password = signUpFragment.getEtPassword().getEditText().getText().toString();
        if (password.isEmpty()){
            signUpFragment.getEtPassword().setError("Kata sandi tidak boleh kosong");
            return;
        }
        if (!StringUtils.isPasswordValid(password)) {
            signUpFragment.getEtPassword().setError("Kata sandi minimal terdiri dari 6 karakter");
            return;
        }
        signUpFragment.getEtPassword().setError("");
        /**String age = signUpFragment.getEtAge().getEditText().getText().toString();
        if (age.isEmpty()){
            signUpFragment.getEtAge().setError("Usia tidak boleh kosong");
            return;
        }
        try{
            int num = Integer.parseInt(age);
            if (num<0 || num>99){
                signUpFragment.getEtAge().setError("Usia harus lebih besar dari 0 dan lebih kecil dari 99");
                return;
            }
        } catch (NumberFormatException e) {
            signUpFragment.getEtAge().setError("Usia harus valid");
            return;
        }
        signUpFragment.getEtAge().setError("");*/
        String address = signUpFragment.getEtAddress().getEditText().getText().toString();
        if (address.isEmpty()){
            signUpFragment.getEtAddress().setError("Alamat tidak boleh kosong");
            return;
        }
        signUpFragment.getEtAddress().setError("");
        String zipCode = signUpFragment.getEtZipCode().getEditText().getText().toString();
        if (zipCode.isEmpty()){
            signUpFragment.getEtZipCode().setError("Kode pos tidak boleh kosong");
            return;
        }
        if (zipCode.length()!=5){
            signUpFragment.getEtZipCode().setError("Kode pos harus terdiri dari 5 angka");
            return;
        }
        signUpFragment.getEtZipCode().setError("");
        String job = signUpFragment.getEtJob().getEditText().getText().toString();
        if (job.isEmpty()){
            signUpFragment.getEtJob().setError("Pekerjaan tidak boleh kosong");
            return;
        }
        signUpFragment.getEtJob().setError("");
        //String affiliation = signUpFragment.getEtAffiliation().getEditText().getText().toString();
        String gender = signUpFragment.getSpGender().getSelectedItem().toString();
        String education = signUpFragment.getSpEducation().getSelectedItem().toString();
        //String distance = signUpFragment.getSpDistance().getSelectedItem().toString();

        Map<String,String> map = new HashMap<>();
        map.put("userName", phoneNumber);
        map.put("fullName", name);
        map.put("email", email);
        map.put("password", password);
        map.put("gender", gender);
        map.put("address", address);
        map.put("postalCode", zipCode);
        map.put("profession", job);
        map.put("education", education);
        authService.signUp(map).enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                dissmissPleaseWaitDialog();
                if (response.isSuccessful()) {
                    onSuccessfulLogin(response);
                } else if (response.errorBody() != null) {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string().trim());
                        signUpFragment.getEtZipCode().setError(jObjError.getString("message"));
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
                Log.e(TAG, "error", t);
                showSnackbar(ApiUtils.SOMETHING_WRONG);

            }
        });
    }

    private void onSuccessfulLogin(retrofit2.Response<Response> response){
        Response body = response.body();
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new GsonDeserializer()).create();
        Log.d(TAG,body.toString());
        JsonObject jsonObject = gson.toJsonTree(body.getData()).getAsJsonObject();
        LoginDetails details = gson.fromJson(jsonObject, LoginDetails.class);
        if (details != null) {
            String token = response.headers().get("Token");
            App.getInstance().getPreferenceManager().setUser(getApplicationContext(), details.getUser());
            App.getInstance().getPreferenceManager().setToken(getApplicationContext(), token);
            App.getInstance().getPreferenceManager().setLoginFlag(getApplicationContext(), true);
            startActivity(new Intent(getApplicationContext(), MainHomeActivity.class));
            FirebaseMessaging.getInstance().subscribeToTopic(String.valueOf(details.getUser().getId()));
            FirebaseMessaging.getInstance().subscribeToTopic("global");

            List<SurveyQuestion> questions = details.getQuestions();
            Map<String,List<SurveyQuestion>> questionMap = new TreeMap<>();
            for (SurveyQuestion q : questions){
                String section = q.getSection().getId();
                List<SurveyQuestion> list = new ArrayList<>();
                if (questionMap.containsKey(section)){
                    list = questionMap.get(section);
                }
                list.add(q);
                questionMap.put(section, list);
            }
            App.getInstance().getPreferenceManager().setSurveyQuestions(questionMap);

            List<Section> sections = details.getSections();
            App.getInstance().getPreferenceManager().setSections(sections);
            WelcomeActivity.activity.finish();
            finish();
        } else {
            showSnackbar(body.getMessage());
        }
    }
}