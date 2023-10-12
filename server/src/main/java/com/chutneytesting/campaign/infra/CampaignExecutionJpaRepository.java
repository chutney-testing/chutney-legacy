package com.chutneytesting.campaign.infra;

import com.chutneytesting.campaign.infra.jpa.CampaignExecution;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CampaignExecutionJpaRepository extends JpaRepository<CampaignExecution, Long>, JpaSpecificationExecutor<CampaignExecution> {

    List<CampaignExecution> findByCampaignIdOrderByIdDesc(Long campaignId);

    List<CampaignExecution> findAllByCampaignId(Long campaignId);

    Optional<CampaignExecution> findFirstByCampaignIdOrderByIdDesc(Long campaignId);
}
