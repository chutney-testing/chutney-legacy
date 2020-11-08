export const fr = {
  language: 'French',
  scenarios: {
    title: 'Scenarios',
  },

  // simple function using string literal and interpolation
  // see the cs.translation.ts for another example
  langsSupported: (n: number) => `Cette application supporte ${n} langage${n === 1 ? '' : 's'}.`,
};
