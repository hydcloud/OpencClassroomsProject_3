describe('DataShare - fichier protégé par mot de passe', () => {

    it('refuse un mauvais mot de passe puis autorise le bon', () => {

        cy.visit('http://localhost:4200');

        cy.get('input[type="file"]')
            .selectFile({
                contents: Cypress.Buffer.from(
                    'Contenu fichier protégé Cypress'
                ),
                fileName: 'protected-cypress.txt',
                mimeType: 'text/plain'
            });

        cy.get('#password')
            .type('Secret123');

        cy.contains('Transférer')
            .click();

        cy.contains('Fichier envoyé avec succès.')
            .should('be.visible');

        cy.contains('Votre lien de téléchargement')
            .should('be.visible');

        cy.get('input[name="anonymousDownloadPassword"]')
            .type('Mauvais123');

        cy.intercept(
            'POST',
            '**/api/files/*/file'
        ).as('wrongPassword');

        cy.contains('Télécharger le fichier')
            .click();

        cy.wait('@wrongPassword')
            .its('response.statusCode')
            .should('eq', 403);

        cy.contains('Mot de passe incorrect.')
            .should('be.visible');

        cy.get('input[name="anonymousDownloadPassword"]')
            .clear()
            .type('Secret123');

        cy.intercept(
            'POST',
            '**/api/files/*/file'
        ).as('correctPassword');

        cy.contains('Télécharger le fichier')
            .click();

        cy.wait('@correctPassword')
            .its('response.statusCode')
            .should('eq', 200);

    });

});