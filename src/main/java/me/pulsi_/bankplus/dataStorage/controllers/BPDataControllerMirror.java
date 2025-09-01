package me.pulsi_.bankplus.dataStorage.controllers;

import me.pulsi_.bankplus.dataStorage.PlayerAccountData;
import me.pulsi_.bankplus.dataStorage.services.IBPDataService;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.Optional;

public class BPDataControllerMirror implements IBPDataController {

    private final IBPDataService mainService;
    private final IBPDataService backupService;


    public BPDataControllerMirror(IBPDataService mainService, IBPDataService backupService) {
        this.mainService = mainService;

        this.backupService = backupService;
    }

    @Override
    public void connect() {
        mainService.connect();
        backupService.connect();
    }

    @Override
    public boolean isConnected() {
        return mainService.isConnected();
    }

    @Override
    public void disconnect() {
        mainService.disconnect();
        backupService.disconnect();
    }

    @Override
    public void updatePlayer(PlayerAccountData playerAccountData) {
        mainService.updatePlayer(playerAccountData);
        backupService.updatePlayer(playerAccountData);
    }

    @Override
    public void savePlayer(PlayerAccountData playerAccountData) {
        mainService.savePlayer(playerAccountData);
        backupService.savePlayer(playerAccountData);
    }

    @Override
    public boolean isPlayerRegistered(OfflinePlayer player, String bankName) {
        return mainService.isPlayerRegistered(player, bankName);
    }

    @Override
    public void registerPlayer(OfflinePlayer player) {
        mainService.registerPlayer(player);
        backupService.registerPlayer(player);
    }


    @Override
    public PlayerAccountData getPlayerAccountData(String bankName, OfflinePlayer player) {

        Optional<PlayerAccountData> optionalPlayerAccountData = mainService.getPlayerAccountData(bankName, player);

        if (optionalPlayerAccountData.isPresent()) {
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
