describe('chutney', () => {
  beforeEach(() => cy.login('user', 'user'));

  it('should display welcome message', () => {
    cy.visit('/fr/scenarios');
    cy.get('.scenarios-search-form').should('be.visible');
    cy.get('.mat-header-row').should('be.visible');
  });
});
