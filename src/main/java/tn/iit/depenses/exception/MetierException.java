package tn.iit.depenses.exception;

public abstract class MetierException extends RuntimeException {
    protected MetierException(String message) {
        super(message);
    }
}