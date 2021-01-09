describe('User Login', () => {
  beforeEach(() => cy.visit('/auth/login'));

  it('should display login page', () => {
    cy.location('pathname').should('equal', '/auth/login');
    cy.get('h4').contains('Login to your account');
  });
});
