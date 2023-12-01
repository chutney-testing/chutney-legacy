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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { Metric } from '@core/model/metric.model';
import { PrometheusService } from '@core/services/prometheus.service';
import { NgbNavChangeEvent } from '@ng-bootstrap/ng-bootstrap';
import { filterOnTextContent } from '@shared/tools';
import { interval, Subscription } from 'rxjs';

@Component({
    selector: 'chutney-metrics',
    templateUrl: './metrics.component.html',
    styleUrls: ['./metrics.component.scss']
})
export class MetricsComponent implements OnInit, OnDestroy {

    metrics: Metric[] = [];
    chutneyMetrics: Metric[] = [];

    filtredMetrics: Metric[] = [];
    filtredChutneyMetrics: Metric[] = [];
    textFilter = '';

    activeTab = 'chutneyMetrics';
    autoRefresh = false;

    refreshSubscribe: Subscription;

    constructor(
        private prometheusService: PrometheusService) {
    }

    ngOnInit(): void {
        this.refreshMetrics();
    }

    ngOnDestroy(): void {
        this.refreshSubscribe?.unsubscribe();
    }

    onRefreshSwitchChange() {
        this.autoRefresh = !this.autoRefresh;
        if (this.autoRefresh) {
            this.refreshSubscribe = interval(10000)
                .subscribe(() => { this.refreshMetrics() });
        } else {
            this.refreshSubscribe.unsubscribe();
        }
    }

    onTabChange(changeEvent: NgbNavChangeEvent) {
        this.activeTab = changeEvent.nextId;
        this.updateTextFilter(this.textFilter);
    }

    updateTextFilter(text: string) {
        this.textFilter = text;
        if (this.activeTab === 'chutneyMetrics') {
            this.filtredChutneyMetrics = filterOnTextContent(this.chutneyMetrics, this.textFilter, ['name', 'tags']);
        } else {
            this.filtredMetrics = filterOnTextContent(this.metrics, this.textFilter, ['name', 'tags']);
        }
    }

    refreshMetrics() {
        const chutneyMetricPattern = '^scenario|^campaign';
        this.prometheusService.getMetrics()
            .subscribe(result => {
                this.metrics = result;
                this.chutneyMetrics = this.metrics.filter(metric => metric.name.match(chutneyMetricPattern));
                this.metrics = this.metrics.filter(metric => !metric.name.match(chutneyMetricPattern));
                this.updateTextFilter(this.textFilter);
            });
    }
}
