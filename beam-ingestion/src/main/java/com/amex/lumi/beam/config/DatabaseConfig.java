package com.amex.lumi.beam.config;

public class DatabaseConfig {

    private static final String HOST =
            getRequiredEnvironmentVariable("DB_HOST");

    private static final int PORT =
            Integer.parseInt(getRequiredEnvironmentVariable("DB_PORT"));

    private static final String DATABASE =
            getRequiredEnvironmentVariable("DB_NAME");

    private static final String USERNAME =
            getRequiredEnvironmentVariable("DB_USER");

    private static final String PASSWORD =
            getRequiredEnvironmentVariable("DB_PASSWORD");

    public static String getJdbcUrl() {
        return "jdbc:postgresql://"
                + HOST
                + ":"
                + PORT
                + "/"
                + DATABASE;
    }

    public static String getUsername() {
        return USERNAME;
    }

    public static String getPassword() {
        return PASSWORD;
    }

    private static String getRequiredEnvironmentVariable(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required environment variable is missing: " + name
            );
        }

        return value;
    }
}