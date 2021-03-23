export class CampaignScheduling {

    constructor(
        public campaignId: number,
        public campaignTitle: string,
        public schedulingDate: Date,
        public frequency?: String,
        public id?: number
        ) {
    }
}
