import { Component, OnDestroy, OnInit } from '@angular/core';
import { PrometheusService } from '@core/services/prometheus.service';
import { NgbNavChangeEvent } from '@ng-bootstrap/ng-bootstrap';
import { filterOnTextContent } from '@shared/tools';
import { interval, Observable, Subscription } from 'rxjs';

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
        const metricRegex = new RegExp('(?<name>[^{]*)(?<tags>{.*})? (?<value>.*)');
        const chutneyMetricPattern = '^scenario|^campaign';
        this.prometheusService.getMetrics()
            .subscribe(result => {
                this.metrics = result.split('\n')
                    .filter(element => element && !element.startsWith('#'))
                    .map(element => {
                        const [, name, tags, value] = metricRegex.exec(element) || [];
                        return new Metric(name, tags, value);
                    });
                this.chutneyMetrics = this.metrics.filter(metric => metric.name.match(chutneyMetricPattern));
                this.metrics = this.metrics.filter(metric => !metric.name.match(chutneyMetricPattern));
                this.updateTextFilter(this.textFilter);
            });
    }
}

class Metric {
    constructor(
        public name: string,
        public tags: string,
        public value: string
    ) { }
}
