describe('DataShare - upload utilisateur authentifié', () => {

  it('permet à un utilisateur de se connecter', () => {

    cy.visit('http://localhost:4200');

    cy.contains('Se connecter')
      .click();

    cy.get('#email')
      .type('stephane@gmail.com');

    cy.get('#password')
      .type('MotDePasse123');

    cy.get('button[type="submit"]')
      .click();

    cy.contains('Bienvenue')
      .should('be.visible');

    cy.get('input[type="file"]')
      .selectFile({
        contents: Cypress.Buffer.from('Contenu du fichier de test Cypress'),
        fileName: 'cypress-test.txt',
        mimeType: 'text/plain'
      });

    cy.contains('Transférer')
      .click();

    cy.contains('Fichier envoyé avec succès.')
      .should('be.visible');

    cy.contains('cypress-test.txt')
      .should('be.visible');

  });

});