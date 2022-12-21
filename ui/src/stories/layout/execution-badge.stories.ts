import { componentWrapperDecorator, Meta, Story } from '@storybook/angular';
import { ExecutionBadgeComponent } from '@shared/components';

export default {
    title: 'Chutney/Execution Badge',
    component: ExecutionBadgeComponent,
    excludeStories: /^Default$/,
    decorators: [
        componentWrapperDecorator((story) => `<div style="margin: 3em">${story}</div>`),
    ],
    args: {
        status: 'SUCCESS',
        spin: false,
    },
} as Meta;

const Template: Story = (args) => ({
    props: args,
});

export const Default = Template.bind({});
Default.args = {
    // args are taken from component level args.
};

export const Success = Template.bind({});
Success.args = {
    ...Default.args
};

export const Failure = Template.bind({});
Failure.args = {
    ...Default.args,
    status: 'FAILURE'
};

export const Running = Template.bind({});
Running.args = {
    ...Default.args,
    status: 'RUNNING'
};

export const Spinning = Template.bind({});
Spinning.args = {
    ...Running.args,
    spin: true
};

export const Paused = Template.bind({});
Paused.args = {
    ...Default.args,
    status: 'PAUSED'
};

export const Stopped = Template.bind({});
Stopped.args = {
    ...Default.args,
    status: 'STOPPED'
};

export const NotExecuted = Template.bind({});
NotExecuted.args = {
    ...Default.args,
    status: 'NOT_EXECUTED'
};
