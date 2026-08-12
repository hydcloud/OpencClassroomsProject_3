describe('DataShare - upload anonyme', () => {

  it('permet à un utilisateur anonyme d’envoyer puis télécharger un fichier', () => {

    cy.visit('http://localhost:4200');

    cy.get('input[type="file"]')
      .selectFile({
        contents: Cypress.Buffer.from(
          'Contenu du fichier anonyme Cypress'
        ),
        fileName: 'anonymous-cypress.txt',
        mimeType: 'text/plain'
      });

    cy.contains('Transférer')
      .click();

    cy.contains('Fichier envoyé avec succès.')
      .should('be.visible');

    cy.contains('Votre lien de téléchargement')
      .should('be.visible');

    cy.intercept(
      'POST',
      '**/api/files/*/file'
    ).as('downloadFile');

    cy.contains('Télécharger le fichier')
      .click();

    cy.wait('@downloadFile')
      .its('response.statusCode')
      .should('eq', 200);

  });

});