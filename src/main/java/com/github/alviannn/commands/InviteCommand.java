package com.github.alviannn.commands;

import com.github.alviannn.DiscordBot;
import com.github.alviannn.commands.base.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class InviteCommand extends Command {

    private final DiscordBot plugin;

    public InviteCommand(DiscordBot plugin) {
        super("invite");
        this.plugin = plugin;
    }

    @Override
    public void execute(Member member, TextChannel channel, String[] args) {
        Invite invite = channel.createInvite()
                .setMaxUses(10)
                .setMaxAge(1L, TimeUnit.HOURS)
                .complete();

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Invitation link")
                .addField("Expires", "1 hour", false)
                .addField("Users limit", "10", false)
                .addField("Invitation link", invite.getUrl(), false)
                .setThumbnail(plugin.discord.getSelfUser().getAvatarUrl());

        this.queueMessage(builder.build());
    }

}
