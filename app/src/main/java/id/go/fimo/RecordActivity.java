package id.go.fimo;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import android.view.View;
import android.widget.Button;

import com.stepstone.stepper.Step;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;
import com.stepstone.stepper.viewmodel.StepViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import id.go.fimo.model.Data;
import id.go.fimo.model.Section;
import id.go.fimo.model.SurveyQuestion;
import id.go.fimo.step.LocationStep;
import id.go.fimo.step.ImageStep;
import id.go.fimo.step.SurveySectionStep;

public class RecordActivity extends BaseActivity implements StepperLayout.StepperListener{

    private static final String TAG = RecordActivity.class.toString();
    public static String SECTION = "";
    private StepperLayout mStepperLayout;

    private List<Section> sections = App.getInstance().getPreferenceManager().getSections();
    private Map<String, ImageStep> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_data);
        mStepperLayout = findViewById(R.id.stepperLayout);
        mStepperLayout.setAdapter(new AbstractFragmentStepAdapter(getSupportFragmentManager(), this) {
            @Override
            public int getCount() {
                return sections.size();
            }

            @Override
            public Step createStep(int position) {
                Section section = sections.get(position);
                if (section.getType().equals("image")){
                    ImageStep step = new ImageStep(section.getId());
                    map.put(section.getId(),step);
                    return step;
                }else if (section.getType().equals("survey")){
                    List<SurveyQuestion> questions = App.getInstance().getPreferenceManager().getSurveyQuestions(section.getId());
                    return new SurveySectionStep(questions);
                }else if (section.getType().equals("location")){
                    return new LocationStep();
                }

                return null;
            }

            @NonNull
            @Override
            public StepViewModel getViewModel(@IntRange(from = 0) int position) {
                //Override this method to set Step title for the Tabs, not necessary for other stepper types
                Section section = sections.get(position);
                if (position == 0) {
                    return new StepViewModel.Builder(context)
                            .setBackButtonLabel(getResources().getString(R.string.cancel))
                            .setEndButtonLabel(getResources().getString(R.string.next))
                            .setTitle(section.getName()).create();
                }else if (position==sections.size()-1){
                    return new StepViewModel.Builder(context)
                            .setBackButtonLabel(getResources().getString(R.string.back))
                            .setEndButtonLabel(getResources().getString(R.string.finish))
                            .setTitle(section.getName()).create();
                }else{
                    return new StepViewModel.Builder(context)
                            .setBackButtonLabel(getResources().getString(R.string.back))
                            .setEndButtonLabel(getResources().getString(R.string.next))
                            .setTitle(section.getName()).create();
                }

            }

        });
        mStepperLayout.setListener(this);

        if (!checkPermissions()) {
            requestPermissions();
        }else{
            App.getInstance().startService();
        }
    }

    /**@Override
    protected void onStart() {
        super.onStart();
        bindService(uploadServiceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        //uploadService.stopForeground(true);
        //uploadService.stopSelf();
    }*/

    @Override
    public void onCompleted(View view) {
        final Data data = App.getInstance().getPreferenceManager().getData();
        data.setUploaded(false);
        App.getInstance().getPreferenceManager().setData(data);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_submit);
        Button upload = dialog.findViewById(R.id.uploadButton);
        upload.setOnClickListener(view1 -> {
            dialog.dismiss();
            upload(App.getInstance().getPreferenceManager().saveData());
        });
        Button save = dialog.findViewById(R.id.saveButton);
        save.setOnClickListener(view12 -> {
            App.getInstance().getPreferenceManager().saveData();
            dialog.dismiss();
            finish();
        });
        dialog.show();
    }

    private void upload(final Data data){
        data.setUploading(true);
        App.getInstance().getPreferenceManager().putData(data.getId(), data);
        //uploadService.upload(redTide.getId());
        Intent intent = new Intent(this, UploadService.class);
        intent.putExtra("id", data.getId());
        ContextCompat.startForegroundService(this, intent);
        finish();
    }

    @Override
    public void onError(VerificationError verificationError) {

    }

    @Override
    public void onStepSelected(int i) {

    }

    @Override
    public void onReturn() {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        map.get(SECTION).onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
