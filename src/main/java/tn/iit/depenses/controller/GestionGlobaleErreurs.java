package tn.iit.depenses.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.support.RequestContextUtils;
import tn.iit.depenses.exception.MetierException;

/**
 * Gestion centralisée des erreurs : remplace les try/catch dupliqués dans chaque contrôleur.
 * L'utilisateur est renvoyé sur la page d'où vient l'action, avec le message en rouge.
 */
@ControllerAdvice
public class GestionGlobaleErreurs {
    private static final String MESSAGE_SAISIE_INVALIDE = "Une valeur saisie est invalide.";

    /** Règle métier non respectée (catégorie introuvable, doublon, action interdite…). */
    @ExceptionHandler(MetierException.class)
    public String erreurMetier(MetierException ex, HttpServletRequest requete) {
        return rediriger(requete, ex.getMessage());
    }

    /** Échec de @Valid sur un formulaire : on affiche le message de la première contrainte violée. */
    @ExceptionHandler(BindException.class)
    public String erreurValidation(BindException ex, HttpServletRequest requete) {
        FieldError erreur = ex.getFieldError();
        boolean messageExploitable = erreur != null && !erreur.isBindingFailure();
        return rediriger(requete, messageExploitable ? erreur.getDefaultMessage() : MESSAGE_SAISIE_INVALIDE);
    }

    /** Paramètre d'URL impossible à convertir : ?mois=abc, /rapports/XLS… */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String parametreInvalide(MethodArgumentTypeMismatchException ex, HttpServletRequest requete) {
        return rediriger(requete, "Paramètre invalide : " + ex.getName());
    }

    /** Contrainte d'unicité de la base violée (ex. deux créations simultanées du même nom). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String conflitDeDonnees(HttpServletRequest requete) {
        return rediriger(requete, "Cette donnée existe déjà.");
    }

    private static String rediriger(HttpServletRequest requete, String message) {
        RequestContextUtils.getOutputFlashMap(requete).put(MessagesFlash.ERREUR, message);
        return Routes.rediriger(pageDeRetour(requete.getRequestURI()));
    }

    private static String pageDeRetour(String uri) {
        if (uri.startsWith(Routes.ADMINISTRATION)) {
            return Routes.ADMINISTRATION;
        }
        if (uri.startsWith(Routes.CATEGORIES)) {
            return Routes.CATEGORIES;
        }
        if (uri.startsWith(Routes.INSCRIPTION)) {
            return Routes.INSCRIPTION;
        }
        return Routes.TABLEAU_DE_BORD;
    }
}
