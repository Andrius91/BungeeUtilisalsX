package be.dieterblancke.bungeeutilisalsx.common.api.punishments;

import be.dieterblancke.bungeeutilisalsx.common.api.utils.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PunishmentAction
{

    private String uid;
    private PunishmentType type;
    private TimeUnit unit;
    private int time;
    private int limit;
    private List<String> actions;

}
