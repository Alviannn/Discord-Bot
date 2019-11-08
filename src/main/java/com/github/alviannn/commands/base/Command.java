package com.github.alviannn.commands.base;

import com.github.alviannn.DiscordBot;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.CompletableFuture;

@Getter
public abstract class Command {

    private final String name;
    private final String[] aliases;
    private TextChannel channel;

    public Command(String name, String... aliases) {
        this.name = name;

        for (int i = 0; i < aliases.length; i++)
            aliases[i] = aliases[i].toLowerCase();

        this.aliases = aliases;
    }

    public abstract void execute(Member member, TextChannel channel, String[] args);

    public void queueMessage(Object message) {
        DiscordBot.queueMessage(channel, message);
    }

    public CompletableFuture<Message> submitMessage(Object message) {
        return DiscordBot.submitMessage(channel, message);
    }

    public void registerChannel(TextChannel channel) {
        this.channel = channel;
    }

}
