package me.pulsi_.bankplus.dataStorage.repositories.plainText;

import org.bukkit.OfflinePlayer;

import java.io.File;

public class BPDataRepositoryFileAccountName extends BPDataRepositoryFile {
    @Override
    protected File getPlayerFile(OfflinePlayer player) {
        return new File(parentFolder, player.getName() + ".yml");
    }
}
