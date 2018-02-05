package com.dbsoftwares.bungeeutilisals.bungee.commands.punishments;

import com.dbsoftwares.bungeeutilisals.api.command.Command;
import com.dbsoftwares.bungeeutilisals.api.event.events.punishment.UserPunishEvent;
import com.dbsoftwares.bungeeutilisals.api.punishments.IPunishmentExecutor;
import com.dbsoftwares.bungeeutilisals.api.punishments.PunishmentInfo;
import com.dbsoftwares.bungeeutilisals.api.punishments.PunishmentType;
import com.dbsoftwares.bungeeutilisals.api.user.UserStorage;
import com.dbsoftwares.bungeeutilisals.api.user.interfaces.User;
import com.dbsoftwares.bungeeutilisals.api.utils.Utils;
import com.dbsoftwares.bungeeutilisals.api.utils.file.FileLocation;
import com.dbsoftwares.bungeeutilisals.bungee.BungeeUtilisals;
import com.dbsoftwares.bungeeutilisals.bungee.storage.SQLStatements;

import java.util.Arrays;
import java.util.List;

public class TempBanCommand extends Command {

    public TempBanCommand() {
        super("tempban", Arrays.asList(BungeeUtilisals.getConfiguration(FileLocation.PUNISHMENTS_CONFIG)
                        .getString("commands.tempban.aliases").split(", ")),
                BungeeUtilisals.getConfiguration(FileLocation.PUNISHMENTS_CONFIG).getString("commands.tempban.permission"));
    }

    @Override
    public List<String> onTabComplete(User user, String[] args) {
        return null;
    }

    @Override
    public void onExecute(User user, String[] args) {
        if (args.length < 3) {
            user.sendLangMessage("punishments.tempban.usage");
            return;
        }
        String timeFormat = args[1];
        String reason = Utils.formatList(Arrays.copyOfRange(args, 2, args.length), " ");
        Long time = Utils.parseDateDiff(timeFormat);

        if (time == 0L) {
            user.sendLangMessage("punishments.tempban.non-valid");
            return;
        }
        if (!SQLStatements.isUserPresent(args[0])) {
            user.sendLangMessage("never-joined");
            return;
        }
        UserStorage storage = SQLStatements.getUser(args[0]);
        if (SQLStatements.isTempbanPresent(storage.getUuid(), true)) {
            user.sendLangMessage("punishments.tempban.already-banned");
            return;
        }

        UserPunishEvent event = new UserPunishEvent(PunishmentType.TEMPBAN, user, storage.getUuid(),
                storage.getUserName(), storage.getIp(), reason, user.getServerName(), time);
        api.getEventLoader().launchEvent(event);

        if (event.isCancelled()) {
            user.sendLangMessage("punishments.cancelled");
            return;
        }
        IPunishmentExecutor executor = api.getPunishmentExecutor();
        PunishmentInfo info = executor.addTempBan(storage.getUuid(), storage.getUserName(), storage.getIp(), time,
                reason, user.getServerName(), user.getName());

        api.getUser(storage.getUserName()).ifPresent(banned -> {
            String kick = Utils.formatList(banned.getLanguageConfig().getStringList("punishments.tempban.kick"), "\n");
            kick = executor.setPlaceHolders(kick, info);

            banned.kick(kick);
        });

        api.langBroadcast("punishments.tempban.broadcast",
                BungeeUtilisals.getConfiguration(FileLocation.PUNISHMENTS_CONFIG).getString("commands.tempban.broadcast"),
                executor.getPlaceHolders(info).toArray(new Object[]{}));
    }
}