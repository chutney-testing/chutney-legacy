import { Meta, moduleMetadata, Story } from '@storybook/angular';
import { LoginComponent } from '@core/components/login/login.component';
import { InfoService, LoginService } from '@core/services';
import { Authorization, User } from '@model';
import { intersection } from '@shared/tools';
import { ActivatedRoute } from '@angular/router';
import { Observable, of } from 'rxjs';

const mockLoginService = {
    hasAuthorization(authorization: Array<Authorization> | Authorization = [], u: User = null) {
        return !authorization.length || intersection([Authorization.SCENARIO_EXECUTE], [...authorization]).length;
    },
    isAuthenticated(): boolean { return false }
};

const mockInfoService = {
    getVersion(): Observable<string> {
        return of('fake.version');
    },
    getApplicationName(): Observable<string> {
        return of('app-name');
    }
};

export default {
    title: 'Pages/Login',
    component: LoginComponent,
    decorators: [
        moduleMetadata({
            imports: [],
            providers: [
                { provide: LoginService, useValue: mockLoginService },
                { provide: InfoService, useValue: mockInfoService },
                { provide: ActivatedRoute, useValue: { params: of([{action: 'login'}]), queryParams: of([{url: '/'}]) } }
            ]
        })
    ],
    args: {
        applicationName: 'Chutney Instance App Name',
        connectionError: '',
        version: '1.0.0-RELEASE'
    }
} as Meta;

const Template: Story = (args) => ({
    props: args,
});

export const Default = Template.bind({});
Default.args = {
    // args are taken from component level args.
};

export const Error = Template.bind({});
Error.args = {
    ...Default.args,
    connectionError: 'this is an example error message',
};

export const LongValues = Template.bind({});
LongValues.args = {
    ...Default.args,
    applicationName: 'This is a very long example of an application name, This is a very long example of an application name',
    connectionError: 'This is a very long example of an error message, This is a very long example of an error message, This is a very long example of an error message, ' +
                     'This is a very long example of an error message, This is a very long example of an error message, This is a very long example of an error message',
    version: 'This is a very long example of a version',
};
