import { Meta, moduleMetadata, Story } from '@storybook/angular';
import { Authorization, User } from '@model';
import { SharedModule } from '@shared/shared.module';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { APP_BASE_HREF } from '@angular/common';
import { TranslateTestingModule } from '../../app/testing/translate-testing.module';
import { userEvent, waitFor, within } from '@storybook/testing-library';
import { expect, jest } from '@storybook/jest';
import { ChutneyRightMenuComponent } from '@shared/components/layout/right-menu/chutney-right-menu.component';
import { LoginService } from '@core/services';
import { intersection } from '@shared/tools';

const mockLoginService = {
    hasAuthorization(authorization: Array<Authorization> | Authorization = [], u: User = null) {
        return !authorization.length || intersection([Authorization.SCENARIO_EXECUTE], [...authorization]).length;
    }
};

export default {
    title: 'Components/Right menu',
    component: ChutneyRightMenuComponent,
    decorators: [
        moduleMetadata({
            imports: [RouterModule.forRoot([], {useHash: true}), SharedModule, HttpClientModule, TranslateModule, TranslateTestingModule],
            providers: [
                {
                    provide: APP_BASE_HREF,
                    useValue: '/',
                },
                {
                    provide: LoginService,
                    useValue: mockLoginService
                }
            ]
        }),
    ],
} as Meta;

const Template: Story<ChutneyRightMenuComponent> = (args: ChutneyRightMenuComponent) => ({
    props: args,
});

export const EmptyMenu = Template.bind({});
EmptyMenu.args = {
    menuItems: [],
};

export const TranslatedItemLabel = Template.bind({});
TranslatedItemLabel.args = {
    menuItems: [
        {
            label: 'global.actions.edit'
        }
    ]
};

export const ItemWithIcon = Template.bind({});
ItemWithIcon.args = {
    menuItems: [
        {
            label: 'global.actions.edit',
            iconClass: 'fa fa-pencil-alt',
        }
    ]
};

export const ItemWithLink = Template.bind({});
ItemWithLink.args = {
    menuItems: [
        {
            label: 'global.actions.edit',
            iconClass: 'fa fa-pencil-alt',
            link: '/'
        }
    ]
};

export const ItemWithClickCallback = Template.bind({});
ItemWithClickCallback.args = {
    menuItems: [
        {
            label: 'global.actions.edit',
            iconClass: 'fa fa-pencil-alt',
            click: jest.fn()
        }
    ]
};
ItemWithClickCallback.play = async ({args, canvasElement}) => {
    const canvas = within(canvasElement);

    await userEvent.click(canvas.getByRole('nav-link'));
    console.log(args.menuItems[0]);

    await waitFor(() => expect(args.menuItems[0].click).toHaveBeenCalled());

};
export const DropDownItem = Template.bind({});
DropDownItem.args = {
    menuItems: [
        {
            label: 'global.actions.execute',
            iconClass: 'fa fa-play',
            options: [
                {id: '1', label: 'env 1'},
                {id: '2', label: 'env 2'}
            ],
            click: jest.fn()
        }
    ]
};

export const EmptyIfNotAuthorized = Template.bind({});
EmptyIfNotAuthorized.args = {
    menuItems: [
        {
            label: 'global.actions.edit',
            iconClass: 'fa fa-pencil-alt',
            authorizations: [Authorization.SCENARIO_WRITE]
        }
    ]
};

export const MenuWithManyItems = Template.bind({});
MenuWithManyItems.args = {
    menuItems: [
        {
            label: 'global.actions.execute',
            iconClass: 'fa fa-play',
            options: [
                {id: '1', label: 'env 1'},
                {id: '2', label: 'env 2'}
            ],
            click: jest.fn()
        },
        {
            label: 'global.actions.edit',
            iconClass: 'fa fa-pencil-alt',
            link: '/'
        }
    ]
};
