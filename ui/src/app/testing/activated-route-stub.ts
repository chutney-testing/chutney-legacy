import { of } from 'rxjs';
import { convertToParamMap } from '@angular/router';

export class ActivatedRouteStub {

    params = {};

    snapshot = { queryParamMap: {
        }
    };

    constructor() {
    }

    setParamMap(params?: Object) {
        this.params = of(params);
    }

    setSnapshotQueryParamMap(params?: Object) {
        this.snapshot.queryParamMap = convertToParamMap(params);
    }
}
