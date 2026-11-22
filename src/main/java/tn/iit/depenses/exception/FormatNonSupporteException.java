package tn.iit.depenses.exception;

import tn.iit.depenses.domain.FormatRapport;

public class FormatNonSupporteException extends MetierException {
    public FormatNonSupporteException(FormatRapport format) {
        super("Le format de rapport " + format + " n'est pas pris en charge.");
    }
}
