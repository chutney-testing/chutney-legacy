
describe('Login', () => {
    it('should redirect to login', () => {
        cy.visit('/');
        cy.url().should('eq', Cypress.config().baseUrl + '/#/login');
    })

    it('should not login without credentials', () => {
        cy.visit('/');
        cy.get('button[type="submit"]').click();
        cy.url().should('eq', Cypress.config().baseUrl + '/#/login');

    })

    it('should not login when bad credentials', () => {
        cy.visit('/');
        cy.get('input[id="username"]').type("wrong user");
        cy.get('input[id="password"]').type("wrong password");
        cy.get('button[type="submit"]').click();
        cy.get('div.alert').should('be.visible').and('contain.text','Bad credentials');

    })

    it('should not login when bad credentials', () => {
        cy.visit('/');
        cy.get('input[id="username"]').type("wrong user");
        cy.get('input[id="password"]').type("wrong password");
        cy.get('button[type="submit"]').click();
        cy.get('div.alert').should('be.visible').and('contain.text','Bad credentials');

    })
})
