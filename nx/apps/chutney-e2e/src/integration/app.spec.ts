describe('chutney', () => {
  beforeEach(() => cy.visit('/'));

  it('should display welcome message', () => {
    // Custom command example, see `../support/commands.ts` file
    cy.login('my-email@something.com', 'myPassword');

    // Function helper example, see `../support/app.po.ts` file
    cy.get(".scenarios-search-form").should('be.visible');
    cy.get(".mat-header-row").should('be.visible');
  });
});
