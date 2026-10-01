package com.example.campaignapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    private String id;
    private String name;
    private String subject;
    @Enumerated(EnumType.STRING)
    private CampaignStatus status;
    private boolean deleted = false;

    public Campaign() {
    }

    public Campaign(String id, String name, String subject, CampaignStatus status) {
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
