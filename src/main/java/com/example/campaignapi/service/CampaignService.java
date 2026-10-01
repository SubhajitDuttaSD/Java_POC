package com.example.campaignapi.service;

import com.example.campaignapi.exception.CampaignNotFoundException;
import com.example.campaignapi.exception.DuplicateCampaignNameException;
import com.example.campaignapi.model.Campaign;
import com.example.campaignapi.repository.CampaignRepository;

import com.example.campaignapi.dto.CampaignResponse;
import com.example.campaignapi.dto.CreateCampaignRequest;
import com.example.campaignapi.dto.UpdateCampaignRequest;
import com.example.campaignapi.model.CampaignStatus;


import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.List;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;

    public CampaignService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    public List<CampaignResponse> getCampaigns() {
        return campaignRepository.findAllByDeletedFalse()
                .stream()
                .map(campaign -> new CampaignResponse(
                        campaign.getId(),
                        campaign.getName(),
                        campaign.getSubject(),
                        campaign.getStatus()
                ))
                .toList();
    }

    public CampaignResponse getCampaignById(String id) {
        Campaign campaign = findActiveCampaignOrThrow(id);

        return new CampaignResponse(
                campaign.getId(),
                campaign.getName(),
                campaign.getSubject(),
                campaign.getStatus()
        );
    }

    public CampaignResponse createCampaign(CreateCampaignRequest request) {
        String id = "cmp_" + UUID.randomUUID();
        Campaign campaign = new Campaign(
                id,
                request.getName(),
                request.getSubject(),
                CampaignStatus.DRAFT
        );
        Campaign savedCampaign = campaignRepository.save(campaign);
        return new CampaignResponse(
                savedCampaign.getId(),
                savedCampaign.getName(),
                savedCampaign.getSubject(),
                savedCampaign.getStatus()
        );
    }

    public CampaignResponse updateCampaign(String id, UpdateCampaignRequest request) {
        Campaign campaign = findActiveCampaignOrThrow(id);

        if (request.getName() != null) {
            if (campaignRepository.existsByNameIgnoreCaseAndDeletedFalseAndIdNot(request.getName(), id)) {
                throw new DuplicateCampaignNameException(request.getName());
            }
            campaign.setName(request.getName());
        }

        if (request.getSubject() != null) {
            campaign.setSubject(request.getSubject());
        }

        Campaign savedCampaign = campaignRepository.save(campaign);
        return new CampaignResponse(
                savedCampaign.getId(),
                savedCampaign.getName(),
                savedCampaign.getSubject(),
                savedCampaign.getStatus()
        );
    }

    public void deleteCampaign(String id) {
        Campaign campaign = findActiveCampaignOrThrow(id);
        campaign.setDeleted(true);
        campaignRepository.save(campaign);
    }

    private Campaign findActiveCampaignOrThrow(String id) {
        return campaignRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CampaignNotFoundException(id));
    }
}