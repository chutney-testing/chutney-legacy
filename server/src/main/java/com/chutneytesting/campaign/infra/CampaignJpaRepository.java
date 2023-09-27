package com.chutneytesting.campaign.infra;

import com.chutneytesting.campaign.infra.jpa.CampaignEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface CampaignJpaRepository extends CrudRepository<CampaignEntity, Long>, JpaSpecificationExecutor<CampaignEntity> {
    Optional<CampaignEntity> findByTitleAndEnvironment(String title, String environment);
}
