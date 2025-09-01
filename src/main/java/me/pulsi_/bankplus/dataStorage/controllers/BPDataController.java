package me.pulsi_.bankplus.dataStorage.controllers;

import me.pulsi_.bankplus.dataStorage.PlayerAccountData;
import me.pulsi_.bankplus.dataStorage.services.IBPDataService;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.Optional;

public class BPDataController implements IBPDataController {


    private final IBPDataService service;

    public BPDataController(IBPDataService service) {
        this.service = service;

    }

    @Override
    public void connect() {
        service.connect();
    }

    @Override
    public boolean isConnected() {
        return service.isConnected();
    }

    @Override
    public void disconnect() {
        service.disconnect();
    }

    @Override
    public void registerPlayer(OfflinePlayer player) {
        service.registerPlayer(player);
    }


    @Override
    public void updatePlayer(PlayerAccountData playerAccountData) {
        service.updatePlayer(playerAccountData);
    }

    @Override
    public void savePlayer(PlayerAccountData playerAccountData) {
        service.savePlayer(playerAccountData);
    }

    @Override
    public boolean isPlayerRegistered(OfflinePlayer player, String bankName) {
        return service.isPlayerRegistered(player, bankName);
    }

    @Override
    public PlayerAccountData getPlayerAccountData(String bankName, OfflinePlayer player) {
        Optional<PlayerAccountData> optionalPlayerAccountData = service.getPlayerAccountData(bankName, player);

        if (optionalPlayerAccountData.isPresent()){
            return optionalPlayerAccountData.get();
        }

        //If the player is not registered, we will create a new one.
        registerPlayer(player);

        return new PlayerAccountData(bankName,
                player.getUniqueId(),
                player.getName(),
                1,
                new BigDecimal(ConfigValues.getMainGuiName().equals(bankName) ? ConfigValues.getStartAmount().toString() : "0"),
                BigDecimal.ZERO,
                BigDecimal.ZERO);


    }

}