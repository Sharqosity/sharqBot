package sharqBot.Message;

public class PickupReward {

    private int reward;
    private boolean streakEarned;

    public PickupReward(int reward, boolean streakEarned) {
        this.reward = reward;
        this.streakEarned = streakEarned;
    }

    public int getReward() {
        return reward;
    }

    public boolean getStreakEarned() {
        return streakEarned;
    }
}