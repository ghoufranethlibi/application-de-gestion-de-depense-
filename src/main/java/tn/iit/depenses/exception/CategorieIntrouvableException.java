package tn.iit.depenses.exception;

public class CategorieIntrouvableException extends MetierException {
    public CategorieIntrouvableException() {
        super("Catégorie introuvable ou non autorisée.");
    }
}