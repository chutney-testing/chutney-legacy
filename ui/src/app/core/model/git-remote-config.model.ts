export class GitRemoteConfig {
    constructor(
      public name: string,
      public url: string,
      public branch: string,
      public privateKeyPath: string,
      public privateKeyPassphrase: string,
    ) { }
  }
