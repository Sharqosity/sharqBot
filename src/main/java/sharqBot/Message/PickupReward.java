package sharqBot.Message;

public class PickupReward {

    private double reward;
    private boolean streakEarned;

    public PickupReward(double reward, boolean streakEarned) {
        this.reward = reward;
        this.streakEarned = streakEarned;
    }

    public double getReward() {
        return reward;
    }

    public boolean getStreakEarned() {
        return streakEarned;
    }
}
