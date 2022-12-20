import { BackupsService } from './backups.service';
import { Backup } from '@core/model/backups.model';
import { of } from 'rxjs';

describe('BackupsService', () => {

   let sut: BackupsService;
   let httpClientSpy: { get: jasmine.Spy };

  beforeEach(() => {
    httpClientSpy = jasmine.createSpyObj('HttpClient', ['get']);
    sut = new BackupsService(<any> httpClientSpy);
  });

  it('should list available backups (HttpClient called once)', () => {
      const backup = new Backup(["backupable"]);
      const expectedBackups: Backup[] = [backup];

    httpClientSpy.get.and.returnValue(of(expectedBackups));

    sut.list()
        .subscribe({
            next: backups => expect(backups).withContext('expected backups').toEqual(expectedBackups,),
            error: fail
        }
    );
    expect(httpClientSpy.get.calls.count()).withContext('one call').toBe(1, );
  });
});



