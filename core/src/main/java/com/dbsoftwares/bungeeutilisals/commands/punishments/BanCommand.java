package com.dbsoftwares.bungeeutilisals.commands.punishments;

import com.dbsoftwares.bungeeutilisals.api.command.Command;
import com.dbsoftwares.bungeeutilisals.api.event.events.punishment.UserPunishEvent;
import com.dbsoftwares.bungeeutilisals.api.punishments.IPunishmentExecutor;
import com.dbsoftwares.bungeeutilisals.api.punishments.PunishmentInfo;
import com.dbsoftwares.bungeeutilisals.api.punishments.PunishmentType;
import com.dbsoftwares.bungeeutilisals.api.user.UserStorage;
import com.dbsoftwares.bungeeutilisals.api.user.interfaces.User;
import com.dbsoftwares.bungeeutilisals.api.utils.Utils;
import com.dbsoftwares.bungeeutilisals.api.utils.file.FileLocation;
import com.dbsoftwares.bungeeutilisals.BungeeUtilisals;

import java.util.Arrays;
import java.util.List;

public class BanCommand extends Command {

    public BanCommand() {
        super(
                "ban",
                Arrays.asList(FileLocation.PUNISHMENTS_CONFIG.getConfiguration().getString("commands.ban.aliases").split(", ")),
                FileLocation.PUNISHMENTS_CONFIG.getConfiguration().getString("commands.ban.permission")
        );
    }

    @Override
    public List<String> onTabComplete(User user, String[] args) {
        return null;
    }

    @Override
    public void onExecute(User user, String[] args) {
        if (args.length < 2) {
            user.sendLangMessage("punishments.ban.usage");
            return;
        }
        String reason = Utils.formatList(Arrays.copyOfRange(args, 1, args.length), " ");

        if (!BungeeUtilisals.getInstance().getDatabaseManagement().getDataManager().isUserPresent(args[0])) {
            user.sendLangMessage("never-joined");
            return;
        }
        UserStorage storage = BungeeUtilisals.getInstance().getDatabaseManagement().getDataManager().getUser(args[0]);
        if (BungeeUtilisals.getInstance().getDatabaseManagement().getDataManager().isBanPresent(storage.getUuid(), true)) {
            user.sendLangMessage("punishments.ban.already-banned");
            return;
        }

        UserPunishEvent event = new UserPunishEvent(PunishmentType.BAN, user, storage.getUuid(),
                storage.getUserName(), storage.getIp(), reason, user.getServerName(), null);
        api.getEventLoader().launchEvent(event);

        if (event.isCancelled()) {
            user.sendLangMessage("punishments.cancelled");
            return;
        }
        IPunishmentExecutor executor = api.getPunishmentExecutor();
        PunishmentInfo info = executor.addBan(storage.getUuid(), storage.getUserName(), storage.getIp(),
                reason, user.getServerName(), user.getName());

        api.getUser(storage.getUserName()).ifPresent(banned -> {
            String kick = Utils.formatList(banned.getLanguageConfig().getStringList("punishments.ban.kick"), "\n");
            kick = executor.setPlaceHolders(kick, info);

            banned.kick(kick);
        });

        user.sendLangMessage("punishments.ban.executed", executor.getPlaceHolders(info));

        api.langBroadcast("punishments.ban.broadcast",
                FileLocation.PUNISHMENTS_CONFIG.getConfiguration().getString("commands.ban.broadcast"),
                executor.getPlaceHolders(info).toArray(new Object[]{}));
    }
}