import { BackupsService, BackupDto } from './backups.service';
import { Backup } from '@core/model/backups.model';
import { of } from 'rxjs';

describe('BackupsService', () => {

   let sut: BackupsService;
   let httpClientSpy: { get: jasmine.Spy };

  beforeEach(() => {
    httpClientSpy = jasmine.createSpyObj('HttpClient', ['get']);
    sut = new BackupsService(<any> httpClientSpy);
  });

  it('should return expected backups (HttpClient called once)', () => {
    const expectedBackups: BackupDto[] =
      [new BackupDto(true, true, true, true, true, true)];

    httpClientSpy.get.and.returnValue(of(expectedBackups));

    sut.list().subscribe(
      backups => expect(backups).toEqual([new Backup(true, true, true, true, true, true)], 'expected backups'),
      fail
    );
    expect(httpClientSpy.get.calls.count()).toBe(1, 'one call');
  });
});



