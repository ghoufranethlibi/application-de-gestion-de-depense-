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

Le compte administrateur local initial est `admin@budget.local` avec le mot de passe `Admin123!`. Les nouveaux membres peuvent créer leur compte depuis la page de connexion.

Pour remplacer les identifiants administrateur avant le premier démarrage :

```powershell
$env:APP_ADMIN_EMAIL = "admin@exemple.tn"
$env:APP_ADMIN_PASSWORD = "choisir-un-mot-de-passe-long"
mvn spring-boot:run
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
├── config/       sécurité, création du compte admin et catégories initiales
├── controller/   routes web et formulaires
├── domain/       Membre, Operation, Revenu, Depense, Categorie
├── repository/   accès aux données avec Spring Data JPA
└── service/      règles métier, bilans, rapports et stratégies d’export
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
