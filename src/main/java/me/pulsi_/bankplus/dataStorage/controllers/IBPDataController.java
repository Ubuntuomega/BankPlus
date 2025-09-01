package me.pulsi_.bankplus.dataStorage.controllers;

import me.pulsi_.bankplus.dataStorage.PlayerAccountData;
import org.bukkit.OfflinePlayer;

public interface IBPDataController {
    /**
     * Connect this controller to its repository (SQL, File system...).
     */
    void connect();

    /**
     * Check if the controller is connected to its repository.
     * @return True if the controller is currently connected to its repository
     */
    boolean isConnected();

    /**
     * Disconnect the repository of this controller.
     */
    void disconnect();

    /**
     * Create a default player account data in all banks
     * @param player
     */
    void registerPlayer(OfflinePlayer player);

    /**
     * Update player account details only if the player have a registered account.
     * @param playerAccountData
     */
    void updatePlayer(PlayerAccountData playerAccountData);

    /**
     * Update the player's account data, creating a new one if they did not have one.
     * @param playerAccountData
     */
    void savePlayer(PlayerAccountData playerAccountData);

    /**
     * Check if the player has an account registered in the bank
     * @param player
     * @param bankName
     * @return
     */
    boolean isPlayerRegistered(OfflinePlayer player, String bankName);

    /**
     * Returns the data of a player
     * @param bankName
     * @param player
     * @return
     */
    PlayerAccountData getPlayerAccountData(String bankName, OfflinePlayer player);

}
