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

it('isValidEnvironmentName returns false on null', () => {
  expect(service.isValidEnvironmentName(null)).toBe(false);
});

it('isValidEnvironmentName returns false on empty string', () => {
  expect(service.isValidEnvironmentName('')).toBe(false);
});

it('isValidEnvironmentName returns false on lower case', () => {
  expect(service.isValidEnvironmentName('test')).toBe(false);
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
