package com.dbsoftwares.bungeeutilisals.api.event.events.punishment;

import com.dbsoftwares.bungeeutilisals.api.event.AbstractEvent;
import com.dbsoftwares.bungeeutilisals.api.punishmentts.PunishmentInfo;
import com.dbsoftwares.bungeeutilisals.api.user.User;
import lombok.Getter;
import lombok.Setter;

/**
 * This event will be executed upon User kick.
 */
public class UserKickEvent extends AbstractEvent {

    @Getter @Setter String userName;
    @Getter @Setter String bannerName;
    @Getter @Setter PunishmentInfo info;

    public UserKickEvent(String user, String banner, PunishmentInfo info) {
        this.userName = user;
        this.bannerName = banner;
        this.info = info;
    }

    public User getUser() {
        return getApi().getUser(userName).orElse(null);
    }

    public User getBanner() {
        return getApi().getUser(bannerName).orElse(null);
    }
}
