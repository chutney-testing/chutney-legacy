package com.chutneytesting.design.api.campaign;

import static com.chutneytesting.design.domain.campaign.FREQUENCY.tofrequency;

import com.chutneytesting.design.domain.campaign.SchedulingCampaign;
import com.chutneytesting.design.domain.campaign.SchedulingCampaignRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ui/campaign/v1/scheduling")
@CrossOrigin(origins = "*")
public class ScheduleCampaignController {

    private final SchedulingCampaignRepository schedulingCampaignRepository;

    public ScheduleCampaignController(SchedulingCampaignRepository schedulingCampaignRepository) {
        this.schedulingCampaignRepository = schedulingCampaignRepository;
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_READ')")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SchedulingCampaignDto> getAll() {
        return schedulingCampaignRepository.getALl().stream()
            .map(sc -> new SchedulingCampaignDto(sc.id, sc.campaignId, sc.campaignTitle, sc.getSchedulingDate(), sc.frequency.label))
            .sorted(Comparator.comparing(SchedulingCampaignDto::getSchedulingDate))
            .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_WRITE')")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void add(@RequestBody SchedulingCampaignDto dto) {
        schedulingCampaignRepository.add(new SchedulingCampaign(null, dto.getCampaignId(), dto.getCampaignTitle(), dto.getSchedulingDate(), tofrequency(dto.getFrequency())));
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_WRITE')")
    @DeleteMapping(path = "/{schedulingCampaignId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@PathVariable("schedulingCampaignId") Long schedulingCampaignId) {
        schedulingCampaignRepository.removeById(schedulingCampaignId);
    }

}
