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

package com.dbsoftwares.bungeeutilisals.commands.general.message;

import com.dbsoftwares.bungeeutilisals.BungeeUtilisals;
import com.dbsoftwares.bungeeutilisals.api.BUCore;
import com.dbsoftwares.bungeeutilisals.api.command.CommandCall;
import com.dbsoftwares.bungeeutilisals.api.data.StaffUser;
import com.dbsoftwares.bungeeutilisals.api.event.events.user.UserPrivateMessageEvent;
import com.dbsoftwares.bungeeutilisals.api.user.interfaces.User;
import com.dbsoftwares.bungeeutilisals.api.utils.Utils;

import java.util.List;
import java.util.Optional;

public class MsgCommandCall implements CommandCall
{

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        if ( args.size() < 2 )
        {
            user.sendLangMessage( "general-commands.msg.usage" );
            return;
        }
        final String name = args.get( 0 );

        if ( user.getName().equalsIgnoreCase( name ) )
        {
            user.sendLangMessage( "general-commands.msg.self-msg" );
            return;
        }

        if ( BUCore.getApi().getPlayerUtils().isOnline( name ) && !isHidden( name ) )
        {
            final Optional<User> optional = BUCore.getApi().getUser( name );
            final String message = String.join( " ", args.subList( 1, args.size() ) );

            if ( optional.isPresent() )
            {
                final User target = optional.get();

                if ( target.getStorage().getIgnoredUsers().stream().anyMatch( ignored -> ignored.equalsIgnoreCase( user.getName() ) ) )
                {
                    user.sendLangMessage( "general-commands.msg.ignored" );
                    return;
                }

                user.getStorage().setData( "MSG_LAST_USER", target.getName() );
                target.getStorage().setData( "MSG_LAST_USER", user.getName() );

                {
                    String msgMessage = target.buildLangMessage( "general-commands.msg.format.receive" );
                    msgMessage = Utils.c( msgMessage );
                    msgMessage = msgMessage.replace( "{sender}", user.getName() );
                    msgMessage = msgMessage.replace( "{message}", message );

                    target.sendRawMessage( msgMessage );
                }
                {
                    String msgMessage = user.buildLangMessage( "general-commands.msg.format.send" );
                    msgMessage = Utils.c( msgMessage );
                    msgMessage = msgMessage.replace( "{receiver}", target.getName() );
                    msgMessage = msgMessage.replace( "{message}", message );

                    user.sendRawMessage( msgMessage );
                }

                BUCore.getApi().getEventLoader().launchEventAsync( new UserPrivateMessageEvent( user, target, message ) );
            }
            else
            {
                user.sendLangMessage( "offline" );
            }
        }
        else
        {
            user.sendLangMessage( "offline" );
        }
    }

    private boolean isHidden( final String name )
    {
        for ( StaffUser user : BungeeUtilisals.getInstance().getStaffMembers() )
        {
            if ( user.getName().equalsIgnoreCase( name ) && user.isHidden() )
            {
                return true;
            }
        }
        return false;
    }
}
