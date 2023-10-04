package com.chutneytesting.campaign.infra;

import com.chutneytesting.campaign.infra.jpa.CampaignExecution;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface CampaignExecutionJpaRepository extends CrudRepository<CampaignExecution, Long>, JpaSpecificationExecutor<CampaignExecution> {

    List<CampaignExecution> findFirst20ByCampaignIdOrderByIdDesc(Long campaignId);

    List<CampaignExecution> findAllByCampaignId(Long campaignId);

    Optional<CampaignExecution> findFirstByCampaignIdOrderByIdDesc(Long campaignId);
}
