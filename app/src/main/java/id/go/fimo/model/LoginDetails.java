package id.go.fimo.model;

import java.util.List;

public class LoginDetails {
    private User user;
    private List<Section> sections;
    private List<SurveyQuestion> questions;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Section> getSections() { return sections; }

    public void setSections(List<Section> sections) { this.sections = sections; }

    public List<SurveyQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<SurveyQuestion> questions) {
        this.questions = questions;
    }

}
