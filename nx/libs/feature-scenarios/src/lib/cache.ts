import { makeVar } from '@apollo/client';

export const scenariosFilterVar = makeVar({
  text: '',
  tags: [],
  date: '',
  advanced: false,
});
