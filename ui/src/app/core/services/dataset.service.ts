import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@env/environment';
import { Campaign, CampaignExecutionReport, TestCase, ScenarioIndex, Dataset, KeyValue } from '@model';
import { HttpClient } from '@angular/common/http';
import { distinct } from '@shared/tools';

@Injectable({
    providedIn: 'root'
})
export class DataSetService {

    private resourceUrl = '';

    constructor(private http: HttpClient) { }

    findAll(): Observable<Array<Dataset>> {
        const mockDataSet1 = new Dataset('Name of the dataset 1',
                                    'description of the dataset 1', ['tag1', 'tag2'], new Date(), [], [], 'id1');
        const mockDataSet2 = new Dataset('Name of the dataset 2',
                                    'description of the dataset 2', ['tag3', 'tag4'], new Date(), [], [], 'id2');
        const mockDataSet3 = new Dataset('Name of the dataset 3',
                                    'description of the dataset 3', ['tag1', 'tag3'], new Date(), [], [], 'id3');
        const mockDataSet4 = new Dataset('Name of the dataset 4',
                                    'description of the dataset 4', ['tag2', 'tag3'], new Date(), [], [], 'id4');
        return of([mockDataSet1, mockDataSet2, mockDataSet3, mockDataSet4]);
    }

    findById(id: string): Observable<Dataset> {
        const uniquesValues = [new KeyValue('clef1', 'value1'), new KeyValue('clef2', 'value2'), new KeyValue('clef3', 'value3'),
                               new KeyValue('clef4', 'value4'), new KeyValue('clef5', 'value5'), new KeyValue('clef6', 'value6'),
                               new KeyValue('clef7', 'value4'), new KeyValue('clef8', 'value5'), new KeyValue('clef9', 'value6') ];
        const multipleValues = [
            [new KeyValue('clef1', 'value1'), new KeyValue('clef2', 'value2'), new KeyValue('clef3', 'value3'),
            new KeyValue('clef4', 'value4'), new KeyValue('clef5', 'value5'), new KeyValue('clef6', 'value6'),
            new KeyValue('clef7', 'value8'), new KeyValue('clef8', 'value5'), new KeyValue('clef9', 'value6')],
            [new KeyValue('clef1', 'value1'), new KeyValue('clef2', 'value2'), new KeyValue('clef3', 'value3'),
            new KeyValue('clef4', 'value4'), new KeyValue('clef5', 'value5'), new KeyValue('clef6', 'value6'),
            new KeyValue('clef7', 'value8'), new KeyValue('clef8', 'value5'), new KeyValue('clef9', 'value6')],
            [new KeyValue('clef1', 'value1'), new KeyValue('clef2', 'value2'), new KeyValue('clef3', 'value3'),
            new KeyValue('clef4', 'value4'), new KeyValue('clef5', 'value5'), new KeyValue('clef6', 'value6'),
            new KeyValue('clef7', 'value8'), new KeyValue('clef8', 'value5'), new KeyValue('clef9', 'value6')],
            [new KeyValue('clef1', 'value1'), new KeyValue('clef2', 'value2'), new KeyValue('clef3', 'value3'),
            new KeyValue('clef4', 'value4'), new KeyValue('clef5', 'value5'), new KeyValue('clef6', 'value6'),
            new KeyValue('clef7', 'value8'), new KeyValue('clef8', 'value5'), new KeyValue('clef9', 'value6')],
            [new KeyValue('clef1', 'value1'), new KeyValue('clef2', 'value2'), new KeyValue('clef3', 'value3'),
            new KeyValue('clef4', 'value4'), new KeyValue('clef5', 'value5'), new KeyValue('clef6', 'value6'),
            new KeyValue('clef7', 'value8'), new KeyValue('clef8', 'value5'), new KeyValue('clef9', 'value6')],
            [new KeyValue('clef1', 'value1'), new KeyValue('clef2', 'value2'), new KeyValue('clef3', 'value3'),
            new KeyValue('clef4', 'value4'), new KeyValue('clef5', 'value5'), new KeyValue('clef6', 'value6'),
            new KeyValue('clef7', 'value8'), new KeyValue('clef8', 'value5'), new KeyValue('clef9', 'value6')]
        ];
        const mockDataSet = new Dataset('Name of the dataset',
                                    'description of the dataset', ['tag1', 'tag2'], new Date(), uniquesValues, multipleValues, 'id');
        return of(mockDataSet);
    }
}
