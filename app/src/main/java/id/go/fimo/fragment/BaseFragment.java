package id.go.fimo.fragment;

import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import id.go.fimo.RecordActivity;

public class BaseFragment extends Fragment {

    protected void startRecordActivity(){
        startActivity(new Intent(getActivity(), RecordActivity.class));
    }

    public void showSnackbar(String message){
        Snackbar.make(getActivity().findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

}
