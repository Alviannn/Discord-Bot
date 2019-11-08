package com.github.alviannn.main;

import com.github.alviannn.DiscordBot;

public class DiscordBotMain {

    public static void main(String[] args) throws Exception {
        DiscordBot discordBot = new DiscordBot();
        discordBot.startBot();

        Runtime.getRuntime().addShutdownHook(new Thread(discordBot::stopBot));
    }

}
