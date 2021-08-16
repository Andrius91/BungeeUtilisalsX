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

package be.dieterblancke.bungeeutilisalsx.common.api.friends;

import be.dieterblancke.bungeeutilisalsx.common.api.utils.config.ConfigFiles;
import com.dbsoftwares.configuration.api.IConfiguration;

import java.util.Arrays;

public enum FriendSetting
{

    REQUESTS( Boolean.class ),
    MESSAGES( Boolean.class ),
    SERVER_SWITCH( Boolean.class ),
    FRIEND_BROADCAST( Boolean.class );

    private final Class<?> valueType;

    FriendSetting( final Class<?> valueType )
    {
        this.valueType = valueType;
    }

    public static FriendSetting[] getEnabledSettings()
    {
        return Arrays.stream( values() )
                .filter( setting -> ConfigFiles.FRIENDS_CONFIG.getConfig().getBoolean( "settings." + setting.toString().toLowerCase() ) )
                .toArray( FriendSetting[]::new );
    }

    public String getName()
    {
        return toString().charAt( 0 ) + toString().substring( 1 ).toLowerCase();
    }

    public String getName( final IConfiguration language )
    {
        return language.exists( "friends.settings.type." + toString().toLowerCase() )
                ? language.getString( "friends.settings.type." + toString().toLowerCase() )
                : getName();
    }

    public boolean getDefault()
    {
        return ConfigFiles.FRIENDS_CONFIG.getConfig().getBoolean( "settings." + toString().toLowerCase() );
    }

    public boolean isBooleanType()
    {
        return this.valueType == Boolean.class;
    }
}
