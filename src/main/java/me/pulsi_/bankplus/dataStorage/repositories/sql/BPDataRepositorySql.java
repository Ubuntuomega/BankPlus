package me.pulsi_.bankplus.dataStorage.repositories.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.dataStorage.repositories.IBPDataRepository;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BPDataRepositorySql implements IBPDataRepository {

    private final HikariConfig config;

    public HikariDataSource dataSource;


    protected BPDataRepositorySql(String username, String password, String host, int port, String database, boolean useSsl) {
        config = new HikariConfig();

        config.setUsername(username);
        config.setPassword(password);

        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSsl);
        config.setPoolName(BankPlus.INSTANCE().getName() + "-HikariPool");
    }

    /**
     * Creates a new instance of the connection and assign it.
     * This method also enables the SQLMethods.
     */
    @Override
    public boolean connect() {
        try {
            dataSource = new HikariDataSource(config);

            // Create all the missing tables.
            setupTables();

            BPLogger.Console.info("MySQL database successfully connected!");

            return true;
        } catch (SQLException e) {
            BPLogger.Console.warn(e, "Could not connect bankplus to it's database!");
            return false;
        }
    }

    /**
     * Create all the necessary tables if not exist.
     *
     * @throws SQLException
     */
    private void setupTables() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            for (String bankName : BPEconomy.nameList()) {

                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + bankName + "(uuid VARCHAR(255)," +
                            "account_name VARCHAR(255)," +
                            "bank_level VARCHAR(255)," +
                            "money VARCHAR(255)," +
                            "interest VARCHAR(255)," +
                            "debt VARCHAR(255)," +
                            "PRIMARY KEY (uuid))");

                } catch (SQLException e) {
                    BPLogger.Console.warn(true, "Cannot create table +\"" + bankName + "\", reason: " + e.getMessage());
                }

            }

        } catch (SQLException e) {
            BPLogger.Console.warn(true, "Cannot establish a connection to the database, reason: " + e.getMessage());
            throw e;
        }

    }


    /**
     * Disconnect the database.
     */
    @Override
    public void disconnect() {

        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            BPLogger.Console.info("MySQL database successfully disconnected!");

        }

    }

    /**
     * Check if the connection to the database is present and isn't closed.
     *
     * @return true if it's correctly connected, false otherwise.
     */
    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }


    /**
     * Register the player create a new record on each bank tables.
     *
     * @param player The player to register.
     */
    @Override
    public void registerPlayer(OfflinePlayer player) {
        try (Connection con = dataSource.getConnection()) {

            for (String bankName : BPEconomy.nameList()) {

                if (!isRegistered(player, bankName)) {

                    //INSERT IGNORE is in case new banks are added in prod.
                    try (PreparedStatement statement = con.prepareStatement("INSERT IGNORE INTO " + bankName + " (uuid, account_name, bank_level, money, interest, debt) VALUES (?, ?, ?, ?, ?, ?)")) {

                        statement.setString(1, player.getUniqueId().toString()); //UUID
                        statement.setString(2, player.getName()); //Account name
                        statement.setString(3, "1"); //Bank Level
                        statement.setString(4, (ConfigValues.getMainGuiName().equals(bankName) ? ConfigValues.getStartAmount().toString() : "0")); //Initial money
                        statement.setString(5, "0"); //Interest
                        statement.setString(6, "0"); //Debt

                        statement.executeUpdate();


                    } catch (SQLException e) {
                        BPLogger.Console.warn("Cannot register player " + player.getName() + " (" + player.getUniqueId() + ") into bank '" + bankName + "': " + e.getMessage());
                    }

                }


            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
