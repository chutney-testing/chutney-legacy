/**
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



