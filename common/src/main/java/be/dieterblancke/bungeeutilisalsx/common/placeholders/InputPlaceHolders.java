package be.dieterblancke.bungeeutilisalsx.common.placeholders;

import be.dieterblancke.bungeeutilisalsx.common.api.placeholder.PlaceHolderAPI;
import be.dieterblancke.bungeeutilisalsx.common.api.placeholder.PlaceHolderPack;
import be.dieterblancke.bungeeutilisalsx.common.api.placeholder.event.InputPlaceHolderEvent;
import be.dieterblancke.bungeeutilisalsx.common.api.placeholder.event.handler.InputPlaceHolderEventHandler;
import be.dieterblancke.bungeeutilisalsx.common.api.utils.Utils;
import be.dieterblancke.bungeeutilisalsx.common.api.utils.config.ConfigFiles;
import be.dieterblancke.bungeeutilisalsx.common.api.utils.server.ServerGroup;
import be.dieterblancke.configuration.api.IConfiguration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
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
                    final Date parsedDate = dateFormat.parse( event.getArgument() );
                    final Date date = ConfigFiles.CONFIG.isEnabled( "timezone" )
                            ? Date.from( parsedDate.toInstant().atZone( ZoneId.of( ConfigFiles.CONFIG.getConfig().getString( "timezone.zone" ) ) ).toInstant() )
                            : parsedDate;

                    return Utils.getTimeLeft(
                            configuration.getString( "placeholders.timeleft" ),
                            date
                    );
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