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

package com.dbsoftwares.bungeeutilisals.executors;

import com.dbsoftwares.bungeeutilisals.BungeeUtilisals;
import com.dbsoftwares.bungeeutilisals.api.BUCore;
import com.dbsoftwares.bungeeutilisals.api.data.StaffRankData;
import com.dbsoftwares.bungeeutilisals.api.event.event.Event;
import com.dbsoftwares.bungeeutilisals.api.event.event.EventExecutor;
import com.dbsoftwares.bungeeutilisals.api.event.events.network.NetworkStaffJoinEvent;
import com.dbsoftwares.bungeeutilisals.api.event.events.network.NetworkStaffLeaveEvent;
import com.dbsoftwares.bungeeutilisals.api.event.events.user.UserLoadEvent;
import com.dbsoftwares.bungeeutilisals.api.event.events.user.UserUnloadEvent;
import com.dbsoftwares.bungeeutilisals.api.user.interfaces.User;
import com.dbsoftwares.bungeeutilisals.api.utils.file.FileLocation;
import com.dbsoftwares.bungeeutilisals.redis.RedisMessageHandler;
import com.dbsoftwares.bungeeutilisals.redis.handlers.StaffRedisMessageHandler;
import com.dbsoftwares.bungeeutilisals.utils.redisdata.NetworkStaffConnectData;
import com.dbsoftwares.bungeeutilisals.utils.redisdata.NetworkStaffConnectData.StaffNetworkAction;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Comparator;
import java.util.List;

public class UserExecutor implements EventExecutor
{

    @Event
    public void onLoad( UserLoadEvent event )
    {
        User user = event.getUser();
        event.getApi().getUsers().add( user );
    }

    @Event
    public void onUnload( UserUnloadEvent event )
    {
        User user = event.getUser();
        event.getApi().getUsers().remove( user );
    }

    @Event
    public void onStaffLoad( UserLoadEvent event )
    {
        final User user = event.getUser();
        final StaffRankData rank = findStaffRank( user );

        if ( rank == null )
        {
            return;
        }

        if ( BungeeUtilisals.getInstance().getConfig().getBoolean( "redis" ) )
        {
            final RedisMessageHandler<NetworkStaffConnectData> handler =
                    BungeeUtilisals.getInstance().getRedisMessenger().getHandler( StaffRedisMessageHandler.class );

            final NetworkStaffConnectData data = new NetworkStaffConnectData(
                    StaffNetworkAction.STAFF_JOIN, user.getUuid(), user.getName(), rank.getName()
            );
            handler.send( data );
        } else
        {
            BUCore.getApi().getEventLoader().launchEvent(
                    new NetworkStaffJoinEvent( user.getName(), user.getUuid(), rank.getName() )
            );
        }
    }

    @Event
    public void onStaffUnload( UserUnloadEvent event )
    {
        final User user = event.getUser();
        final StaffRankData rank = findStaffRank( user );

        if ( rank == null )
        {
            return;
        }

        if ( BungeeUtilisals.getInstance().getConfig().getBoolean( "redis" ) )
        {
            final RedisMessageHandler<NetworkStaffConnectData> handler =
                    BungeeUtilisals.getInstance().getRedisMessenger().getHandler( StaffRedisMessageHandler.class );

            final NetworkStaffConnectData data = new NetworkStaffConnectData(
                    StaffNetworkAction.STAFF_LEAVE, user.getUuid(), user.getName(), rank.getName()
            );
            handler.send( data );
        } else
        {
            BUCore.getApi().getEventLoader().launchEvent(
                    new NetworkStaffLeaveEvent( user.getName(), user.getUuid(), rank.getName() )
            );
        }
    }

    private StaffRankData findStaffRank( final User user )
    {
        final ProxiedPlayer player = user.getParent();
        final List<StaffRankData> ranks = FileLocation.GENERALCOMMANDS.getDataList();

        return ranks.stream()
                .filter( rank -> player.hasPermission( rank.getPermission() ) )
                .max( Comparator.comparingInt( StaffRankData::getPriority ) )
                .orElse( null );
    }
}