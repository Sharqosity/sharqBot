package sharqBot.Message;
import net.dv8tion.jda.core.entities.User;
public class Bet {
    private User user;
    private int amount;
    public Bet(User user, int amount) {
        this.user = user;
        this.amount = amount;
    }
    public User getUser() {
        return user;
    }
    public int getAmount() {
        return amount;
    }
}