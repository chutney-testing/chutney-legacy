import { Meta, moduleMetadata, Story } from '@storybook/angular';
import { ChutneyMainHeaderComponent } from '@shared/components/layout/header/chutney-main-header.component';
import { LoginService } from '@core/services';
import { Authorization, User } from '@model';
import { Observable, of } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { TranslateTestingModule } from '../../app/testing/translate-testing.module';
import { Theme } from '@core/theme/theme';

const mockLoginService = {
    hasAuthorization(authorization: Array<Authorization> | Authorization = [], u: User = null): boolean {
        return true
    },
    isAuthenticated(): boolean { return true },
    getUser(): Observable<User> { return of(new User('user_id', 'username', 'firstname'))  }
};

export default {
    title: 'Components/Main header',
    component: ChutneyMainHeaderComponent,
    decorators: [
        moduleMetadata({
            imports: [TranslateModule, TranslateTestingModule],
            providers: [
                { provide: LoginService, useValue: mockLoginService }
            ]
        }),
    ],
    args: {}
} as Meta;

const Template: Story<ChutneyMainHeaderComponent> = (args: ChutneyMainHeaderComponent) => ({
    props: args,
});

export const Default = Template.bind({});
Default.args = {
    // args are taken from component level args.
};

