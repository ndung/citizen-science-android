package id.go.fimo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textfield.TextInputLayout;

import androidx.fragment.app.Fragment;
import id.go.fimo.R;

public class VerifyPhoneFragment extends Fragment {

    TextInputLayout etVerificationCode;

    public VerifyPhoneFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_verify_phone, container, false);
        etVerificationCode = view.findViewById(R.id.et_verification_code);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public TextInputLayout getEtVerificationCode() {
        return etVerificationCode;
    }
}