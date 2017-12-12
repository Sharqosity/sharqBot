package sharqBot.Pickup;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.net.UnknownServiceException;

public class PickupUser {

    private User user;
    private int addTime;
    private int expireTime;
    private MessageChannel lastChannel;

    public PickupUser(User user, MessageChannel lastChannel) {

    }


}
