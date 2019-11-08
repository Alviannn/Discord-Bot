package com.github.alviannn.commands;

import com.github.alviannn.commands.base.Command;
import com.github.alviannn.scheduler.Scheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class TaskCommand extends Command {

    public TaskCommand() {
        super("test");
    }

    @Override
    public void execute(Member member, TextChannel channel, String[] args) {
        if (!member.isOwner()) {
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
    }

}
