/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.campaign.infra;

import com.chutneytesting.campaign.infra.jpa.CampaignEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CampaignJpaRepository extends CrudRepository<CampaignEntity, Long>, JpaSpecificationExecutor<CampaignEntity> {

    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO CAMPAIGN (ID, TITLE, DESCRIPTION) VALUES (:id, :title, :description)")
    void saveWithExplicitId(
        Long id,
        String title,
        String description);
}
