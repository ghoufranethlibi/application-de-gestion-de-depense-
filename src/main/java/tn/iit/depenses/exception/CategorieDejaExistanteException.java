package tn.iit.depenses.exception;

public class CategorieDejaExistanteException extends MetierException {
    public CategorieDejaExistanteException() {
        super("Une catégorie de ce nom existe déjà.");
    }
}
