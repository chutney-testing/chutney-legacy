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

import { TestBed } from '@angular/core/testing';
import { ValidationService } from './validation.service';

let service: ValidationService;

beforeEach(() => {
  TestBed.configureTestingModule({ providers: [ValidationService] });
  service = TestBed.get(ValidationService);
});

afterEach(() => {
  TestBed.resetTestingModule();
});

it('isNotEmpty returns false on empty string', () => {
  expect(service.isNotEmpty('')).toBe(false);
});

it('isNotEmpty returns false on null', () => {
  expect(service.isNotEmpty(null)).toBe(false);
});

it('isNotEmpty returns true on non-empty string', () => {
  expect(service.isNotEmpty('test')).toBe(true);
});

it('isValidUrl returns false on null', () => {
  expect(service.isValidUrl(null)).toBe(false);
});

it('isValidUrl returns true on missing port URL', () => {
  expect(service.isValidUrl('test://test:')).toBe(true);
});

it('isValidUrl returns true on valid URL', () => {
  expect(service.isValidUrl('test://test:42')).toBe(true);
});

it('isValidUrl returns true on protocol with number', () => {
  expect(service.isValidUrl('t3://host:1234')).toBe(true);
});

it('isValidUrl returns false when protocol does not start with letter', () => {
  expect(service.isValidUrl('1protocol://host:1234')).toBe(false);
});

it('isValidUrl returns true on protocol with : - . +', () => {
  expect(service.isValidUrl('pro+to-co.l://host:1234')).toBe(true);
});

it('isValidUrl returns true on protocol length is 1', () => {
  expect(service.isValidUrl('p://host:1234')).toBe(true);
});

it('isValidEnvironmentName returns false on null', () => {
  expect(service.isValidEnvironmentName(null)).toBe(false);
});

it('isValidEnvironmentName returns false on empty string', () => {
  expect(service.isValidEnvironmentName('')).toBe(false);
});

it('isValidEnvironmentName returns true on lower case', () => {
  expect(service.isValidEnvironmentName('test')).toBe(true);
});

it('isValidEnvironmentName returns false on space', () => {
  expect(service.isValidEnvironmentName('TEST TEST')).toBe(false);
});

it('isValidEnvironmentName returns false short string', () => {
  expect(service.isValidEnvironmentName('TE')).toBe(false);
});

it('isValidEnvironmentName returns true when valid environment name', () => {
  expect(service.isValidEnvironmentName('TEST_45-2')).toBe(true);
});

it('isValidSpel returns false on null', () => {
    expect(service.isValidSpel(null)).toBe(false);
});

it('isValidSpel returns false with empty spel', () => {
    expect(service.isValidSpel('${}')).toBe(false);
});

it('isValidSpel returns true on valid spel', () => {
    expect(service.isValidSpel('${test}')).toBe(true);
});
