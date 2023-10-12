package com.chutneytesting.campaign.infra;

import com.chutneytesting.campaign.infra.jpa.CampaignScenarioEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface CampaignScenarioJpaRepository extends CrudRepository<CampaignScenarioEntity, Long>, JpaSpecificationExecutor<CampaignScenarioEntity> {

    List<CampaignScenarioEntity> findAllByScenarioId(String scenarioId);
}
