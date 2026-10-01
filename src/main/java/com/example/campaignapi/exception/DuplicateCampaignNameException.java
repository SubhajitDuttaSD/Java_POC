package com.example.campaignapi.exception;

public class DuplicateCampaignNameException extends RuntimeException {

    public DuplicateCampaignNameException(String name) {
        super("A campaign named '" + name + "' already exists");
    }
}
