package com.example.campaignapi.dto;

import com.example.campaignapi.model.CampaignStatus;

public class CampaignResponse {

    private String id;
    private String name;
    private String subject;
    private CampaignStatus status;

    public CampaignResponse() {
    }

    public CampaignResponse(
            String id,
            String name,
            String subject,
            CampaignStatus status
    ) {
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
}