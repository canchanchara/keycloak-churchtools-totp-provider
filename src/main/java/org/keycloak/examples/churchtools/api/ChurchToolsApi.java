package org.keycloak.examples.churchtools.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import org.keycloak.examples.churchtools.model.ChurchToolsException;
import org.keycloak.examples.churchtools.model.LoginDto;
import org.keycloak.examples.churchtools.model.ServerCredentials;
import org.keycloak.examples.churchtools.model.ServerSingleAttributeDto;
import org.keycloak.examples.churchtools.model.SettingsResultDto;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ChurchToolsApi {

    private ChurchToolsApi() {

    }

    private static final Logger logger = Logger.getLogger(ChurchToolsApi.class);

    public static boolean is2FAEnabled(ServerCredentials serverCredentials, CookieManager cookieManager, String personId) {
        try {

            logger.info("is2FAEnabled: User ID" + personId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://" + serverCredentials.getInstance() + ".church.tools/api/persons/" + personId + "/settings/churchcore"))
                    .headers("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newBuilder()
                    .cookieHandler(cookieManager)
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());


            SettingsResultDto settingsResultDto = createMapper().readValue(response.body(), SettingsResultDto.class);

            if (response.statusCode() == 200 &&
                    settingsResultDto != null &&
                    settingsResultDto.getData() != null &&
                    settingsResultDto.getData().stream()
                            .anyMatch(e -> e.getAttribute().equals("twoFactorEnabled") &&
                                    ((Boolean) (e.getValue())).booleanValue())) {
                logger.info("2FA ist für " + personId + "  aktiviert!");
                return true;
            }

            logger.info("2FA ist für " + personId + " deaktiviert!");
            return false;


        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error(e);
            throw new ChurchToolsException(e.getMessage());
        }

    }

    public static String getOtpSecret(ServerCredentials serverCredentials, CookieManager cookieManager, String personId) {

        try {
            logger.info("twoFactorSecret from Church Tools for User ID: " + personId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://" + serverCredentials.getInstance() + ".church.tools/api/persons/" + personId + "/settings/churchcore/twoFactorSecret"))
                    .headers("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newBuilder()
                    .cookieHandler(cookieManager)
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());


            ServerSingleAttributeDto serverSingleAttributeDto = createMapper().readValue(response.body(), ServerSingleAttributeDto.class);

            if (response.statusCode() == 200 &&
                    serverSingleAttributeDto != null &&
                    serverSingleAttributeDto.getData() != null) {
                logger.debug("secret found in church tools: " + serverSingleAttributeDto.getData().getValue().toString());
                return serverSingleAttributeDto.getData().getValue().toString();
            }

            throw new ChurchToolsException("2FA Secret konnte nicht gefunden werden!");

        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error(e);
            throw new ChurchToolsException(e.getMessage());
        }

    }


    public static CookieManager login(ServerCredentials serverCredentials) {

        try {

            logger.info("Login to " + serverCredentials.getInstance());

            LoginDto loginDto = new LoginDto();
            loginDto.setUsername(serverCredentials.getUsername());
            loginDto.setPassword(serverCredentials.getPassword());
            loginDto.setRememberMe(true);

            String loginJson = createMapper().writeValueAsString(loginDto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://" + serverCredentials.getInstance() + ".church.tools/api/login"))
                    .headers("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                    .build();

            CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

            HttpResponse<String> response = HttpClient.newBuilder()
                    .cookieHandler(cookieManager)
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ChurchToolsException("Ungültiger Login! " + response.body());
            }

            return cookieManager;
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error(e);
            throw new ChurchToolsException(e.getMessage());
        }

    }


    private static ObjectMapper createMapper() {
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

}
