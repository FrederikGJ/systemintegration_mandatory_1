package dk.library.db;

import org.sqlite.SQLiteConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Adgang til library.db. Opret én instans pr. applikation og åbn én forbindelse pr. kald.
 * Stien findes via miljøvariablen {@value #ENV_VAR}, ellers {@value #DEFAULT_PATH}.
 */
public final class Database {

    public static final String ENV_VAR = "LIBRARY_DB";
    public static final String DEFAULT_PATH = "../db/library.db";

    private final Path file;
    private final SQLiteConfig config;

    public Database() {
        this(Path.of(System.getenv().getOrDefault(ENV_VAR, DEFAULT_PATH)));
    }

    public Database(Path file) {
        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException(
                    "Fandt ikke databasen: " + file.toAbsolutePath()
                    + ". Læg library.db i db/ eller sæt miljøvariablen " + ENV_VAR + ".");
        }
        this.file = file;
        this.config = new SQLiteConfig();
        // Fire API-processer deler én fil: WAL giver samtidige læsere, busy_timeout venter på skrivelåse.
        config.setJournalMode(SQLiteConfig.JournalMode.WAL);
        config.setBusyTimeout(5_000);
    }

    public Path file() {
        return file;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + file, config.toProperties());
    }
}
