package org.keycloak.examples.churchtools.model;

public class OtpLoginDto {

    private String code;
    private String personId;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }
}
