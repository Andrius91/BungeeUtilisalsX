/*
 * Copyright (C) 2018 DBSoftwares - Dieter Blancke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package be.dieterblancke.bungeeutilisalsx.common.commands.punishments;

import be.dieterblancke.bungeeutilisalsx.common.BuX;
import be.dieterblancke.bungeeutilisalsx.common.api.event.events.punishment.UserPunishmentFinishEvent;
import be.dieterblancke.bungeeutilisalsx.common.api.punishments.IPunishmentHelper;
import be.dieterblancke.bungeeutilisalsx.common.api.punishments.PunishmentInfo;
import be.dieterblancke.bungeeutilisalsx.common.api.punishments.PunishmentType;
import be.dieterblancke.bungeeutilisalsx.common.api.user.UserStorage;
import be.dieterblancke.bungeeutilisalsx.common.api.user.interfaces.User;
import be.dieterblancke.bungeeutilisalsx.common.api.utils.config.ConfigFiles;

import java.util.List;

public class BanCommandCall extends PunishmentCommand
{

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        final PunishmentArgs punishmentArgs = loadArguments( user, args, false );

        if ( punishmentArgs == null )
        {
            user.sendLangMessage( "punishments.ban.usage" + ( useServerPunishments() ? "-server" : "" ) );
            return;
        }
        if ( punishmentArgs.isSelfPunishment() )
        {
            user.sendLangMessage( "punishments.self-punishment" );
            return;
        }
        if ( !punishmentArgs.hasJoined() )
        {
            user.sendLangMessage( "never-joined" );
            return;
        }

        final String reason = punishmentArgs.getReason();
        final UserStorage storage = punishmentArgs.getStorage();
        if ( dao().getPunishmentDao().getBansDao().isBanned( storage.getUuid(), punishmentArgs.getServerOrAll() ) )
        {
            user.sendLangMessage( "punishments.ban.already-banned" );
            return;
        }

        if ( punishmentArgs.launchEvent( PunishmentType.BAN ) )
        {
            return;
        }
        final IPunishmentHelper executor = BuX.getApi().getPunishmentExecutor();

        final PunishmentInfo info = dao().getPunishmentDao().getBansDao().insertBan(
                storage.getUuid(),
                storage.getUserName(),
                storage.getIp(),
                reason,
                punishmentArgs.getServerOrAll(),
                true,
                user.getName()
        );

        // Attempting to kick if player is online. If briding is enabled and player is not online, it will attempt to kick on other bungee's.
        super.attemptKick( storage, "punishments.ban.kick", info );

        user.sendLangMessage( "punishments.ban.executed", executor.getPlaceHolders( info ).toArray( new Object[0] ) );

        if ( !parameters.contains( "-s" ) )
        {
            if ( parameters.contains( "-nbp" ) )
            {
                BuX.getApi().langBroadcast(
                        "punishments.ban.broadcast",
                        executor.getPlaceHolders( info ).toArray( new Object[]{} )
                );
            }
            else
            {
                BuX.getApi().langPermissionBroadcast(
                        "punishments.ban.broadcast",
                        ConfigFiles.PUNISHMENT_CONFIG.getConfig().getString( "commands.ban.broadcast" ),
                        executor.getPlaceHolders( info ).toArray( new Object[]{} )
                );
            }
        }

        BuX.getApi().getEventLoader().launchEvent( new UserPunishmentFinishEvent(
                PunishmentType.BAN,
                user,
                storage.getUuid(),
                storage.getUserName(),
                storage.getIp(),
                reason,
                punishmentArgs.getServerOrAll(),
                null
        ) );
    }
}