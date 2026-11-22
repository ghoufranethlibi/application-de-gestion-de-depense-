package tn.iit.depenses.exception;

public class OperationIntrouvableException extends MetierException {
    public OperationIntrouvableException() {
        super("Opération introuvable ou non autorisée.");
    }
}