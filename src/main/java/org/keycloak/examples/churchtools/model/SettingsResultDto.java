package org.keycloak.examples.churchtools.model;

import java.util.List;

public class SettingsResultDto {

    private List<SettingAttributeDto> data;

    public List<SettingAttributeDto> getData() {
        return data;
    }

    public void setData(List<SettingAttributeDto> data) {
        this.data = data;
    }
}
