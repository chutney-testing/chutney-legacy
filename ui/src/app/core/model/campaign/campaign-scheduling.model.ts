import { FREQUENCY } from '@core/model/campaign/FREQUENCY';


export class CampaignScheduling {

    constructor(
        public campaignId: number,
        public campaignTitle: string,
        public schedulingDate: Date,
        public frequency?: FREQUENCY,
        public id?: number
    ) {
    }
}
