package com.chutneytesting.campaign.infra;

import com.chutneytesting.campaign.infra.jpa.CampaignExecution;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface CampaignExecutionJpaRepository extends CrudRepository<CampaignExecution, Long>, JpaSpecificationExecutor<CampaignExecution> {

    List<CampaignExecution> findFirst20ByCampaignIdOrderByIdDesc(Long campaignId);

    List<CampaignExecution> findAllByCampaignId(Long campaignId);

    @Query(value = "SELECT * FROM CAMPAIGN_EXECUTIONS campaignExec WHERE campaignExec.CAMPAIGN_ID = :campaignId ORDER BY campaignExec.ID DESC LIMIT 1", nativeQuery = true)
    Optional<CampaignExecution> findLastByCampaignId(@Param("campaignId") Long campaignId);
}
