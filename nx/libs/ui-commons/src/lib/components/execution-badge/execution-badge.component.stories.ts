import { text, number, boolean } from '@storybook/addon-knobs';
import { ExecutionBadgeComponent } from './execution-badge.component';

export default {
  title: 'ExecutionBadgeComponent',
};

export const primary = () => ({
  moduleMetadata: {
    imports: [],
  },
  component: ExecutionBadgeComponent,
  props: {
    status: text('status', ''),
  },
});
