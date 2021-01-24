import '!style-loader!css-loader!sass-loader!../../styles/src/lib/styles.scss';
import { addDecorator } from '@storybook/angular';
import { withKnobs } from '@storybook/addon-knobs';

addDecorator(withKnobs);
