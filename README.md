# Gestionnaire de dépenses personnelles — partie OOP

Application web Java réalisée à partir des documents d’analyse OOA et de conception OOD. L’interface et les messages sont en français.

## Démarrage rapide

Prérequis : Java 17 ou plus récent et Maven 3.6.3 ou plus récent.

1. Décompresser le dossier du projet.
2. Ouvrir un terminal dans `gestionnaire-depenses`.
3. Lancer l’application :

   ```bash
   mvn spring-boot:run
   ```

4. Ouvrir <http://localhost:8080>.

### Profils et compte administrateur

| Profil | Quand | Mot de passe administrateur |
| --- | --- | --- |
| `dev` | par défaut (`mvn spring-boot:run`) | `admin@budget.local` / `Admin123!` (démo locale uniquement) |
| `prod` | `SPRING_PROFILES_ACTIVE=prod` | **obligatoire** via `APP_ADMIN_PASSWORD`, sinon l’application refuse de démarrer |
| `test` | tests automatiques | base H2 en mémoire |

Le mot de passe n’est plus affiché sur la page de connexion. Pour choisir ses propres identifiants avant le premier démarrage :

```powershell
$env:APP_ADMIN_EMAIL = "admin@exemple.tn"
$env:APP_ADMIN_PASSWORD = "choisir-un-mot-de-passe-long"
mvn spring-boot:run
```

### Tests

```bash
mvn test      # tests unitaires (JUnit 5 + Mockito) et d’intégration (MockMvc, H2 en mémoire)
mvn verify    # ce que lance la CI GitHub Actions à chaque push
```

La base de données H2 est créée dans `data/` et conserve les opérations entre les redémarrages. Elle est incluse pour faciliter une démonstration locale; le modèle relationnel peut ensuite être raccordé à PostgreSQL ou MySQL.

## Fonctions incluses

- inscription, connexion et déconnexion avec mots de passe hachés;
- création, modification, suppression et consultation des revenus et dépenses;
- contrôle du propriétaire des opérations et des catégories;
- catégories personnelles et catégories par défaut;
- réaffectation des opérations à « Autres » lors de la suppression d’une catégorie;
- montants exacts avec `BigDecimal` (trois décimales en TND), répartition par catégorie et évolution sur six mois;
- graphiques adaptés aux petits écrans;
- export mensuel en CSV et PDF;
- écran administrateur pour activer/désactiver des comptes membres et gérer les catégories par défaut.

## Organisation du code

```text
src/main/java/tn/iit/depenses/
├── config/       sécurité, AdminInitializer, CategoriesParDefautInitializer
├── controller/   routes web, GestionGlobaleErreurs (@ControllerAdvice), GradientDonut
├── domain/       Membre, Operation, Revenu, Depense, Categorie, TypeOperation
├── exception/    exceptions métier (MetierException et ses sous-classes)
├── repository/   Spring Data JPA + requêtes d’agrégation (SUM / GROUP BY)
└── service/      règles métier, TableauDeBordFacade, bilans, rapports
    ├── dto/      OperationRequest, InscriptionRequest, CategorieRequest… (validés par @Valid)
    └── export/   ExportStrategy, CsvExport, PdfExport, GenerateurPdf

src/test/java/tn/iit/depenses/
├── service/      tests unitaires des services (Mockito)
├── controller/   test du graphique en anneau
└── integration/  sécurité, formulaires, vues, exports, agrégations
```

Les vues Thymeleaf et les styles se trouvent dans `src/main/resources/templates/` et `src/main/resources/static/`.

## Correspondance avec les exigences

| Exigences | Réalisation |
| --- | --- |
| EF-01 | Inscription, authentification par e-mail et mot de passe, rôles membre/administrateur |
| EF-02 à EF-03 | Ajouter, modifier, supprimer et consulter les revenus et dépenses |
| EF-04 à EF-05 | Catégories typées, date, montant exact et description |
| EF-06 à EF-08 | Bilan mensuel, répartition par catégorie et évolution graphique |
| EF-09 | Téléchargement d’un rapport CSV ou PDF |
| EF-10 | Administration des comptes membres et catégories proposées par défaut |
| ENF-02, ENF-04 | Hachage BCrypt, séparation des opérations par propriétaire et calculs `BigDecimal` |
| ENF-05, ENF-06 | Interface responsive et architecture contrôleur/service/repository |

## Choix de cette version

L’énoncé OOD laisse le choix entre MySQL et PostgreSQL. Cette version utilise H2 en mode fichier afin de pouvoir être lancée localement sans service de base de données distinct. L’affichage utilise le TND et trois décimales, choix adapté au contexte de Sfax. Les exports PDF sont générés par l’application sans bibliothèque externe; les caractères accentués sont translittérés dans le PDF. La base doit être migrée et les paramètres administrateur modifiés avant toute mise en production.


## Corrections suite à la revue de code

| Point faible relevé | Correction |
| --- | --- |
| Aucun test automatisé | 40+ tests : unitaires (Mockito) et d’intégration (MockMvc + H2), dont l’isolation des données entre membres |
| `try/catch` dupliqués dans les contrôleurs | Exceptions métier (`exception/`) + `GestionGlobaleErreurs` (`@ControllerAdvice`) |
| Validation dispersée, « data clumps » (montant, date, description, catégorie) | DTO `OperationRequest`, `InscriptionRequest`, `CategorieRequest` validés par `@Valid` |
| Mois invalide remplacé en silence, `FormatRapport.valueOf` fragile | Conversion par Spring (`YearMonth`, `FormatRapport`) + message « Paramètre invalide » |
| Mot de passe admin par défaut `Admin123!` | Uniquement en profil `dev` ; obligatoire via `APP_ADMIN_PASSWORD` sinon ; retiré de la page de connexion |
| `DataInitializer` avec deux responsabilités | `AdminInitializer` + `CategoriesParDefautInitializer` |
| `RapportService` couplé à `CsvExport`/`PdfExport` (ternaire) | Registre `Map<FormatRapport, ExportStrategy>` injecté par Spring (OCP, DIP) |
| `PageController` trop chargé | `TableauDeBordFacade` + `GradientDonut` |
| `PdfExport` mêlant contenu et format PDF | `PdfExport` (contenu) + `GenerateurPdf` (mécanique PDF) |
| Chaînes en dur (« Autres », 'REVENU', 'ADMIN', routes) | `TypeOperation` (libellé, signe, catégorie de réaffectation), `Membre.isAdmin()`, `Routes` |
| `categorie` en EAGER, calculs SUM/GROUP BY en Java | `LAZY` + `@EntityGraph` ; agrégations faites par la base |
| Unicité des catégories vérifiée seulement en Java | Contrainte unique en base `(membre_id, type_operation, nom)` |
| Pas d’intégration continue | `.github/workflows/ci.yml` (`mvn verify`) |
| Bug : supprimer sa catégorie « Autres » cassait la réaffectation | Suppression interdite avec un message clair |

Pistes restantes (long terme) : PostgreSQL + Flyway pour les migrations, Docker, analyse statique (Checkstyle/SpotBugs).
