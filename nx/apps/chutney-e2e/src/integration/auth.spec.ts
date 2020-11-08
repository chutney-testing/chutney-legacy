describe('User Login', () => {
  beforeEach(() => cy.visit('/fr/auth/login'));

  it('should display login page', () => {
    cy.location('pathname').should('equal', '/fr/auth/login');
    cy.get('h4').contains('Login to your account');
  });
});
