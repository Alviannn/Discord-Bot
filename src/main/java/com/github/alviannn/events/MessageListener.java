package com.github.alviannn.events;

import com.github.alviannn.utils.Closer;
import com.github.alviannn.utils.Utils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        Message message = e.getMessage();

        String content = message.getContentRaw();
        String channel = e.getChannel().getName();
        String user = e.getAuthor().getName();

        if (e.getAuthor().isBot())
            return;

        try {
            this.storeMessage(user, channel, content);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * stores the messages to log
     *
     * @param user the user
     * @param channel the channel
     * @param message the message
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void storeMessage(String user, String channel, String message) throws IOException {
        File logsFile = new File("logs", Utils.getDateFormat("yyyy-MM-dd") + ".log");

        if (!logsFile.getParentFile().exists())
            logsFile.getParentFile().mkdirs();

        if (!logsFile.exists())
            logsFile.createNewFile();

        try (Closer closer = new Closer()) {
            PrintWriter writer = closer.register(new PrintWriter(new FileWriter(logsFile, true)));

            writer.println("[" + Utils.getDateFormat("HH:mm:ss") + "][#" + channel + "] " + user + ": " + message);
        }
    }

}
