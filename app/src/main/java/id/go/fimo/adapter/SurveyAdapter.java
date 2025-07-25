package id.go.fimo.adapter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.xw.repo.BubbleSeekBar;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.go.fimo.App;
import id.go.fimo.R;
import id.go.fimo.model.QuestionParameter;
import id.go.fimo.model.SurveyQuestion;

public class SurveyAdapter extends RecyclerView.Adapter<SurveyAdapter.ViewHolder> {

    List<SurveyQuestion> list = new ArrayList<>();

    private Activity activity;
    private Map<String,Object> answers = new HashMap<>();

    public SurveyAdapter(Activity activity, List<SurveyQuestion> list) {
        this.activity = activity;
        this.list = list;
    }

    public Map<String, Object> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, Object> answers) {
        this.answers = answers;
    }

    private static final String TAG = SurveyAdapter.class.toString();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_survey_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private DecimalFormat decimalFormat = new DecimalFormat("###,###,###");

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final SurveyQuestion question = list.get(position);

        holder.textView.setText(Html.fromHtml((position + 1) + ". " + question.getQuestion()));
        if (question.getType() == 1) {
            holder.radioGroup.setVisibility(View.GONE);
            holder.checkBoxes.setVisibility(View.GONE);
            holder.editText.setVisibility(View.GONE);
            holder.seekBar.setVisibility(View.VISIBLE);
            holder.seekBar.setCustomSectionTextArray(new BubbleSeekBar.CustomSectionTextArray() {
                @NonNull
                @Override
                public SparseArray<String> onCustomize(int sectionCount, @NonNull SparseArray<String> array) {
                    array.clear();

                    for (int i=0;i<question.getParameters().size();i++){
                        QuestionParameter parameter = question.getParameters().get(i);
                        array.put(i, parameter.getDescription());
                    }

                    return array;
                }
            });

            holder.seekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
                @Override
                public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                    List<QuestionParameter> ans = new ArrayList<>();
                    for (QuestionParameter parameter : question.getParameters()) {
                        if (parameter.getSequence() == holder.seekBar.getProgress()) {
                            ans.add(parameter);
                        }
                    }
                    App.getInstance().getPreferenceManager().putSurveyAnswers(question.getId(), ans);
                }

                @Override
                public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

                }

                @Override
                public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

                }

            });

            Object ans = answers.get(question.getAttribute());
            if (ans!=null) {
                holder.seekBar.setProgress(Float.parseFloat((String) ans));
            }
            holder.setIsRecyclable(false);
        }
        else if (question.getType() == 2) {
            holder.editText.setVisibility(View.GONE);
            holder.checkBoxes.setVisibility(View.GONE);
            holder.radioGroup.setVisibility(View.VISIBLE);
            holder.radioGroup.removeAllViews();
            holder.seekBar.setVisibility(View.GONE);
            for (QuestionParameter choice : question.getParameters()) {
                RadioButton rb = new RadioButton(activity);
                rb.setTag(choice.getSequence());
                rb.setText(Html.fromHtml(choice.getDescription()));
                rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                rb.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                holder.radioGroup.addView(rb);
            }
            holder.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    RadioButton checkedRadioButton = group.findViewById(checkedId);
                    if (checkedRadioButton != null) {
                        boolean isChecked = checkedRadioButton.isChecked();
                        if (isChecked) {
                            List<QuestionParameter> newans = new ArrayList<>();
                            newans.add(question.getParameters().get((Integer) checkedRadioButton.getTag() - 1));
                            App.getInstance().getPreferenceManager().putSurveyAnswers(question.getId(), newans);
                        }
                    }
                }
            });
            //List<QuestionParameter> ans = App.getInstance().getPreferenceManager().getSurveyAnswers(question.getId());
            Object ans = answers.get(question.getAttribute());
            if (ans != null) {
                for (int i = 0; i < holder.radioGroup.getChildCount(); i++) {
                    RadioButton rb = (RadioButton) holder.radioGroup.getChildAt(i);
                    //if (ans.get(0).getSequence() == (Integer) rb.getTag()) {
                    //    rb.setChecked(true);
                    //}
                    if (((String)ans).equalsIgnoreCase(rb.getText().toString())){
                        rb.setChecked(true);
                    }
                }
            }
        } else if (question.getType() == 3) {
            holder.radioGroup.setVisibility(View.GONE);
            holder.checkBoxes.setVisibility(View.GONE);
            holder.editText.setVisibility(View.VISIBLE);
            holder.seekBar.setVisibility(View.GONE);
            //holder.editText.addTextChangedListener(new NumberTextWatcher(holder.editText));
            holder.editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    List<QuestionParameter> ans = new ArrayList<>();
                    QuestionParameter parameter = new QuestionParameter();
                    parameter.setId(0);
                    parameter.setDescription(holder.editText.getText().toString());
                    ans.add(parameter);
                    App.getInstance().getPreferenceManager().putSurveyAnswers(question.getId(), ans);
                }
            });
            Object ans = answers.get(question.getAttribute());
            //List<QuestionParameter> ans = App.getInstance().getPreferenceManager().getSurveyAnswers(question.getId());
            if (ans != null) {
                holder.editText.setText((String)ans);
            }
        } else if (question.getType() == 4) {
            holder.radioGroup.setVisibility(View.GONE);
            holder.checkBoxes.removeAllViews();
            holder.checkBoxes.setVisibility(View.VISIBLE);
            holder.editText.setVisibility(View.GONE);
            holder.seekBar.setVisibility(View.GONE);
            //List<QuestionParameter> ans = App.getInstance().getPreferenceManager().getSurveyAnswers(question.getId());
            Object ans = answers.get(question.getAttribute());
            for (QuestionParameter choice : question.getParameters()) {
                CheckBox cb = new CheckBox(activity);
                cb.setTag(choice.getSequence());
                cb.setText(Html.fromHtml(choice.getDescription()));
                cb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                cb.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                holder.checkBoxes.addView(cb);

                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        CheckBox checkedCheckBox = (CheckBox) compoundButton;
                        List<QuestionParameter> newans = App.getInstance().getPreferenceManager().getSurveyAnswers(question.getId());
                        if (newans == null) {
                            newans = new ArrayList<>();
                        }
                        QuestionParameter checkAns = question.getParameters().get((Integer) checkedCheckBox.getTag() - 1);
                        if (isChecked && !newans.contains(checkAns)) {
                            newans.add(checkAns);
                        } else if (!isChecked && newans.contains(checkAns)) {
                            newans.remove(checkAns);
                        }
                        App.getInstance().getPreferenceManager().putSurveyAnswers(question.getId(), newans);
                    }
                });
                if (ans != null) {
                    if (ans instanceof List && ((List)ans).contains(choice.getDescription())){
                        cb.setChecked(true);
                    } else if (ans instanceof String && ((String)ans).equalsIgnoreCase(choice.getDescription())){
                        cb.setChecked(true);
                    }
                }
            }

        } else if (question.getType() == 5) {
            holder.radioGroup.setVisibility(View.GONE);
            holder.checkBoxes.setVisibility(View.GONE);
            holder.seekBar.setVisibility(View.GONE);
            holder.editText.setVisibility(View.VISIBLE);
            holder.editText.setFocusable(false);
            holder.editText.setInputType(InputType.TYPE_CLASS_DATETIME);
            holder.editText.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    new DatePickerDialog(activity, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new OnDateSetListener(holder.editText),
                                myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
            holder.editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    List<QuestionParameter> ans = new ArrayList<>();
                    QuestionParameter parameter = new QuestionParameter();
                    parameter.setId(0);
                    parameter.setDescription(holder.getEditText().getText().toString());
                    ans.add(parameter);
                    App.getInstance().getPreferenceManager().putSurveyAnswers(question.getId(), ans);
                }
            });
            //List<QuestionParameter> ans = App.getInstance().getPreferenceManager().getSurveyAnswers(question.getId());
            Object ans = answers.get(question.getAttribute());
            if (ans != null) {
                holder.editText.setText((String)ans);
            }
        } else if (question.getType() == 6) {
            holder.radioGroup.removeAllViews();
            holder.radioGroup.setVisibility(View.VISIBLE);
            holder.checkBoxes.setVisibility(View.GONE);
            holder.editText.setVisibility(View.VISIBLE);
            holder.editText.setInputType(InputType.TYPE_CLASS_TEXT);
            holder.seekBar.setVisibility(View.GONE);
            for (int i = 0; i < question.getParameters().size(); i++) {
                QuestionParameter choice = question.getParameters().get(i);

                RadioButton rb = new RadioButton(activity);
                rb.setTag(choice.getSequence());
                rb.setText(Html.fromHtml(choice.getDescription()));
                rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                rb.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                holder.radioGroup.addView(rb);
            }

            List<QuestionParameter> ans = App.getInstance().getPreferenceManager().getSurveyAnswers(question.getId());
            if (ans != null) {
                if (ans.get(0).getId() >= holder.radioGroup.getChildCount()) {
                    RadioButton rb = (RadioButton) holder.radioGroup.getChildAt(holder.radioGroup.getChildCount() - 1);
                    rb.setChecked(true);
                    holder.editText.setVisibility(View.VISIBLE);
                    holder.editText.setText(ans.get(0).getDescription());
                } else {
                    for (int i = 0; i < holder.radioGroup.getChildCount(); i++) {
                        RadioButton rb = (RadioButton) holder.radioGroup.getChildAt(i);
                        if (ans.get(0).getSequence() == (Integer) rb.getTag()) {
                            rb.setChecked(true);
                        }
                    }
                }
            }

            holder.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    RadioButton checkedRadioButton = group.findViewById(checkedId);
                    if (checkedRadioButton != null) {
                        boolean isChecked = checkedRadioButton.isChecked();
                        if (isChecked && (Integer) checkedRadioButton.getTag() != holder.radioGroup.getChildCount()) {
                            List<QuestionParameter> newans = new ArrayList<>();
                            newans.add(question.getParameters().get((Integer) checkedRadioButton.getTag() - 1));
                            App.getInstance().getPreferenceManager().putSurveyAnswers(question.getId(), newans);
                        }
                    }
                }
            });

            holder.editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    RadioButton rb = ((RadioButton) holder.radioGroup.getChildAt(holder.radioGroup.getChildCount() - 1));
                    if (!rb.isChecked()) {
                        holder.radioGroup.clearCheck();
                        rb.setChecked(true);
                    }
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    List<QuestionParameter> ans = new ArrayList<>();
                    QuestionParameter questionParameter = new QuestionParameter();
                    int tempID = holder.radioGroup.getChildCount() + 1;
                    questionParameter.setId(tempID);
                    //questionParameter.setSequence(tempID);
                    questionParameter.setDescription(holder.getEditText().getText().toString());
                    ans.add(questionParameter);
                    App.getInstance().getPreferenceManager().putSurveyAnswers(question.getId(), ans);
                }
            });

            holder.setIsRecyclable(false);
        } else if (question.getType() == 7) {
            holder.radioGroup.setVisibility(View.GONE);
            holder.checkBoxes.setVisibility(View.GONE);
            holder.seekBar.setVisibility(View.GONE);
            holder.editText.setVisibility(View.VISIBLE);
            holder.editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            holder.editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    List<QuestionParameter> ans = new ArrayList<>();
                    QuestionParameter parameter = new QuestionParameter();
                    parameter.setId(0);
                    parameter.setDescription(holder.editText.getText().toString());
                    ans.add(parameter);
                    App.getInstance().getPreferenceManager().putSurveyAnswers(question.getId(), ans);
                }
            });
            //List<QuestionParameter> ans = App.getInstance().getPreferenceManager().getSurveyAnswers(question.getId());
            Object ans = answers.get(question.getAttribute());
            if (ans != null) {
                holder.editText.setText((String)ans);
            }
        } else if (question.getType() == 8) {
            holder.radioGroup.setVisibility(View.GONE);
            holder.checkBoxes.setVisibility(View.GONE);
            holder.seekBar.setVisibility(View.GONE);
            holder.editText.setVisibility(View.VISIBLE);
            holder.editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            holder.editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    List<QuestionParameter> ans = new ArrayList<>();
                    QuestionParameter parameter = new QuestionParameter();
                    parameter.setId(0);
                    parameter.setDescription(holder.editText.getText().toString());
                    ans.add(parameter);
                    App.getInstance().getPreferenceManager().putSurveyAnswers(question.getId(), ans);
                }
            });
            //List<QuestionParameter> ans = App.getInstance().getPreferenceManager().getSurveyAnswers(question.getId());
            Object ans = answers.get(question.getAttribute());
            if (ans != null) {
                holder.editText.setText((String)ans);
            }
        }
    }

    class OnDateSetListener implements DatePickerDialog.OnDateSetListener{

        EditText editText;

        OnDateSetListener(EditText editText){
            this.editText = editText;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String myFormat = "yyyy-MM-dd";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
            editText.setText(sdf.format(myCalendar.getTime()));
        }
    }

    Calendar myCalendar = Calendar.getInstance();

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected BubbleSeekBar seekBar;
        protected TextView textView;
        protected EditText editText;
        protected RadioGroup radioGroup;
        protected LinearLayout checkBoxes;

        public ViewHolder(View itemView) {
            super(itemView);
            seekBar = itemView.findViewById(R.id.seekbar);
            textView = itemView.findViewById(R.id.description);
            editText = itemView.findViewById(R.id.editText);
            radioGroup = itemView.findViewById(R.id.radioGroup);
            checkBoxes = itemView.findViewById(R.id.linearLayout_checkboxes);
        }

        public EditText getEditText() {
            return editText;
        }

        public RadioGroup getRadioGroup() {
            return radioGroup;
        }

        public LinearLayout getCheckBoxes() {
            return checkBoxes;
        }
    }
}
