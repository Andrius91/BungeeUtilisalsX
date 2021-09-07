package be.dieterblancke.bungeeutilisalsx.bungee.utils;

import be.dieterblancke.bungeeutilisalsx.common.motd.MotdConnection;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetSocketAddress;

@Data
@AllArgsConstructor
public class BungeeMotdConnection implements MotdConnection
{

    private final int version;
    private final String name;
    private final InetSocketAddress virtualHost;

}