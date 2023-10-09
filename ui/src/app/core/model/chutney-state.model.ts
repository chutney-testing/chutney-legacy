export class ChutneyState {
  constructor(
    public tags: Array<String> = [],
    public campaignTags: Array<String> = [],
    public noTag: boolean,
    public campaignNoTag: boolean
  ) { }
}
