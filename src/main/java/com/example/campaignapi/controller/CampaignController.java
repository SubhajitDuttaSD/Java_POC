package com.example.campaignapi.controller;

import com.example.campaignapi.service.CampaignService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.campaignapi.dto.CampaignResponse;
import com.example.campaignapi.dto.CreateCampaignRequest;
import com.example.campaignapi.dto.UpdateCampaignRequest;
import com.example.campaignapi.dto.ValidationErrorResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.ResponseEntity;
import java.net.URI;

import jakarta.validation.Valid;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/campaigns")
@Tag(name = "Campaigns", description = "Create and manage email campaigns")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key"),
        @ApiResponse(responseCode = "403", description = "API key lacks the required scope"),
        @ApiResponse(responseCode = "429", description = "Too many requests")
})
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping
    @Operation(summary = "List campaigns", description = "Returns all campaigns that have not been deleted.")
    @ApiResponse(responseCode = "200", description = "Campaigns returned",
            content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(
                    schema = @Schema(implementation = CampaignResponse.class))))
    public List<CampaignResponse> getCampaigns() {
        return campaignService.getCampaigns();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a campaign by ID")
    @ApiResponse(responseCode = "200", description = "Campaign found",
            content = @Content(schema = @Schema(implementation = CampaignResponse.class)))
    @ApiResponse(responseCode = "404", description = "Campaign not found")
    public CampaignResponse getCampaignById(@PathVariable String id) {
        return campaignService.getCampaignById(id);
    }

    @PostMapping
    @Operation(summary = "Create a campaign", description = "New campaigns are always created in DRAFT status.")
    @ApiResponse(responseCode = "201", description = "Campaign created")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    public ResponseEntity<CampaignResponse> createCampaign(
            @Valid @RequestBody CreateCampaignRequest request) {

        CampaignResponse response = campaignService.createCampaign(request);

        URI location = URI.create("/api/v1/campaigns/" + response.getId());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a campaign",
            description = "Partially updates name and/or subject. Renaming a campaign to another "
                    + "campaign's name (case-insensitive) is rejected; renaming to its own name "
                    + "in different casing is allowed.")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Campaign not found")
    @ApiResponse(responseCode = "409", description = "Another campaign already has this name")
    public CampaignResponse updateCampaign(
            @PathVariable String id,
            @Valid @RequestBody UpdateCampaignRequest request) {

        return campaignService.updateCampaign(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a campaign",
            description = "Soft delete: the campaign is hidden from all reads but kept in storage.")
    @ApiResponse(responseCode = "204", description = "Campaign deleted")
    @ApiResponse(responseCode = "404", description = "Campaign not found")
    public ResponseEntity<Void> deleteCampaign(@PathVariable String id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }
}
