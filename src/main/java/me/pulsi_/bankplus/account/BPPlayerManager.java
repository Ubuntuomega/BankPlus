package me.pulsi_.bankplus.account;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import java.util.UUID;

public class BPPlayerManager {

    private final OfflinePlayer p;

    public BPPlayerManager(OfflinePlayer p) {
        this.p = p;
    }

    public BPPlayerManager(UUID uuid) {
        this.p = Bukkit.getOfflinePlayer(uuid);
    }


    public boolean isPlayerRegistered() {
        return BankPlus.INSTANCE().getDataManager().getPlayerController().isPlayerRegistered(p, ConfigValues.getMainGuiName());
    }

    public void registerPlayer() {
        BankPlus.INSTANCE().getDataManager().getPlayerController().registerPlayer(p);
    }


}