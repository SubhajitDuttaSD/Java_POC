package com.example.campaignapi.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateCampaignRequest {

    @NotBlank(message = "Campaign name is required")
    private String name;

    @NotBlank(message = "Campaign subject is required")
    private String subject;

    public CreateCampaignRequest() {
    }

    public CreateCampaignRequest(String name, String subject) {
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