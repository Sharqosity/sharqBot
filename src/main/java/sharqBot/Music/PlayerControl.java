package sharqBot.Music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;
import sharqBot.Main;

import java.util.HashMap;
import java.util.Map;

public class PlayerControl extends ListenerAdapter {
    private final AudioPlayerManager playerManager;
    private final Map<String, GuildManager> musicManagers;


    //TODO volume function
    private final int DEFAULT_VOLUME = 75;
    private int volume = 50;


    public PlayerControl() {
        playerManager = new DefaultAudioPlayerManager();
        musicManagers = new HashMap<>();
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        Channel channelLeft = event.getChannelLeft();
        if (channelLeft.getMembers().size() <= 1) {
            event.getGuild().getAudioManager().closeAudioConnection();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromType(ChannelType.TEXT)) {
            return;
        }
        if (event.getAuthor().isBot()) {
            return;
        }

        Guild guild = event.getGuild();
        GuildManager guildManager = getMusicManager(guild);
        AudioPlayer player = guildManager.player;
        TrackScheduler trackScheduler = guildManager.trackScheduler;

        Message message = event.getMessage();
        String content = message.getRawContent();

        String[] command;

        command = content.split(" ");

        if (command[0].equals("<@384172837218287616>") && command[1].equalsIgnoreCase("leave")) {
            guild.getAudioManager().setSendingHandler(null);
            guild.getAudioManager().closeAudioConnection();
        }

        if (Main.isMuntTTSIsOn()) {
            if ((command[0].equalsIgnoreCase("!MTS"))) {

                VoiceChannel vc = event.getMember().getVoiceState().getChannel();
                if (vc == null) {
                    event.getChannel().sendMessage("You must be in a voice channel to use this command!").queue();
                    return;
                }
                AudioManager manager = guild.getAudioManager();
                manager.setSendingHandler(new PlayerSendHandler(player));

                AudioSourceManagers.registerLocalSource(playerManager);

                guild.getAudioManager().setSendingHandler(guildManager.sendHandler);
                guild.getAudioManager().openAudioConnection(vc);

                for (int i = 1; i < command.length; i++) {
                    final String[] finalCommand = command;

                    final int eye = i;
                    playerManager.loadItemOrdered(manager, "./src/muntDict/" + command[eye] + ".mp3", new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack audioTrack) {
                            trackScheduler.queue(audioTrack);
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist audioPlaylist) {

                        }

                        @Override
                        public void noMatches() {
                            event.getChannel().sendMessage("Invalid word: " + finalCommand[eye]).queue();
                        }

                        @Override
                        public void loadFailed(FriendlyException e) {
                            event.getChannel().sendMessage("Load failed on word: " + finalCommand[eye]).queue();
                            e.printStackTrace();

                        }
                    });

                }
                return;
            }
        } else {
            if (command[0].equalsIgnoreCase("!MTS")) {
                event.getChannel().sendMessage("Munt TTS is currently off, please get someone sensible to enable it").queue();
                return;
            }
        }


        if (content.startsWith("!")) {

            VoiceChannel vc = event.getMember().getVoiceState().getChannel();
            if (vc == null) {
//                event.getChannel().sendMessage("You must be in a voice channel to use this command!").queue();
                return;
            }

            AudioSourceManagers.registerLocalSource(playerManager);

            guild.getAudioManager().setSendingHandler(guildManager.sendHandler);

            String fileName = content.substring(1, content.length());
            playerManager.loadItem("./src/resources/" + fileName + ".mp3", new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    guild.getAudioManager().openAudioConnection(vc);
                    trackScheduler.playNow(track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    for (AudioTrack track : playlist.getTracks()) {
                        trackScheduler.queue(track);
                    }
                }

                @Override
                public void noMatches() {
                    // Notify the user that we've got nothing
//                    event.getChannel().sendMessage("No matches found!").queue();

                }

                @Override
                public void loadFailed(FriendlyException throwable) {
                    // Notify the user that everything exploded
                    System.out.println(throwable.getMessage());
                }
            });

        } else {
            command = content.split(" ");

            if (command[0].equals("<@384172837218287616>") && (command[1].equalsIgnoreCase("play") || (command[1].equalsIgnoreCase("queue")))) {

                VoiceChannel vc = event.getMember().getVoiceState().getChannel();
                if (vc == null) {
                    event.getChannel().sendMessage("You must be in a voice channel to use this command!").queue();
                    return;
                }

                guild.getAudioManager().setSendingHandler(guildManager.sendHandler);
                guild.getAudioManager().openAudioConnection(vc);

                AudioSourceManagers.registerRemoteSources(playerManager);

                final String commandString = command[1];

                playerManager.loadItem(command[2], new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        event.getChannel().sendMessage("Track loaded!").queue();
                        if(commandString.equalsIgnoreCase("play")) {
                            trackScheduler.playNow(track);
                        } else if (commandString.equalsIgnoreCase("queue")) {
                            trackScheduler.queue(track);
                        }
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        event.getChannel().sendMessage("Playlist loaded!").queue();

                        for (AudioTrack track : playlist.getTracks()) {
                            trackScheduler.queue(track);
                        }
                    }

                    @Override
                    public void noMatches() {
                        // Notify the user that we've got nothing
                        event.getChannel().sendMessage("No matches found!").queue();
                    }

                    @Override
                    public void loadFailed(FriendlyException throwable) {
                        // Notify the user that everything exploded
                        event.getChannel().sendMessage("Load failed!").queue();
                        System.out.println(throwable.getMessage());
                    }
                });
            }
        }
    }


    private GuildManager getMusicManager(Guild guild) {
        String guildId = guild.getId();
        GuildManager mng = musicManagers.get(guildId);
        if (mng == null) {
            synchronized (musicManagers) {
                mng = musicManagers.get(guildId);
                if (mng == null) {
                    mng = new GuildManager(playerManager);
                    mng.player.setVolume(DEFAULT_VOLUME);
                    musicManagers.put(guildId, mng);
                }
            }
        }
        return mng;
    }
}
