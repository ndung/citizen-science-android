package id.go.fimo.step;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import id.go.fimo.App;
import id.go.fimo.R;
import id.go.fimo.adapter.SurveyAdapter;
import id.go.fimo.fragment.BaseFragment;
import id.go.fimo.model.Data;
import id.go.fimo.model.QuestionParameter;
import id.go.fimo.model.SurveyQuestion;

public class SurveySectionStep extends BaseFragment implements Step {

    private static final String TAG = SurveySectionStep.class.toString();
    private List<SurveyQuestion> list = null;
    private RecyclerView recyclerView;
    private Data data;
    private SurveyAdapter adapter;

    public SurveySectionStep(List<SurveyQuestion> list){
        this.list = list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.layout_questionnaire, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        adapter = new SurveyAdapter(this.getActivity(), list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        return rootView;
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        for (int i=0;i<list.size();i++){
            try {
                SurveyQuestion question = list.get(i);
                boolean bool = App.getInstance().getPreferenceManager().getSurveyAnswers(question.getId())!=null;
                if (question.getType() == 2) {
                    if (question.isRequired() && !bool) {
                        return new VerificationError(getResources().getString(R.string.survey_empty_rb_answer) + " pada pertanyaan no " + (i + 1));
                    }
                } else if (question.getType() == 3 || question.getType() == 7 || question.getType() == 8) {
                    if (question.isRequired() && !bool){
                        return new VerificationError(getResources().getString(R.string.survey_empty_et_answer) + " pada pertanyaan no " + (i + 1));
                    }
                } else if (question.getType() == 4) {
                    if (question.isRequired() && !bool) {
                        return new VerificationError(getResources().getString(R.string.survey_empty_cb_answer) + " pada pertanyaan no " + (i + 1));
                    }
                } else if (question.getType() == 5) {
                    if (question.isRequired() && !bool){
                        return new VerificationError(getResources().getString(R.string.survey_empty_et_answer) + " pada pertanyaan no " + (i + 1));
                    }
                } else if (question.getType() == 6) {
                    if (question.isRequired() && !bool) {
                        return new VerificationError(getResources().getString(R.string.survey_empty_cb_answer) + " pada pertanyaan no " + (i + 1));
                    }
                }
            }catch(Exception ex){
                Log.e(TAG, "error", ex);
            }
        }
        data.setSurvey(App.getInstance().getPreferenceManager().getSurveyAnswers());
        App.getInstance().getPreferenceManager().setData(data);
        return null;
    }


    @Override
    public void onSelected() {
        data = App.getInstance().getPreferenceManager().getData();
        if (data.getSurvey()!=null) {
            App.getInstance().getPreferenceManager().setSurveyAnswers(data.getSurvey());
            Map<String, Object> map = new LinkedHashMap<>();
            for (SurveyQuestion question : list) {
                List<QuestionParameter> pars = data.getSurvey().get(question.getId());
                if (pars != null && !pars.isEmpty() && pars.size() == 1) {
                    map.put(question.getAttribute(), pars.get(0).getDescription());
                } else if (pars == null || pars.isEmpty()) {
                    map.put(question.getAttribute(), "");
                } else {
                    List<String> ansList = new ArrayList<>();
                    for (QuestionParameter ans : pars) {
                        ansList.add(ans.getDescription());
                    }
                    map.put(question.getAttribute(), ansList);
                }
            }
            adapter.setAnswers(map);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onError(@NonNull VerificationError verificationError) {
        showSnackbar(verificationError.getErrorMessage());
    }
}
