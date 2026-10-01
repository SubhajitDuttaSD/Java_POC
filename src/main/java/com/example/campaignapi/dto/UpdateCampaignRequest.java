package com.example.campaignapi.dto;

import jakarta.validation.constraints.Size;

public class UpdateCampaignRequest {

    @Size(min = 1, message = "Campaign name must not be blank")
    private String name;

    @Size(min = 1, message = "Campaign subject must not be blank")
    private String subject;

    public UpdateCampaignRequest() {
    }

    public UpdateCampaignRequest(String name, String subject) {
        this.name = name;
        this.subject = subject;
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
}
