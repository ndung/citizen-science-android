package id.go.fimo.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import id.go.fimo.BuildConfig;
import id.go.fimo.model.QuestionParameter;
import id.go.fimo.model.Data;
import id.go.fimo.model.Section;
import id.go.fimo.model.SurveyQuestion;
import id.go.fimo.model.User;

public class PreferenceManager {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private Context _context;

    private static final int PRIVATE_MODE = 0;
    private static final String PREF_NAME = BuildConfig.APPLICATION_ID;
    private static final String CURRENT_DATA = "current_data";
    private static final String DATA_MAP = "data_map";
    private static final String TOKEN = "token";
    private static final String USER_DATA = "user_data";
    private static final String SECTION_DATA = "section_data";
    private static final String LOGIN_KEY = "login_key";

    public PreferenceManager(Context context) {
        this._context = context;
        preferences = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = preferences.edit();
    }

    private static final String TAG = PreferenceManager.class.toString();

    public void putString(Context context, String key, String value){
        editor.putString(key, value).commit();
    }

    public String getString(Context context, String key){
        return preferences.getString(key, "");
    }

    public void putBoolean(Context context, String key, boolean value){
        editor.putBoolean(key, value).commit();
    }

    public boolean getBoolean(Context context, String key){
        return preferences.getBoolean(key, false);
    }
    public void setToken(Context context, String token){
        putString(context, TOKEN, token);
    }

    public String getToken(Context context){
        return getString(context, TOKEN);
    }

    public User getUser(Context context){
        try{
            String json = getString(context, USER_DATA);
            return new Gson().fromJson(json, User.class);
        }catch (Exception e){
            return null;
        }
    }

    public void setUser(Context context, User user){
        putString(context, USER_DATA, new Gson().toJson(user));
    }

    public void setLoginFlag(Context context, boolean flag){
        putBoolean(context, LOGIN_KEY, flag);
    }

    public boolean getLoginFlag(Context context){
        return getBoolean(context, LOGIN_KEY);
    }

    public void setData(Data data){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();
        if (data !=null) {
            String json = gson.toJson(data);
            editor.putString(CURRENT_DATA, json);
        }else{
            editor.putString(CURRENT_DATA, "");
        }
        editor.commit();
    }

    public Data getData(){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();
        String json = preferences.getString(CURRENT_DATA, "");
        if (json==null || json.isEmpty()){
            return null;
        }
        return gson.fromJson(json, Data.class);
    }

    public LinkedHashMap<String, Data> getDataMap(){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();
        String json = preferences.getString(DATA_MAP, "");
        if (json.equals("")){
            return new LinkedHashMap<>();
        }
        System.out.println("json:"+json);
        return gson.fromJson(json, new TypeToken<LinkedHashMap<String, Data>>() {}.getType());
    }

    public Data getData(String id){
        Map<String, Data> map = getDataMap();
        return map.get(id);
    }

    public void putData(String id, Data data){
        Map<String, Data> map = getDataMap();
        map.put(id, data);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();
        String json = gson.toJson(map);
        editor.putString(DATA_MAP, json);
        editor.commit();
    }

    public void deleteData(String id){
        Map<String, Data> map = getDataMap();
        map.remove(id);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();
        String json = gson.toJson(map);
        editor.putString(DATA_MAP, json);
        editor.commit();
    }

    public Data saveData(){
        Data data = getData();
        data.setFinishDate(new Date());
        putData(data.getId(), data);
        setData(null);
        setSurveyAnswers(new HashMap<Integer, List<QuestionParameter>>());
        return data;
    }

    private static final String SURVEY_QUESTIONS = "survey_questions";
    private static final String SURVEY_ANSWERS = "survey_answers";

    public void putSurveyQuestions(String key, List<SurveyQuestion> value){
        Map<String, List<SurveyQuestion>> map = getSurveyQuestions();
        map.put(key,value);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();
        String json = gson.toJson(map);
        editor.putString(SURVEY_QUESTIONS, json);
        editor.commit();
    }

    public void setSurveyQuestions(Map<String, List<SurveyQuestion>> map){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();
        String json = gson.toJson(map);
        editor.putString(SURVEY_QUESTIONS, json);
        editor.commit();
    }

    public List<SurveyQuestion> getSurveyQuestions(String sectionId){
        Map<String, List<SurveyQuestion>> map = getSurveyQuestions();
        return map.get(sectionId);
    }

    public Map<String, List<SurveyQuestion>> getSurveyQuestions(){
        try {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();
            String json = preferences.getString(SURVEY_QUESTIONS, "");
            if (json.equals("")){
                return new LinkedHashMap<>();
            }
            return gson.fromJson(json, new TypeToken<Map<String, List<SurveyQuestion>>>() {}.getType());
        }catch(Exception ex){
            return null;
        }
    }

    public void putSurveyAnswers(Integer key, List<QuestionParameter> value){
        System.out.println("putSurveyAnswers:"+key);
        Map<Integer, List<QuestionParameter>> map = getSurveyAnswers();
        map.put(key,value);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();
        String json = gson.toJson(map);
        editor.putString(SURVEY_ANSWERS, json);
        editor.commit();
    }

    public void setSurveyAnswers(Map<Integer, List<QuestionParameter>> map){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();
        String json = gson.toJson(map);
        editor.putString(SURVEY_ANSWERS, json);
        editor.commit();
    }

    public List<QuestionParameter> getSurveyAnswers(Integer question){
        Map<Integer, List<QuestionParameter>> map = getSurveyAnswers();
        return map.get(question);
    }

    public Map<Integer, List<QuestionParameter>> getSurveyAnswers(){
        try {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();
            String json = preferences.getString(SURVEY_ANSWERS, "");
            if (json.equals("")){
                return new LinkedHashMap<>();
            }
            return gson.fromJson(json, new TypeToken<Map<Integer, List<QuestionParameter>>>() {}.getType());
        }catch(Exception ex){
            return null;
        }
    }

    public void setSections(List<Section> list){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();
        String json = gson.toJson(list);
        editor.putString(SECTION_DATA, json);
        editor.commit();
    }

    public List<Section> getSections(){
        try {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization().create();
            String json = preferences.getString(SECTION_DATA, "");
            if (json.equals("")){
                return new ArrayList<>();
            }
            return gson.fromJson(json, new TypeToken<List<Section>>() {}.getType());
        }catch(Exception ex){
            return null;
        }
    }
}
