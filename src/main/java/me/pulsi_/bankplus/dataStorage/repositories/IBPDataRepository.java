package me.pulsi_.bankplus.dataStorage.repositories;

import me.pulsi_.bankplus.dataStorage.PlayerAccountData;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

public interface IBPDataRepository {

    boolean connect();
    void disconnect();
    boolean isConnected();

    boolean isRegistered(OfflinePlayer player, String bankName);
    void updatePlayer(PlayerAccountData playerAccountData);
    void registerPlayer(OfflinePlayer player);
    Optional<PlayerAccountData> getPlayer(String bankName, OfflinePlayer player);
    void savePlayer(PlayerAccountData playerAccountData);
}
