import { Component, ViewChild, Injectable } from '@angular/core';
import { TestBed, ComponentFixture } from '@angular/core/testing';

import { Authorization } from '@model';
import { LoginService } from '@core/services';
import { HasAuthorizationDirective } from './hasAuthorization.directive';

describe('hasAuthorization directive...', () => {

    const loginService = jasmine.createSpyObj('LoginService', ['hasAuthorization']);
    // Default stub
    loginService.hasAuthorization.and.returnValue(false);
    // Stub with arguments validation
    const authorizeEmptyAuthNoUser = function(a, u) { return Object.keys(a).length == 0 && !u; }
    const unauthorizeEmptyAuthNoUser = function(a, u) { return ! authorizeEmptyAuthNoUser(a, u); }

    const testInject = jasmine.createSpyObj('TestInject', ['authorizations']);
    testInject.authorizations.and.returnValue(null);

    let fixture: ComponentFixture<HasAuthorizationsHostComponent>;
    let testHostComponent: HasAuthorizationsHostComponent;

    beforeEach(() => {
        TestBed.resetTestingModule();
        TestBed.configureTestingModule({
            declarations: [
                HasAuthorizationsHostComponent,
                HasAuthorizationDirective
            ], providers: [
                { provide: LoginService, useValue: loginService },
                { provide: TestInject, useValue: testInject }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(HasAuthorizationsHostComponent);
        testHostComponent = fixture.componentInstance;
    });

    it('should error by default', () => {
        expect(fixture.detectChanges).toThrow();
    });

    describe('empty authorizations...', () => {
        beforeEach(() => {
            testInject.authorizations.and.returnValue({});
        });

        it('should render view when service authorize', () => {
            loginService.hasAuthorization.and.callFake(authorizeEmptyAuthNoUser);
            fixture.detectChanges();

            expect(testHostComponent).toBeDefined();
            expect(testHostComponent.spanView).toBeDefined();
        });

        it('should not render view when service unauthorize', () => {
            loginService.hasAuthorization.and.callFake(unauthorizeEmptyAuthNoUser);
            fixture.detectChanges();

            expect(testHostComponent).toBeDefined();
            expect(testHostComponent.spanView).toBeUndefined();
        });
    });

    describe('not logic empty authorizations...', () => {
        beforeEach(() => {
            testInject.authorizations.and.returnValue({ not: true });
        });

        it('should not render view when service authorize', () => {
            loginService.hasAuthorization.and.callFake(authorizeEmptyAuthNoUser);
            fixture.detectChanges();

            expect(testHostComponent).toBeDefined();
            expect(testHostComponent.spanView).toBeUndefined();
        });

        it('should render view when service unauthorize', () => {
            loginService.hasAuthorization.and.callFake(unauthorizeEmptyAuthNoUser);
            fixture.detectChanges();

            expect(testHostComponent).toBeDefined();
            expect(testHostComponent.spanView).toBeDefined();
        });
    });

    describe('should pass authorizations array to service...', () => {
        const auth_array = [Authorization.ADMIN_ACCESS,Authorization.SCENARIO_READ];

        it('direct', () => {
            testInject.authorizations.and.returnValue(auth_array);
            loginService.hasAuthorization.and.callFake(
                function(a, u) { return a === auth_array && !u; }
            );
            fixture.detectChanges();

            expect(testHostComponent).toBeDefined();
            expect(testHostComponent.spanView).toBeDefined();
        });

        it('object', () => {
            testInject.authorizations.and.returnValue({ authorizations: auth_array });
            loginService.hasAuthorization.and.callFake(
                function(a, u) { return a === auth_array && !u; }
            );
            fixture.detectChanges();

            expect(testHostComponent).toBeDefined();
            expect(testHostComponent.spanView).toBeDefined();
        });
    });

    it('should pass user to service...', () => {
        const userObj = {};

        testInject.authorizations.and.returnValue({user: userObj});
        loginService.hasAuthorization.and.callFake(
            function(a, u) { return Object.keys(a).length == 0 && u === userObj; }
        );
        fixture.detectChanges();

        expect(testHostComponent).toBeDefined();
        expect(testHostComponent.spanView).toBeDefined();
    });
});

@Injectable()
class TestInject {
    constructor() {}
    authorizations() {}
}

@Component({
    template: `<span *hasAuthorization="authorizations()" #spanView>wtf</span>`
})
class HasAuthorizationsHostComponent {
    @ViewChild('spanView', /* TODO: add static flag */ {}) spanView: any;
    constructor(private testInject: TestInject) {}
    authorizations() { return this.testInject.authorizations(); }
}
