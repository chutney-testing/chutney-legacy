export const en = {
  language: 'English',
  scenarios: {
    title: 'Scenarios',
  },
  campaigns: {
    title: 'Campaigns',
  },

  // simple function using string literal and interpolation
  // see the cs.translation.ts for another example
  langsSupported: (n: number) =>
    `This app supports ${n} language${n === 1 ? '' : 's'}.`,
};
