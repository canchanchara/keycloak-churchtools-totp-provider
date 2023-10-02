/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.examples.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialTypeMetadata;
import org.keycloak.credential.CredentialTypeMetadataContext;
import org.keycloak.examples.authenticator.credential.SecretQuestionCredentialModel;
import org.keycloak.examples.churchtools.api.ChurchToolsApi;
import org.keycloak.examples.churchtools.model.ServerCredentials;
import org.keycloak.examples.totp.DefaultCodeGenerator;
import org.keycloak.examples.totp.DefaultCodeVerifier;
import org.keycloak.examples.totp.HashingAlgorithm;
import org.keycloak.examples.totp.time.SystemTimeProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;

import java.net.CookieManager;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SecretQuestionCredentialProvider implements CredentialProvider<SecretQuestionCredentialModel>, CredentialInputValidator {
    private static final Logger logger = Logger.getLogger(SecretQuestionCredentialProvider.class);

    protected KeycloakSession session;
    private ServerCredentials serverCredentials;

    public SecretQuestionCredentialProvider(KeycloakSession session, ServerCredentials serverCredentials) {
        this.session = session;
        this.serverCredentials = serverCredentials;
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!(input instanceof UserCredentialModel)) {
            logger.debug("Expected instance of UserCredentialModel for CredentialInput");
            return false;
        }
        if (!input.getType().equals(getType())) {
            return false;
        }
        String challengeResponse = input.getChallengeResponse();
        if (challengeResponse == null) {
            return false;
        }


        CookieManager cookieManager = ChurchToolsApi.login(serverCredentials);
        String otpSecret = ChurchToolsApi.getOtpSecret(serverCredentials, cookieManager, getInternalChurchToolsId(user.getId()));

        logger.debug("otpSecret from ChurchTools " + otpSecret);

        SystemTimeProvider systemTimeProvider = new SystemTimeProvider();

        DefaultCodeVerifier defaultCodeVerifier = new DefaultCodeVerifier(new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6), systemTimeProvider);
        return defaultCodeVerifier.isValidCode(otpSecret, input.getChallengeResponse());

    }


    @Override
    public boolean supportsCredentialType(String credentialType) {
        return getType().equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {

        if (!supportsCredentialType(credentialType)) return false;

        CookieManager cookieManager = ChurchToolsApi.login(serverCredentials);
        return ChurchToolsApi.is2FAEnabled(serverCredentials, cookieManager, getInternalChurchToolsId(user.getId()));
    }

    private String getInternalChurchToolsId(String keycloakUserId) {
        return keycloakUserId.substring(keycloakUserId.lastIndexOf(":") + 1);
    }

    @Override
    public CredentialModel createCredential(RealmModel realm, UserModel user, SecretQuestionCredentialModel credentialModel) {
        if (credentialModel.getCreatedDate() == null) {
            credentialModel.setCreatedDate(Time.currentTimeMillis());
        }
        return user.credentialManager().createStoredCredential(credentialModel);
    }

    @Override
    public boolean deleteCredential(RealmModel realm, UserModel user, String credentialId) {
        return user.credentialManager().removeStoredCredentialById(credentialId);
    }

    @Override
    public SecretQuestionCredentialModel getCredentialFromModel(CredentialModel model) {
        return SecretQuestionCredentialModel.createFromCredentialModel(model);
    }

    @Override
    public CredentialTypeMetadata getCredentialTypeMetadata(CredentialTypeMetadataContext metadataContext) {
        return CredentialTypeMetadata.builder()
                .type(getType())
                .category(CredentialTypeMetadata.Category.TWO_FACTOR)
                .displayName(SecretQuestionCredentialProviderFactory.PROVIDER_ID)
                .helpText("secret-question-text")
                .createAction(SecretQuestionAuthenticatorFactory.PROVIDER_ID)
                .removeable(false)
                .build(session);
    }

    @Override
    public String getType() {
        return SecretQuestionCredentialModel.TYPE;
    }

    @Override
    public void close() {

    }
}
