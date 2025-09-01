package me.pulsi_.bankplus.dataStorage.repositories.sql;

import me.pulsi_.bankplus.dataStorage.PlayerAccountData;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class BPDataRepositorySqlAccountName extends BPDataRepositorySql {
    /**
     * Initializes the MySQL system, loading all the necessary values.
     *
     * @param username The database username.
     * @param password The database password.
     * @param host     The database host.
     * @param port     The database port.
     * @param database The database schema to use.
     * @param useSsl   Whether the connection should use SSL.
     */
    public BPDataRepositorySqlAccountName(String username, String password, String host, int port, String database, boolean useSsl) {
        super(username, password, host, port, database, useSsl);
    }


    /**
     * Check if the player record is present in the given bank table.
     *
     * @param player   The player to check for registration.
     * @param bankName The bank table name.
     * @return true if there is a player record, false otherwise.
     */
    @Override
    public boolean isRegistered(OfflinePlayer player, String bankName) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT account_name FROM ? WHERE account_name = ?")) {

            statement.setString(1, bankName);
            statement.setString(2, player.getName());

            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void savePlayer(PlayerAccountData playerAccountData) {
        if (isRegistered(Bukkit.getOfflinePlayer(playerAccountData.uuid()), playerAccountData.bankName())) {
            updatePlayer(playerAccountData);
        } else {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("INSERT INTO " + playerAccountData + " VALUES uuid = ?, account_name=?, bank_level=?, money=?, interest=?, debt=?")) {

                String uuid = playerAccountData.uuid().toString();
                String accountName = playerAccountData.accountName();
                String bankLevel = Math.max(1, playerAccountData.bankLevel()) + "";
                String money = BPFormatter.styleBigDecimal(playerAccountData.money());
                String interest = BPFormatter.styleBigDecimal(playerAccountData.interest());
                String debt = BPFormatter.styleBigDecimal(playerAccountData.debt());

                statement.setString(1, uuid);
                statement.setString(2, accountName);
                statement.setString(3, bankLevel);
                statement.setString(4, money);
                statement.setString(5, interest);
                statement.setString(6, debt);

                statement.executeUpdate();

            } catch (SQLException e) {
                BPLogger.Console.warn(true, "Could not update values of player \"" + playerAccountData.accountName() + "(" + playerAccountData.uuid() + ")\", reason: \"" + e.getMessage() + "\"");
            }
        }

    }


    @Override
    public void updatePlayer(PlayerAccountData playerAccountData) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE " + playerAccountData.bankName() + " SET money=?, bank_level=?, interest=?, debt=? WHERE account_name = ?")) {

            statement.setString(1, BPFormatter.styleBigDecimal(playerAccountData.money())); //Money
            statement.setString(2, Math.max(1, playerAccountData.bankLevel()) + ""); //Bank level
            statement.setString(3, BPFormatter.styleBigDecimal(playerAccountData.interest())); //Interest
            statement.setString(4, BPFormatter.styleBigDecimal(playerAccountData.debt())); //Debt

            statement.setString(5, playerAccountData.accountName());

            statement.executeUpdate();

        } catch (SQLException e) {
            BPLogger.Console.warn(true, "Could not update values of player \"" + playerAccountData.accountName() + "(" + playerAccountData.uuid() + ")\", reason: \"" + e.getMessage() + "\"");
        }
    }


    @Override
    public Optional<PlayerAccountData> getPlayer(String bankName, OfflinePlayer player) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement("SELECT uuid, bank_level, money, interest, debt FROM " + bankName + " WHERE account_name = ?")) {

            statement.setString(1, player.getName());


            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                int bank_level = Integer.parseInt(resultSet.getString("bank_level"));
                BigDecimal money = new BigDecimal(resultSet.getString("money"));
                BigDecimal interest = new BigDecimal(resultSet.getString("interest"));
                BigDecimal debt = new BigDecimal(resultSet.getString("debt"));

                PlayerAccountData playerAccountData = new PlayerAccountData(bankName, uuid, player.getName(), bank_level, money, interest, debt);

                return Optional.of(playerAccountData);
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
