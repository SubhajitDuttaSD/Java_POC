package com.example.campaignapi.repository;

import com.example.campaignapi.model.Campaign;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, String> {

    List<Campaign> findAllByDeletedFalse();

    Optional<Campaign> findByIdAndDeletedFalse(String id);

    boolean existsByNameIgnoreCaseAndDeletedFalseAndIdNot(String name, String id);
}
