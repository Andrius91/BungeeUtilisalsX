package com.dbsoftwares.bungeeutilisals.commands;

/*
 * Created by DBSoftwares on 10/01/2018
 * Developer: Dieter Blancke
 * Project: BungeeUtilisals
 */

import com.dbsoftwares.bungeeutilisals.api.BUCore;
import com.dbsoftwares.bungeeutilisals.api.command.Command;
import com.dbsoftwares.bungeeutilisals.api.language.Language;
import com.dbsoftwares.bungeeutilisals.api.user.interfaces.User;
import com.dbsoftwares.bungeeutilisals.api.utils.file.FileLocation;
import com.dbsoftwares.bungeeutilisals.BungeeUtilisals;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;

public class PluginCommand extends Command {

    public PluginCommand() {
        super("bungeeutilisals", Lists.newArrayList("bu", "butilisals", "butili"), "bungeeutilisals.admin");
    }

    @Override
    public List<String> onTabComplete(User user, String[] args) {
        return ImmutableList.of();
    }

    @Override
    public void onExecute(User user, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                for (FileLocation location : FileLocation.values()) {
                    try {
                        location.getConfiguration().reload();

                        location.getData().clear();
                        location.loadData();
                    } catch (IOException e) {
                        e.printStackTrace();
                        user.sendMessage("&fCould not reload " + location.toString().toLowerCase().replace("_", " ") + "!");
                    }
                }
                BungeeUtilisals.getInstance().reload();

                for (Language language : BUCore.getApi().getLanguageManager().getLanguages()) {
                    BUCore.getApi().getLanguageManager().reloadConfig(BungeeUtilisals.getInstance(), language);
                }

                user.sendMessage("&fAll configuration files have been reloaded!");
                return;
            } else if (args[0].equalsIgnoreCase("version")) {
                user.sendMessage("&fYou are running BungeeUtilisals v&c" + BungeeUtilisals.getInstance().getDescription().getVersion() + "&f!");
                return;
            }
        }

        user.sendMessage("&fBungeeUtilisals made by &cdidjee2&f:");
        user.sendMessage("&f- /bu reload");
        user.sendMessage("&f- /bu version");
    }
}
