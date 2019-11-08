package com.github.alviannn.commands;

import com.github.alviannn.DiscordBot;
import com.github.alviannn.commands.base.Command;
import com.github.alviannn.scheduler.Scheduler;
import com.github.alviannn.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClearCommand extends Command {

    private final DiscordBot plugin;

    public ClearCommand(DiscordBot plugin) {
        super("clear", "clean");
        this.plugin = plugin;
    }

    @Override
    public void execute(Member member, TextChannel channel, String[] args) {
        if (!member.hasPermission(Permission.MESSAGE_MANAGE)) {
            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("🔴 An error has occurred!")
                    .setColor(Color.RED)
                    .setDescription("No permission!");

            CompletableFuture<Message> future = this.submitMessage(builder.build());

            new Scheduler() {
                @Override
                public void run() {
                    try {
                        future.get().delete().queue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskLater(3L);

            return;
        }
        if (args.length == 0) {
            this.queueMessage("Usage: " + plugin.commandPrefix + "clear <total messages>");
        }
        else {
            if (!Utils.isNumeric(args[0], Utils.NumericType.INTEGER)) {
                this.queueMessage("The first argument must be number/integer!");
                return;
            }

            int totalMessages = Integer.parseInt(args[0]);

            try {
                List<Message> messages = channel.getHistory().retrievePast(totalMessages).complete();
                channel.deleteMessages(messages).queue();
            } catch (Exception e) {
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle("🔴 An error has occurred!")
                        .setColor(Color.RED)
                        .setDescription(e.getMessage());

                this.queueMessage(builder.build());

                return;
            }

            this.queueMessage("Total messages cleared: " + totalMessages);
        }
    }

}
