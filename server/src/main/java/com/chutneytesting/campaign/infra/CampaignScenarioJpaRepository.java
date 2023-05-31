package com.chutneytesting.campaign.infra;

import com.chutneytesting.campaign.infra.jpa.CampaignScenario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface CampaignScenarioJpaRepository extends CrudRepository<CampaignScenario, Long>, JpaSpecificationExecutor<CampaignScenario> {

    List<CampaignScenario> findAllByScenarioId(String scenarioId);
}
