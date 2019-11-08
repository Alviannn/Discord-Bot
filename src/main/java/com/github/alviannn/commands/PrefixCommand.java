package com.github.alviannn.commands;

import com.github.alviannn.DiscordBot;
import com.github.alviannn.commands.base.Command;
import com.github.alviannn.scheduler.Scheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.internal.managers.PresenceImpl;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class PrefixCommand extends Command {

    private final DiscordBot plugin;

    public PrefixCommand(DiscordBot plugin) {
        super("prefix");
        this.plugin = plugin;
    }

    @Override
    public void execute(Member member, TextChannel channel, String[] args) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
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
            EmbedBuilder builder = new EmbedBuilder();
            builder.setDescription("The bot prefix on this server is `" + plugin.commandPrefix + "`!");
            this.queueMessage(builder.build());
        }
        else {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setDescription("The bot prefix has been changed to `" + args[0] + "`");

            PresenceImpl presence = (PresenceImpl) plugin.discord.getPresence();
            Activity activity = Activity.watching("Nothing | " + plugin.commandPrefix + "prefix");

            presence.setCacheActivity(activity);
            presence.setActivity(activity);
            presence.setStatus(presence.getStatus());

            this.queueMessage(builder.build());

            plugin.getConfig().set("command-prefix", args[0]);
            try {
                plugin.getConfig().saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
