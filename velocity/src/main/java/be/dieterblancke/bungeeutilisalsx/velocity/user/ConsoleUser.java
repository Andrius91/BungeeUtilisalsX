package be.dieterblancke.bungeeutilisalsx.velocity.user;

import be.dieterblancke.bungeeutilisalsx.common.BuX;
import be.dieterblancke.bungeeutilisalsx.common.api.friends.FriendData;
import be.dieterblancke.bungeeutilisalsx.common.api.friends.FriendSettings;
import be.dieterblancke.bungeeutilisalsx.common.api.language.Language;
import be.dieterblancke.bungeeutilisalsx.common.api.language.LanguageConfig;
import be.dieterblancke.bungeeutilisalsx.common.api.storage.dao.MessageQueue;
import be.dieterblancke.bungeeutilisalsx.common.api.user.UserCooldowns;
import be.dieterblancke.bungeeutilisalsx.common.api.user.UserStorage;
import be.dieterblancke.bungeeutilisalsx.common.api.user.interfaces.User;
import be.dieterblancke.bungeeutilisalsx.common.api.utils.Utils;
import be.dieterblancke.bungeeutilisalsx.common.api.utils.Version;
import be.dieterblancke.bungeeutilisalsx.common.api.utils.other.IProxyServer;
import be.dieterblancke.bungeeutilisalsx.common.api.utils.other.QueuedMessage;
import be.dieterblancke.bungeeutilisalsx.velocity.Bootstrap;
import com.dbsoftwares.configuration.api.IConfiguration;
import com.google.common.collect.Lists;
import com.velocitypowered.proxy.console.VelocityConsole;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class ConsoleUser implements User
{

    private static final String NOT_SUPPORTED = "The console does not support this operation!";
    private static org.apache.logging.log4j.Logger velocityConsoleOutput;
    private final UserStorage storage = new UserStorage();
    private final UserCooldowns cooldowns = new UserCooldowns();
    @Getter
    private final List<FriendData> friends = Lists.newArrayList();
    @Getter
    @Setter
    private boolean socialSpy;
    @Getter
    @Setter
    private boolean commandSpy;

    private static org.apache.logging.log4j.Logger getConsoleOutput()
    {
        if ( velocityConsoleOutput == null )
        {
            try
            {
                final Field loggerField = VelocityConsole.class.getDeclaredField( "logger" );
                loggerField.setAccessible( true );

                velocityConsoleOutput = (Logger) loggerField.get( null );
            }
            catch ( IllegalAccessException | NoSuchFieldException e )
            {
                e.printStackTrace();
            }
        }
        return velocityConsoleOutput;
    }

    @Override
    public void load( UUID uuid )
    {
        // do nothing
    }

    @Override
    public void unload()
    {
        // do nothing
    }

    @Override
    public void save()
    {
        // do nothing
    }

    @Override
    public UserStorage getStorage()
    {
        return storage;
    }

    @Override
    public UserCooldowns getCooldowns()
    {
        return cooldowns;
    }

    @Override
    public String getIp()
    {
        return "127.0.0.1";
    }

    @Override
    public Language getLanguage()
    {
        return BuX.getApi().getLanguageManager().getDefaultLanguage();
    }

    @Override
    public void setLanguage( Language language )
    {
        // do nothing
    }

    @Override
    public void sendRawMessage( String message )
    {
        sendMessage( TextComponent.fromLegacyText( message ) );
    }

    @Override
    public void sendRawColorMessage( String message )
    {
        if ( message.isEmpty() )
        {
            return;
        }
        sendMessage( Utils.format( message ) );
    }

    @Override
    public void sendMessage( String message )
    {
        if ( message.isEmpty() )
        {
            return;
        }
        sendMessage( getLanguageConfig().getConfig().getString( "prefix" ), message );
    }

    @Override
    public void sendMessage( String prefix, String message )
    {
        sendMessage( Utils.format( prefix + message ) );
    }

    @Override
    public void sendMessage( BaseComponent component )
    {
        if ( this.isEmpty( component ) )
        {
            return;
        }

        getConsoleOutput().info( BaseComponent.toLegacyText( component ) );
    }

    @Override
    public void sendMessage( BaseComponent[] components )
    {
        if ( this.isEmpty( components ) )
        {
            return;
        }
        getConsoleOutput().info( BaseComponent.toLegacyText( components ) );
    }

    @Override
    public void kick( String reason )
    {
        // do nothing
    }

    @Override
    public void langKick( String path, Object... placeholders )
    {
        // do nothing
    }

    @Override
    public void forceKick( String reason )
    {
        // do nothing
    }

    @Override
    public String getName()
    {
        return "CONSOLE";
    }

    @Override
    public UUID getUuid()
    {
        return UUID.randomUUID();
    }

    @Override
    public void sendNoPermMessage()
    {
        sendLangMessage( "no-permission" );
    }

    @Override
    public int getPing()
    {
        return 0;
    }

    @Override
    public boolean isConsole()
    {
        return true;
    }

    @Override
    public String getServerName()
    {
        return "BUNGEE";
    }

    @Override
    public boolean isInStaffChat()
    {
        return false;
    }

    @Override
    public void setInStaffChat( boolean staffchat )
    {
        // do nothing
    }

    @Override
    public void sendToServer( IProxyServer proxyServer )
    {
        // do nothing
    }

    @Override
    public Version getVersion()
    {
        return Version.latest();
    }

    @Override
    public FriendSettings getFriendSettings()
    {
        throw new UnsupportedOperationException( NOT_SUPPORTED );
    }

    @Override
    public boolean hasPermission( String permission )
    {
        return Bootstrap.getInstance().getProxyServer().getConsoleCommandSource().hasPermission( permission );
    }

    @Override
    public boolean hasPermission( String permission, boolean specific )
    {
        return Bootstrap.getInstance().getProxyServer().getConsoleCommandSource().hasPermission( permission );
    }

    @Override
    public boolean hasAnyPermission( String... permissions )
    {
        for ( String permission : permissions )
        {
            if ( Bootstrap.getInstance().getProxyServer().getConsoleCommandSource().hasPermission( permission ) )
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public MessageQueue<QueuedMessage> getMessageQueue()
    {
        return null;
    }

    @Override
    public void executeMessageQueue()
    {
        // do nothing
    }

    @Override
    public void executeCommand( final String command )
    {
        Bootstrap.getInstance().getProxyServer().getCommandManager().executeAsync(
                Bootstrap.getInstance().getProxyServer().getConsoleCommandSource(),
                command
        );
    }

    @Override
    public void sendActionBar( String actionbar )
    {
        // do nothing
    }

    @Override
    public void sendTitle( String title, String subtitle, int fadein, int stay, int fadeout )
    {
        // do nothing
    }

    @Override
    public boolean isMsgToggled()
    {
        return false;
    }

    @Override
    public void setMsgToggled( boolean status )
    {
        // do nothing
    }

    @Override
    public void sendPacket( final Object packet )
    {
        // do nothing
    }

    @Override
    public void setTabHeader( BaseComponent[] header, BaseComponent[] footer )
    {
        // do nothing
    }

    @Override
    public String getJoinedHost()
    {
        throw new UnsupportedOperationException( NOT_SUPPORTED );
    }

    @Override
    public boolean isVanished()
    {
        return false;
    }
}
