package tn.iit.depenses.exception;

public class EmailDejaUtiliseException extends MetierException {
    public EmailDejaUtiliseException() {
        super("Cette adresse e-mail est déjà utilisée.");
    }
}