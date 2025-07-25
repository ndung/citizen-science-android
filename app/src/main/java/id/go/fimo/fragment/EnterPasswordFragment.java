package id.go.fimo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import androidx.fragment.app.Fragment;
import id.go.fimo.R;
import id.go.fimo.SignInActivity;

public class EnterPasswordFragment extends Fragment {

    TextInputLayout etPass;
    TextView tvForgetPwd;

    public EnterPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_enter_password, container, false);
        etPass = view.findViewById(R.id.et_pass);
        tvForgetPwd = view.findViewById(R.id.tv_forget_pwd);
        tvForgetPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
        return view;
    }

    private void resetPassword() {
        ((SignInActivity) getActivity()).verifyPhoneNumber();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public TextInputLayout getEtPass() {
        return etPass;
    }

}
