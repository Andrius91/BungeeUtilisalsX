package be.dieterblancke.bungeeutilisalsx.common;

import be.dieterblancke.bungeeutilisalsx.common.api.storage.AbstractStorageManager;
import be.dieterblancke.bungeeutilisalsx.common.api.utils.other.StaffUser;
import be.dieterblancke.bungeeutilisalsx.common.commands.CommandManager;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestBungeeUtilisalsX extends AbstractBungeeUtilisalsX
{

    private final Logger LOGGER = Logger.getLogger( "BungeeUtilisalsX" );

    @Override
    public void initialize()
    {
        api = mock( IBuXApi.class, Mockito.RETURNS_DEEP_STUBS );
    }

    @Override
    protected IBuXApi createBuXApi()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public CommandManager getCommandManager()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void loadPlaceHolders()
    {
        // do nothing
    }

    @Override
    protected void registerLanguages()
    {
        // do nothing
    }

    @Override
    protected void registerListeners()
    {
        // do nothing
    }

    @Override
    protected void registerPluginSupports()
    {

    }

    @Override
    public ProxyOperationsApi proxyOperations()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getDataFolder()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVersion()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<StaffUser> getStaffMembers()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public IPluginDescription getDescription()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Logger getLogger()
    {
        return LOGGER;
    }
}