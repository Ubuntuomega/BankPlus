package me.pulsi_.bankplus.dataStorage.repositories.plainText;

import org.bukkit.OfflinePlayer;

import java.io.File;

public class BPDataRepositoryFileUuid extends BPDataRepositoryFile {
    @Override
    protected File getPlayerFile(OfflinePlayer player) {
        return new File(parentFolder, player.getUniqueId() + ".yml");
    }
}
