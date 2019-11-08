package com.github.alviannn.events;

import com.github.alviannn.DiscordBot;
import com.github.alviannn.commands.base.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;

public class CommandListener extends ListenerAdapter {

    private final DiscordBot plugin;

    public CommandListener(DiscordBot plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        this.execute(e.getMember(), e.getMessage(), e.getChannel());
    }

    private void execute(Member member, Message message, TextChannel channel) {
        if (member == null || member.getUser().isBot())
            return;

        String content = message.getContentRaw();

        if (content.startsWith(plugin.commandPrefix)) {
            content = content.substring(plugin.commandPrefix.length());
            String[] contentArray = content.split(" ");

            if (content.isEmpty() || contentArray.length == 0) {
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle("Information")
                        .setDescription("Thank you for using Alvian DBot!")
                        .addField("Prefix", plugin.commandPrefix, false);

                DiscordBot.queueMessage(channel.getIdLong(), builder.build());
            }
            else {
                for (Command command : plugin.commandList) {
                    if (contentArray[0].equalsIgnoreCase(command.getName())) {

                        String[] args = Arrays.copyOfRange(contentArray, 1, contentArray.length);

                        command.registerChannel(channel);
                        command.execute(member, channel, args);
                        break;
                    }
                }

                for (Command command : plugin.commandList) {
                    for (String alias : command.getAliases()) {
                        if (alias.equalsIgnoreCase(contentArray[0])) {

                            String[] args = Arrays.copyOfRange(contentArray, 1, contentArray.length);

                            command.registerChannel(channel);
                            command.execute(member, channel, args);
                            break;
                        }
                    }
                }

            }
        }
    }

}
