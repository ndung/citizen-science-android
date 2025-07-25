package id.go.fimo.model;

import java.io.Serializable;
import java.util.Date;

public class Image implements Serializable {

    private String id;
    private Section section;
    private Date createAt;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public Section getSection() { return section; }

    public void setSection(Section section) { this.section = section; }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }
}
