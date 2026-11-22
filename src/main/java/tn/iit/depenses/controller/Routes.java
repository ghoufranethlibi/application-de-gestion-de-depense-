package tn.iit.depenses.controller;

/** Routes de l'application, centralisées (plus de chaînes d'URL répétées dans les contrôleurs). */
public final class Routes {
    public static final String ACCUEIL = "/";
    public static final String CONNEXION = "/connexion";
    public static final String INSCRIPTION = "/inscription";
    public static final String TABLEAU_DE_BORD = "/tableau-de-bord";
    public static final String CATEGORIES = "/categories";
    public static final String ADMINISTRATION = "/administration";
    public static final String OPERATIONS = "/operations";
    public static final String RAPPORTS = "/rapports";

    private Routes() {
    }

    public static String rediriger(String route) {
        return "redirect:" + route;
    }
}
