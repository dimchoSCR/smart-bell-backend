package smartbell.restapi;

import smartbell.restapi.db.ComparisonSigns;

import java.beans.PropertyEditorSupport;

public class SignToEnumConverter extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(ComparisonSigns.fromValue(text));
    }
}
