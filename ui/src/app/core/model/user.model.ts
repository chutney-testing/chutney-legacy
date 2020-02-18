
export class User {
  constructor(
    public id: string,
    public name?: string,
    public firstname?: string,
    public lastname?: string,
    public mail?: string,
  ) { }
}

export class UserSession {
  constructor(
    public user: User,
    public startTime: number
  ) { }
}
