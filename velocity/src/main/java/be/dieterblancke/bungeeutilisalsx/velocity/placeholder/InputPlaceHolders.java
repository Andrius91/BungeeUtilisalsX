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

package be.dieterblancke.bungeeutilisalsx.velocity.placeholder;

import be.dieterblancke.bungeeutilisalsx.common.api.placeholder.PlaceHolderAPI;
import be.dieterblancke.bungeeutilisalsx.common.api.placeholder.PlaceHolderPack;
import be.dieterblancke.bungeeutilisalsx.common.api.placeholder.event.InputPlaceHolderEvent;
import be.dieterblancke.bungeeutilisalsx.common.api.placeholder.event.handler.InputPlaceHolderEventHandler;
import be.dieterblancke.bungeeutilisalsx.common.api.utils.Utils;
import be.dieterblancke.bungeeutilisalsx.common.api.utils.config.ConfigFiles;
import be.dieterblancke.bungeeutilisalsx.common.api.utils.server.ServerGroup;
import com.dbsoftwares.configuration.api.IConfiguration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InputPlaceHolders implements PlaceHolderPack
{

    @Override
    public void loadPack()
    {
        PlaceHolderAPI.addPlaceHolder( false, "timeleft", new InputPlaceHolderEventHandler()
        {
            @Override
            public String getReplacement( InputPlaceHolderEvent event )
            {
                final IConfiguration configuration = Utils.getLanguageConfiguration( event.getUser() ).getConfig();
                final SimpleDateFormat dateFormat = new SimpleDateFormat( "dd-MM-yyyy kk:mm:ss" );

                try
                {
                    final Date date = dateFormat.parse( event.getArgument() );

                    return Utils.getTimeLeft( configuration.getString( "placeholders.timeleft" ), date );
                }
                catch ( ParseException e )
                {
                    return "";
                }
            }
        } );
        PlaceHolderAPI.addPlaceHolder( false, "getcount", new InputPlaceHolderEventHandler()
        {
            @Override
            public String getReplacement( InputPlaceHolderEvent event )
            {
                final ServerGroup server = ConfigFiles.SERVERGROUPS.getServer( event.getArgument() );

                if ( server == null )
                {
                    return "0";
                }

                return String.valueOf( server.getPlayers() );
            }
        } );
    }
}