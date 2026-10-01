package com.example.campaignapi.exception;

public class CampaignNotFoundException extends RuntimeException {

    public CampaignNotFoundException(String id) {
        super("Campaign not found: " + id);
    }
}
