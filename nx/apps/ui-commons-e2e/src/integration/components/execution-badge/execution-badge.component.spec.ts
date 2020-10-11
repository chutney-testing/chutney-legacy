describe('ui-commons', () => {
  beforeEach(() =>
    cy.visit('/iframe.html?id=executionbadgecomponent--primary&knob-status')
  );

  it('should render the component', () => {
    cy.get('chutney-testing-execution-badge').should('exist');
  });
});
