package sharqBot.Music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

class GuildManager {

    public final AudioPlayer player;

    public final TrackScheduler trackScheduler;

    public final PlayerSendHandler sendHandler;

    GuildManager(AudioPlayerManager playerManager) {
        player = playerManager.createPlayer();
        trackScheduler = new TrackScheduler(player);
        sendHandler = new PlayerSendHandler(player);
        player.addListener(trackScheduler);
    }


}
