package com.example.campaignapi.service;

import com.example.campaignapi.dto.CampaignResponse;
import com.example.campaignapi.dto.CreateCampaignRequest;
import com.example.campaignapi.dto.UpdateCampaignRequest;
import com.example.campaignapi.exception.CampaignNotFoundException;
import com.example.campaignapi.exception.DuplicateCampaignNameException;
import com.example.campaignapi.model.Campaign;
import com.example.campaignapi.model.CampaignStatus;
import com.example.campaignapi.repository.CampaignRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    private CampaignService campaignService;

    @BeforeEach
    void setUp() {
        campaignService = new CampaignService(campaignRepository);
    }

    @Test
    void getCampaigns_excludesDeleted_andMapsToResponses() {
        Campaign campaign = new Campaign("cmp_1", "Spring Sale", "20% off", CampaignStatus.DRAFT);
        when(campaignRepository.findAllByDeletedFalse()).thenReturn(List.of(campaign));

        List<CampaignResponse> result = campaignService.getCampaigns();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("cmp_1");
        assertThat(result.get(0).getName()).isEqualTo("Spring Sale");
    }

    @Test
    void getCampaignById_whenFound_returnsResponse() {
        Campaign campaign = new Campaign("cmp_1", "Spring Sale", "20% off", CampaignStatus.DRAFT);
        when(campaignRepository.findByIdAndDeletedFalse("cmp_1")).thenReturn(Optional.of(campaign));

        CampaignResponse response = campaignService.getCampaignById("cmp_1");

        assertThat(response.getName()).isEqualTo("Spring Sale");
    }

    @Test
    void getCampaignById_whenMissing_throwsNotFound() {
        when(campaignRepository.findByIdAndDeletedFalse("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campaignService.getCampaignById("missing"))
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void createCampaign_generatesPrefixedId_andDraftStatus() {
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CampaignResponse response = campaignService.createCampaign(
                new CreateCampaignRequest("Spring Sale", "20% off"));

        assertThat(response.getId()).startsWith("cmp_");
        assertThat(response.getName()).isEqualTo("Spring Sale");
        assertThat(response.getStatus()).isEqualTo(CampaignStatus.DRAFT);
    }

    @Test
    void updateCampaign_whenNameTaken_throwsDuplicate_andDoesNotSave() {
        Campaign existing = new Campaign("cmp_1", "Old Name", "Subject", CampaignStatus.DRAFT);
        when(campaignRepository.findByIdAndDeletedFalse("cmp_1")).thenReturn(Optional.of(existing));
        when(campaignRepository.existsByNameIgnoreCaseAndDeletedFalseAndIdNot("New Name", "cmp_1"))
                .thenReturn(true);

        assertThatThrownBy(() -> campaignService.updateCampaign("cmp_1",
                new UpdateCampaignRequest("New Name", null)))
                .isInstanceOf(DuplicateCampaignNameException.class);

        verify(campaignRepository, never()).save(any());
    }

    @Test
    void updateCampaign_renameToOwnNameInDifferentCase_isAllowed() {
        Campaign existing = new Campaign("cmp_1", "Spring Sale", "Subject", CampaignStatus.DRAFT);
        when(campaignRepository.findByIdAndDeletedFalse("cmp_1")).thenReturn(Optional.of(existing));
        when(campaignRepository.existsByNameIgnoreCaseAndDeletedFalseAndIdNot("SPRING SALE", "cmp_1"))
                .thenReturn(false);
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CampaignResponse response = campaignService.updateCampaign("cmp_1",
                new UpdateCampaignRequest("SPRING SALE", null));

        assertThat(response.getName()).isEqualTo("SPRING SALE");
    }

    @Test
    void updateCampaign_withOnlySubject_leavesNameUnchanged_andSkipsDuplicateCheck() {
        Campaign existing = new Campaign("cmp_1", "Spring Sale", "Old Subject", CampaignStatus.DRAFT);
        when(campaignRepository.findByIdAndDeletedFalse("cmp_1")).thenReturn(Optional.of(existing));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CampaignResponse response = campaignService.updateCampaign("cmp_1",
                new UpdateCampaignRequest(null, "New Subject"));

        assertThat(response.getName()).isEqualTo("Spring Sale");
        assertThat(response.getSubject()).isEqualTo("New Subject");
        verify(campaignRepository, never())
                .existsByNameIgnoreCaseAndDeletedFalseAndIdNot(anyString(), anyString());
    }

    @Test
    void updateCampaign_whenMissing_throwsNotFound() {
        when(campaignRepository.findByIdAndDeletedFalse("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campaignService.updateCampaign("missing",
                new UpdateCampaignRequest("X", null)))
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void deleteCampaign_softDeletes() {
        Campaign existing = new Campaign("cmp_1", "Spring Sale", "Subject", CampaignStatus.DRAFT);
        when(campaignRepository.findByIdAndDeletedFalse("cmp_1")).thenReturn(Optional.of(existing));

        campaignService.deleteCampaign("cmp_1");

        ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
        verify(campaignRepository).save(captor.capture());
        assertThat(captor.getValue().isDeleted()).isTrue();
    }

    @Test
    void deleteCampaign_whenMissing_throwsNotFound() {
        when(campaignRepository.findByIdAndDeletedFalse("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campaignService.deleteCampaign("missing"))
                .isInstanceOf(CampaignNotFoundException.class);
    }
}
