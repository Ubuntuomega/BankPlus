package me.pulsi_.bankplus.dataStorage.services;

import me.pulsi_.bankplus.dataStorage.PlayerAccountData;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

public interface IBPDataService {
    void connect();
    void disconnect();
    boolean isConnected();

    void registerPlayer(OfflinePlayer player);
    boolean isPlayerRegistered(OfflinePlayer player, String bankName);
    void savePlayer(PlayerAccountData playerAccountData);
    void updatePlayer(PlayerAccountData playerAccountData);
    Optional<PlayerAccountData> getPlayerAccountData(String bankName, OfflinePlayer player);

}
