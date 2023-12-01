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

import { Observable, Observer, Subscription } from 'rxjs';
import { Injectable } from '@angular/core';
import { filter, share } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class EventManagerService {

    observable: Observable<any>;
    observer: Observer<any>;

    constructor() {
        this.observable = Observable.create((observer: Observer<any>) => {
            this.observer = observer;
        }).pipe(share());
    }

    /**
     * Method to broadcast the event to observer
     */
    broadcast(event) {
        if (this.observer != null) {
            this.observer.next(event);
        }
    }

    /**
     * Method to subscribe to an event with callback
     */
    subscribe(eventName, callback) {
        return this.observable.pipe(filter((event) => {
            return event.name === eventName;
        })).subscribe(callback);
    }

    /**
     * Method to unsubscribe the subscription
     */
    destroy(subscriber: Subscription) {
        subscriber && subscriber.unsubscribe();
    }
}
