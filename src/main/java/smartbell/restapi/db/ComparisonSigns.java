package smartbell.restapi.db;

import smartbell.restapi.BellServiceException;

public enum ComparisonSigns {
    EQUALS("="), GREATER_THAN(">"),
    LESS_THAN("<"), GRATER_THAN_OR_EQUALS(">="), LESS_THAN_OR_EQUALS("<=");

    public String val;

    ComparisonSigns(String val) {
        this.val = val;
    }

    public static ComparisonSigns fromValue(String value) {
        for (ComparisonSigns sign : values()) {
            if(sign.val.equalsIgnoreCase(value)) {
                return sign;
            }
        }

        throw new BellServiceException("No such comparison sign! Please use \'<,>,=, <= or >=  \'");
    }
}
