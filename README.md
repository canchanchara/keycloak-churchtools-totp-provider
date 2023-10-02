# Church Tools TOTP Erweiterung für Keycloak

Mit dieser Erweiterung kann man sich mit dem Church Tools TOTP (2. Faktor) in Keycloak als OpenID Connect Client einloggen.


# Vorbereitung
1. Installieren des Church Tools Storage Providers https://github.com/canchanchara/keycloak-churchtools-storage-provider

# Installation


## Church Tools Credentials
Die Churchtools spezifischen Daten wie die Instanz und Username/Passwort eines Adminusers sind in der keycloak.conf
Datei zu hinterlegen.

keycloak/conf/keycloak.conf

```
spi-credential-churchtools-totp-instance=churchtoolsname
spi-credential-churchtools-totp-username=adminuser
spi-credential-churchtools-totp-password=adminpassword
```

## Jar Installation

Die jar Datei aus dem Targetverzeichnis muss in das providers Verzeichnis von keycloak kopiert werden.
Alternativ kann man das jar aus dem Gitrepo Release Verzeichnis nutzen.


```
cp target/totp-churchtools.jar keycloak/providers
```

## Theme / TOTP Formular
Der Ordner churchtheme muss nach /themes/churchtheme kopiert werden.


Keycloak muss danach neu gestartet werden.

## Konfiguration in Keycloak

* Realm auswählen (zb. Church)
* Authentication im Menü wählen
* browser Flow kopieren (zb. zu Church-Browser)
* OTP Form löschen
* Step "Church Tools TOTP" hinzufügen. 
  * Alias und Cookie Zeit konfigurieren
  * Requirement: Required
* Client auswählen
  * Advanced Reiter wählen 
  * ganz unten "Authentication flow overrides" -> Browser Flow auswählen

Wenn man sich nun einloggt, sollte nach dem User/Passwort das TOTP abgefragt werden.


# Lokal bauen
Das Projekt kann mit Java 17 und Maven gebaut werden.

```bash
mvn clean install
```

