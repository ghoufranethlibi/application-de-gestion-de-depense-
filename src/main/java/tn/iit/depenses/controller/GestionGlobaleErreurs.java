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

@ControllerAdvice
public class GestionGlobaleErreurs {

    @ExceptionHandler(MetierException.class)
    public String erreurMetier(MetierException ex, HttpServletRequest requete) {
        return rediriger(requete, ex.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public String erreurValidation(BindException ex, HttpServletRequest requete) {
        FieldError erreur = ex.getFieldError();
        String message = (erreur == null || erreur.isBindingFailure())
                ? "Une valeur saisie est invalide." : erreur.getDefaultMessage();
        return rediriger(requete, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String parametreInvalide(MethodArgumentTypeMismatchException ex, HttpServletRequest requete) {
        return rediriger(requete, "Paramètre invalide : " + ex.getName());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String conflitDonnees(HttpServletRequest requete) {
        return rediriger(requete, "Cette donnée existe déjà.");
    }

    private String rediriger(HttpServletRequest requete, String message) {
        RequestContextUtils.getOutputFlashMap(requete).put("erreur", message);
        return "redirect:" + pageDeRetour(requete.getRequestURI());
    }

    private String pageDeRetour(String uri) {
        if (uri.startsWith("/administration")) return "/administration";
        if (uri.startsWith("/categories")) return "/categories";
        if (uri.startsWith("/inscription")) return "/inscription";
        return "/tableau-de-bord";
    }
}