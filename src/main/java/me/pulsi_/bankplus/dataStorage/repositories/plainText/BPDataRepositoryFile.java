package me.pulsi_.bankplus.dataStorage.repositories.plainText;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.bankSystem.Bank;
import me.pulsi_.bankplus.bankSystem.BankRegistry;
import me.pulsi_.bankplus.dataStorage.PlayerAccountData;
import me.pulsi_.bankplus.dataStorage.repositories.IBPDataRepository;
import me.pulsi_.bankplus.utils.BPLogger;
import me.pulsi_.bankplus.utils.texts.BPFormatter;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

public abstract class BPDataRepositoryFile implements IBPDataRepository {

    protected File parentFolder;
    protected BPDataRepositoryFilePlayerJoinFileFixer playerJoinListener;

    public BPDataRepositoryFile() {
        parentFolder = new File(BankPlus.INSTANCE().getDataFolder(), "playerdata");
    }

    @Override
    public boolean connect() {
        try {
            if (!parentFolder.exists()) {
                return parentFolder.mkdir();
            }

            playerJoinListener = new BPDataRepositoryFilePlayerJoinFileFixer();

            BankPlus.INSTANCE().getServer().getPluginManager().registerEvents(playerJoinListener, BankPlus.INSTANCE());


        } catch (SecurityException e) {
            BPLogger.Console.warn("Something went wrong while creating folder \"" + parentFolder.getAbsolutePath() + "\": " + e.getMessage());
        }

        return false;
    }

    @Override
    public void disconnect() {
        if (playerJoinListener != null) {
            HandlerList.unregisterAll(playerJoinListener);
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return parentFolder.exists();

        } catch (SecurityException e) {
            BPLogger.Console.warn("Something went wrong while checking if exist folder \"" + parentFolder.getAbsolutePath() + "\": " + e.getMessage());
        }

        return false;
    }


    @Override
    public boolean isRegistered(OfflinePlayer player, String bankName) {
        return getPlayerFile(player).exists();
    }

    @Override
    public void updatePlayer(PlayerAccountData playerAccountData) {
        File file = getPlayerFile(Bukkit.getOfflinePlayer(playerAccountData.uuid()));
        FileConfiguration config = getPlayerConfig(file);

        Bank bank = BankRegistry.getBank(playerAccountData.bankName());
        String bankName = bank.getIdentifier();

        config.set("banks." + bankName + ".level", playerAccountData.bankLevel());
        config.set("banks." + bankName + ".money", BPFormatter.styleBigDecimal(playerAccountData.money()));
        config.set("banks." + bankName + ".debt", BPFormatter.styleBigDecimal(playerAccountData.debt()));
        config.set("banks." + bankName + ".interest", BPFormatter.styleBigDecimal(playerAccountData.interest()));


        savePlayerFile(config, file);

    }


    @Override
    public void savePlayer(PlayerAccountData playerAccountData) {
        registerPlayer(Bukkit.getOfflinePlayer(playerAccountData.uuid()));
        updatePlayer(playerAccountData);
    }

    @Override
    public void registerPlayer(OfflinePlayer player) {
        File file = getPlayerFile(player);
        if (file.exists()) {
            return;
        }

        try {
            file.getParentFile().mkdir();
            file.createNewFile();
        } catch (IOException e) {
            BPLogger.Console.warn("Something went wrong while registering " + player.getName() + ": " + e.getMessage());
        }
    }

    @Override
    public Optional<PlayerAccountData> getPlayer(String bankName, OfflinePlayer player) {
        String configPrefix = "banks." + bankName;

        FileConfiguration config = getPlayerConfig(getPlayerFile(player));

        if (isRegistered(player, bankName)) {
            int bankLevel = config.getInt(configPrefix + ".level", 1);
            BigDecimal money = BPFormatter.getStyledBigDecimal(config.getString(configPrefix + ".money"));
            BigDecimal interest = BPFormatter.getStyledBigDecimal(config.getString(configPrefix + ".money"));
            BigDecimal debt = BPFormatter.getStyledBigDecimal(config.getString(configPrefix + ".money"));

            return Optional.of(new PlayerAccountData(bankName, player.getUniqueId(), player.getName(), bankLevel, money, interest, debt));
        }


        return Optional.empty();
    }


    public void checkForFileFixes(OfflinePlayer p) {
        File file = getPlayerFile(p);
        FileConfiguration config = getPlayerConfig(file);
        boolean hasChanges = false;

        String sName = config.getString("name");
        if (sName == null) {
            config.set("name", p.getName());
            hasChanges = true;
        }

        for (Bank bank : BankRegistry.getBanks().values()) {
            String bankName = bank.getIdentifier();
            String sBalance = config.getString("banks." + bankName + ".money");
            String sLevel = config.getString("banks." + bankName + ".level");
            String sDebt = config.getString("banks." + bankName + ".debt");

            if (sLevel == null) {
                config.set("banks." + bankName + ".level", 1);
                hasChanges = true;
            }
            if (sBalance == null) {
                BigDecimal amount = ConfigValues.getMainGuiName().equals(bankName) ? ConfigValues.getStartAmount() : BigDecimal.valueOf(0);
                config.set("banks." + bankName + ".money", BPFormatter.styleBigDecimal(amount));
                hasChanges = true;
            }
            if (sDebt == null) {
                config.set("banks." + bankName + ".debt", "0");
                hasChanges = true;
            }
            if (ConfigValues.isNotifyingOfflineInterest()) {
                String sInterest = config.getString("banks." + bankName + ".interest");
                if (sInterest == null) {
                    config.set("banks." + bankName + ".interest", "0");
                    hasChanges = true;
                }
            }
        }


        if (hasChanges) {
            savePlayerFile(config, file);
        }
    }


    protected void savePlayerFile(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            BPLogger.Console.error(e.getMessage());
        }
    }


    //Differs
    protected abstract File getPlayerFile(OfflinePlayer player);

    protected FileConfiguration getPlayerConfig(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }


    protected class BPDataRepositoryFilePlayerJoinFileFixer implements Listener {

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            checkForFileFixes(event.getPlayer());
        }


    }

}
