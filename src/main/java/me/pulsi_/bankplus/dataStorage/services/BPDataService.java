package me.pulsi_.bankplus.dataStorage.services;

import me.pulsi_.bankplus.dataStorage.PlayerAccountData;
import me.pulsi_.bankplus.dataStorage.repositories.IBPDataRepository;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

public class BPDataService implements IBPDataService {

    private final IBPDataRepository repository;

    public BPDataService(IBPDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void updatePlayer(PlayerAccountData playerAccountData) {
        repository.updatePlayer(playerAccountData);
    }

    @Override
    public boolean isPlayerRegistered(OfflinePlayer player, String bankName) {
        return repository.isRegistered(player, bankName);
    }

    @Override
    public void registerPlayer(OfflinePlayer player) {
        repository.registerPlayer(player);
    }

    @Override
    public void savePlayer(PlayerAccountData playerAccountData) {
        repository.savePlayer(playerAccountData);
    }


    @Override
    public Optional<PlayerAccountData> getPlayerAccountData(String bankName, OfflinePlayer player) {
        return repository.getPlayer(bankName, player);
    }

    @Override
    public void disconnect() {
        repository.disconnect();
    }

    @Override
    public void connect() {
        repository.connect();
    }

    @Override
    public boolean isConnected() {

        return repository.isConnected();
    }


}
