package id.go.fimo.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Data implements Serializable {

    private String id;
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private Date startDate;
    private Date finishDate;
    private boolean editMode;
    private boolean uploading;
    private boolean uploaded;
    private Date createAt;
    private Map<String,List<String>> imagePaths;
    private List<Image> images;
    private Map<Integer, List<QuestionParameter>> survey;
    private List<SurveyResponse> surveyResponses;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAccuracy() { return accuracy; }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public boolean isUploading() { return uploading; }

    public void setUploading(boolean uploading) { this.uploading = uploading; }

    public boolean isUploaded() { return uploaded; }

    public void setUploaded(boolean uploaded) { this.uploaded = uploaded; }

    public Date getCreateAt() { return createAt; }

    public void setCreateAt(Date createAt) { this.createAt = createAt; }

    public Map<String,List<String>> getImagePaths() { return imagePaths; }

    public void setImagePaths(Map<String,List<String>> imagePaths) { this.imagePaths = imagePaths; }

    public Map<Integer, List<QuestionParameter>> getSurvey() { return survey; }

    public void setSurvey(Map<Integer, List<QuestionParameter>> survey) { this.survey = survey; }

    public List<Image> getImages() { return images; }

    public void setImages(List<Image> images) { this.images = images; }

    public List<SurveyResponse> getSurveyResponses() { return surveyResponses; }

    public void setSurveyResponses(List<SurveyResponse> surveyResponses) { this.surveyResponses = surveyResponses; }

    @Override
    public String toString() {
        return "Record{" +
                "id='" + id + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", accuracy=" + accuracy +
                ", startDate=" + startDate +
                ", finishDate=" + finishDate +
                ", editMode=" + editMode +
                ", imagePaths=" + imagePaths +
                ", images=" + images +
                '}';
    }

}
