package org.example;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class DbSettings {
    private static final String SETTINGS_FILENAME = "settings";
    private static final String DB_URL = "dbUrl";
    private static final String USER = "user";
    private static final String PASSWORD = "password";

    private final String dbUrl;
    private final String user;
    private final String password;

    public DbSettings() {
        final var resourceBundle = ResourceBundle.getBundle(SETTINGS_FILENAME);

        this.dbUrl = resourceBundle.getString(DB_URL);
        this.user = resourceBundle.getString(USER);
        this.password = resourceBundle.getString(PASSWORD);
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
