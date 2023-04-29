package com.chutneytesting.campaign.infra;

import com.chutneytesting.campaign.infra.jpa.CampaignExecution;
import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.test.context.support.WithUserDetails;

public interface CampaignExecutionJpaRepository extends CrudRepository<CampaignExecution, Long>, JpaSpecificationExecutor<CampaignExecution> {

    List<CampaignExecution> findFirst20ByCampaignIdOrderByIdDesc(Long campaignId);

    List<CampaignExecution> findAllByCampaignId(Long campaignId);
}
