package id.go.fimo.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.List;

public class SurveyQuestion implements Serializable, Comparable<SurveyQuestion> {

    @Expose
    private Integer id;
    @Expose
    private String attribute;
    private Section section;
    private String question;
    private Integer type;
    boolean required;
    private List<QuestionParameter> parameters;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAttribute() { return attribute; }

    public void setAttribute(String attribute) { this.attribute = attribute; }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public List<QuestionParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<QuestionParameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "SurveyQuestion{" +
                "id=" + id +
                ", section=" + section +
                ", question='" + question + '\'' +
                ", type=" + type +
                ", required=" + required +
                ", parameters=" + parameters +
                '}';
    }

    @Override
    public int compareTo(SurveyQuestion surveyQuestion) {
        if(id==surveyQuestion.id)
            return 0;
        else if(id>surveyQuestion.id)
            return 1;
        else
            return -1;
    }
}
