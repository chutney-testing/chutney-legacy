import { text, number, boolean, select } from '@storybook/addon-knobs';
import { ExecutionBadgeComponent } from './execution-badge.component';
import { UiMaterialModule } from '@chutney/ui-material';

export default {
  title: 'ExecutionBadgeComponent',
};

const values = ['RUNNING', 'PAUSED', 'FAILURE', 'SUCCESS'];

const defaultValue = values[0];

export const primary = () => ({
  moduleMetadata: {
    imports: [UiMaterialModule],
  },
  component: ExecutionBadgeComponent,
  props: {
    status: select('status', values, defaultValue),
  },
});
