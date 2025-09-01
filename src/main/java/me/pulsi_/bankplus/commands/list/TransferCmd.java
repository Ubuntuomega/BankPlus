package me.pulsi_.bankplus.commands.list;

import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.commands.BPCmdExecution;
import me.pulsi_.bankplus.commands.BPCommand;
import me.pulsi_.bankplus.economy.BPEconomy;
import me.pulsi_.bankplus.dataStorage.PlayerAccountData;
import me.pulsi_.bankplus.dataStorage.controllers.BPDataController;
import me.pulsi_.bankplus.dataStorage.controllers.IBPDataController;
import me.pulsi_.bankplus.dataStorage.repositories.IBPDataRepository;
import me.pulsi_.bankplus.dataStorage.repositories.plainText.BPDataRepositoryFileAccountName;
import me.pulsi_.bankplus.dataStorage.repositories.plainText.BPDataRepositoryFileUuid;
import me.pulsi_.bankplus.dataStorage.repositories.sql.BPDataRepositorySqlAccountName;
import me.pulsi_.bankplus.dataStorage.repositories.sql.BPDataRepositorySqlUuid;
import me.pulsi_.bankplus.dataStorage.services.BPDataService;
import me.pulsi_.bankplus.utils.texts.BPArgs;
import me.pulsi_.bankplus.utils.texts.BPMessages;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TransferCmd extends BPCommand {

    public TransferCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public TransferCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Arrays.asList(
                "%prefix% Usage: /bank transfer [mode]",
                "Specify a mode between <aqua>\"filesToDatabase\"</aqua> and <aqua>\"databaseToFiles\"</aqua>.",
                "Use this command to transfer the playerdata from a place to another in case you switch saving mode."
        );
    }

    @Override
    public int defaultConfirmCooldown() {
        return 5;
    }

    @Override
    public List<String> defaultConfirmMessage() {
        return Collections.singletonList("%prefix% <red>This command will overwrite the data from a place to another, type the command again within 5 seconds to confirm.");
    }

    @Override
    public int defaultCooldown() {
        return 0;
    }

    @Override
    public List<String> defaultCooldownMessage() {
        return Collections.emptyList();
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public boolean skipUsage() {
        return false;
    }

    @Override
    public BPCmdExecution onExecution(CommandSender s, String[] args) {
        String mode = args[1].toLowerCase();

        if (!mode.equals("filestodatabase") && !mode.equals("databasetofiles")) {
            BPMessages.send(s, "Invalid-Action");
            return BPCmdExecution.invalidExecution();
        }

        if (!ConfigValues.isSqlEnabled()) {
            BPMessages.send(s, "%prefix% <red>Could not initialize the task, MySQL hasn't been enabled in the config file!", false);
            return BPCmdExecution.invalidExecution();
        }

        return new BPCmdExecution() {
            @Override
            public void execute() {
                BPMessages.send(s, "%prefix% Task initialized, wait a few moments...", false);

                Bukkit.getScheduler().runTaskAsynchronously(BankPlus.INSTANCE(), () -> {

                    // Initialices the SQL and File controllers
                    IBPDataController fileController;
                    IBPDataController sqlController;
                    IBPDataRepository fileRepo;
                    IBPDataRepository sqlRepo;

                    if (ConfigValues.isStoringUUIDs()) {
                        fileRepo = new BPDataRepositoryFileUuid();
                        sqlRepo = new BPDataRepositorySqlUuid(ConfigValues.getSqlUsername(), ConfigValues.getSqlPassword(), ConfigValues.getSqlHost(), ConfigValues.getSqlPort(), ConfigValues.getSqlDatabase(), ConfigValues.isSqlUsingSSL());
                    } else {
                        fileRepo = new BPDataRepositoryFileAccountName();
                        sqlRepo = new BPDataRepositorySqlAccountName(ConfigValues.getSqlUsername(), ConfigValues.getSqlPassword(), ConfigValues.getSqlHost(), ConfigValues.getSqlPort(), ConfigValues.getSqlDatabase(), ConfigValues.isSqlUsingSSL());
                    }

                    fileController = new BPDataController(new BPDataService(fileRepo));
                    sqlController = new BPDataController(new BPDataService(sqlRepo));


                    fileController.connect();
                    sqlController.connect();


                    if (args[1].equalsIgnoreCase("filestodatabase")) {
                        migratePlayers(fileController, sqlController);
                    } else {
                        migratePlayers(sqlController, fileController);
                    }

                    fileController.disconnect();
                    sqlController.disconnect();

                    BPMessages.send(s, "%prefix% Task finished!", false);
                });
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2)
            return BPArgs.getArgs(args, "databaseToFiles", "filesToDatabase");
        return null;
    }


    private void migratePlayers(IBPDataController origin, IBPDataController destination) {
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {

            for (String bankName : BPEconomy.nameList()) {
                PlayerAccountData playerAccountData = origin.getPlayerAccountData(bankName, player);

                destination.savePlayer(playerAccountData);
            }
        }
    }
}