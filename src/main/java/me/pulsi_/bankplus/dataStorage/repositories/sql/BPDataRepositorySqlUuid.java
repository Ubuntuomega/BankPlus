package me.pulsi_.bankplus.dataStorage.repositories.sql;

import me.pulsi_.bankplus.dataStorage.PlayerAccountData;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * BankPlus MySQL system save both database and files to synchronize
 * them, local files data is always available and updated, and will
 * be updated on the database.
 * <p>
 * In case the database is not available or the connection has been
 * closed, the data will continue to be saved locally, and once the
 * database will connect again the local data will be updated.
 */
public class BPDataRepositorySqlUuid extends BPDataRepositorySql {

    public BPDataRepositorySqlUuid(String username, String password, String host, int port, String database, boolean useSsl) {
        super(username, password, host, port, database, useSsl);
    }


    @Override
    public Optional<PlayerAccountData> getPlayer(String bankName, OfflinePlayer player) {
        return getPlayer(bankName, player.getUniqueId());
    }

    @Override
    public void savePlayer(PlayerAccountData playerAccountData) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + playerAccountData + " VALUES uuid = ?, account_name=?, bank_level=?, money=?, interest=?, debt=? ON DUPLICATE KEY UPDATE bank_level=?, money=?, interest=?, debt=?")) {

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

            statement.setString(7, bankLevel);
            statement.setString(8, money);
            statement.setString(9, interest);
            statement.setString(10, debt);

            statement.executeUpdate();

        } catch (SQLException e) {
            BPLogger.Console.warn(true, "Could not update values of player \"" + playerAccountData.accountName() + "(" + playerAccountData.uuid() + ")\", reason: \"" + e.getMessage() + "\"");
        }
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
             PreparedStatement statement = connection.prepareStatement("SELECT uuid FROM " + bankName + " WHERE uuid = ?")) {

            statement.setString(1, player.getUniqueId().toString());

            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Update the player database values, if something goes wrong it won't save the data and will return false.
     *
     * @param playerAccountData The player to save.
     */
    public void updatePlayer(PlayerAccountData playerAccountData) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE " + playerAccountData.bankName() + " SET money=?, bank_level=?, interest=?, debt=? WHERE uuid = ?")) {

            statement.setString(1, BPFormatter.styleBigDecimal(playerAccountData.money())); //Money
            statement.setString(2, Math.max(1, playerAccountData.bankLevel()) + ""); //Bank level
            statement.setString(3, BPFormatter.styleBigDecimal(playerAccountData.interest())); //Interest
            statement.setString(4, BPFormatter.styleBigDecimal(playerAccountData.debt())); //Debt

            statement.setString(5, playerAccountData.uuid().toString());

            statement.executeUpdate();

        } catch (SQLException e) {
            BPLogger.Console.warn(true, "Could not update values of player \"" + playerAccountData.accountName() + "(" + playerAccountData.uuid() + ")\", reason: \"" + e.getMessage() + "\"");
        }
    }


    public Optional<PlayerAccountData> getPlayer(String bankName, UUID playerUuid) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement("SELECT account_name, bank_level, money, interest, debt FROM " + bankName + " WHERE uuid = ?")) {

            statement.setString(1, playerUuid.toString());


            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String accountName = resultSet.getString("account_name");
                int bank_level = Integer.parseInt(resultSet.getString("bank_level"));
                BigDecimal money = new BigDecimal(resultSet.getString("money"));
                BigDecimal interest = new BigDecimal(resultSet.getString("interest"));
                BigDecimal debt = new BigDecimal(resultSet.getString("debt"));

                PlayerAccountData playerAccountData = new PlayerAccountData(bankName, playerUuid, accountName, bank_level, money, interest, debt);
                return Optional.of(playerAccountData);
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static class BPSQLResponse {

        public final List<String> result;
        public final boolean success;
        public final String errorMessage;

        public BPSQLResponse(boolean success, String errorMessage) {
            this.result = new ArrayList<>();
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public BPSQLResponse(boolean success, String errorMessage, List<String> result) {
            this.result = result;
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public static BPSQLResponse success() {
            return new BPSQLResponse(true, null);
        }

        public static BPSQLResponse success(List<String> result) {
            return new BPSQLResponse(true, null, result);
        }

        public static BPSQLResponse fail(SQLException e) {
            return new BPSQLResponse(false, e.getMessage());
        }
    }
}