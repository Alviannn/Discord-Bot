package com.github.alviannn;

import com.github.alviannn.commands.ClearCommand;
import com.github.alviannn.commands.InviteCommand;
import com.github.alviannn.commands.PrefixCommand;
import com.github.alviannn.commands.TaskCommand;
import com.github.alviannn.commands.base.Command;
import com.github.alviannn.configuration.Configuration;
import com.github.alviannn.events.CommandListener;
import com.github.alviannn.events.MessageListener;
import com.github.alviannn.scheduler.Scheduler;
import com.github.alviannn.scheduler.TaskScheduled;
import com.github.alviannn.utils.Closer;
import com.github.alviannn.utils.DependencyHelper;
import com.github.alviannn.utils.Logger;
import com.github.alviannn.utils.Utils;
import lombok.Getter;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.internal.JDAImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

@SuppressWarnings("WeakerAccess")
public class DiscordBot {

    @Getter
    private static DiscordBot instance;

    public ExecutorService asyncService;
    public ScheduledExecutorService scheduledService;
    public static Map<Long, TaskScheduled> scheduledTasks;

    public String discordToken;
    public long mainChannelId;
    public String commandPrefix;

    @Getter
    private final Logger logger;
    @Getter
    private Configuration config;

    private InputStream clientStream;
    public JDAImpl discord;

    public final List<Command> commandList;

    public DiscordBot() {
        instance = this;
        scheduledTasks = new HashMap<>();

        this.logger = new Logger("DiscordBot");
        this.commandList = new ArrayList<>();
    }

    /**
     * starts the discord bot
     */
    public void startBot() throws Exception {
        long start = System.currentTimeMillis();

        this.startExecutors();

        this.loadLibraries();
        this.loadConfig();

        JDABuilder builder = new JDABuilder(AccountType.BOT);

        builder.setToken(discordToken);
        builder.setActivity(Activity.watching("Nothing | " + commandPrefix + "prefix"));
        builder.setStatus(OnlineStatus.ONLINE);

        builder.addEventListeners(new MessageListener(), new CommandListener(this));
        this.registerCommand(
                new PrefixCommand(this), new InviteCommand(this), new ClearCommand(this),
                new TaskCommand()
        );

        discord = (JDAImpl) builder.build();
        clientStream = System.in;

        new Scheduler(TimeUnit.SECONDS) {
            @Override
            public void run() {
                try (Closer closer = new Closer()) {
                    Scanner scanner = closer.register(new Scanner(clientStream));

                    while (scanner.hasNext()) {
                        String rawMessage = scanner.nextLine();
                        String message = rawMessage.toLowerCase();

                        if (message.equals("!end") || message.equals("!shutdown") || message.equals("!stop")) {
                            stopBot();
                            break;
                        }
                        else if (message.equalsIgnoreCase("!totaltask")) {
                            logger.info("Total tasks #1: " + scheduledTasks.size());
                            continue;
                        }
                        else if (message.equalsIgnoreCase("!config")) {
                            Utils.print("-------------------------------------\n"
                                    + config.getJson().toString(4)
                                    + "\n-------------------------------------"
                            );
                            continue;
                        }

                        logger.info("Bot -> " + rawMessage);
                        queueMessage(mainChannelId, rawMessage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsync();

        logger.info("-------------------------------------");
        logger.info("Bot has been started! (" + (System.currentTimeMillis() - start) + " ms)");
        logger.info("-------------------------------------");
    }

    /**
     * stops the bot
     */
    public void stopBot() {
        if (discord == null)
            return;

        logger.warning("Shutting down bot...");

        new Scheduler() {
            @Override
            public void run() {

                long start = System.currentTimeMillis();
                try {
                    clientStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                discord.shutdownNow();

                discord = null;
                instance = null;

                logger.info("-------------------------------------");
                logger.info("Bot has been stopped! (" + (System.currentTimeMillis() - start) + " ms)");
                logger.info("-------------------------------------");

                System.exit(0);
            }
        }.runTaskLater(5L);
    }

    /**
     * sends message
     *
     * @param channelId the channel id
     * @param message   the message
     */
    @SuppressWarnings("ConstantConditions")
    public static void queueMessage(long channelId, Object message) {
        JDAImpl discord = DiscordBot.getInstance().discord;

        if (message instanceof MessageEmbed)
            discord.getTextChannelById(channelId).sendMessage((MessageEmbed) message).queue();
        else if (message instanceof EmbedBuilder)
            discord.getTextChannelById(channelId).sendMessage(((EmbedBuilder) message).build()).queue();
        else if (message instanceof Message)
            discord.getTextChannelById(channelId).sendMessage((Message) message).queue();
        else if (message instanceof CharSequence || message instanceof String)
            discord.getTextChannelById(channelId).sendMessage((CharSequence) message).queue();
        else
            discord.getTextChannelById(channelId).sendMessage(message.toString()).queue();
    }

    /**
     * sends message
     *
     * @param channelId the channel id
     * @param message   the message
     */
    @SuppressWarnings("ConstantConditions")
    public static CompletableFuture<Message> submitMessage(long channelId, Object message) {
        JDAImpl discord = DiscordBot.getInstance().discord;

        if (message instanceof MessageEmbed)
            return discord.getTextChannelById(channelId).sendMessage((MessageEmbed) message).submit();
        else if (message instanceof EmbedBuilder)
            return discord.getTextChannelById(channelId).sendMessage(((EmbedBuilder) message).build()).submit();
        else if (message instanceof Message)
            return discord.getTextChannelById(channelId).sendMessage((Message) message).submit();
        else if (message instanceof CharSequence || message instanceof String)
            return discord.getTextChannelById(channelId).sendMessage((CharSequence) message).submit();
        else
            return discord.getTextChannelById(channelId).sendMessage(message.toString()).submit();
    }

    /**
     * sends message
     *
     * @param channel the channel
     * @param message the message
     */
    public static void queueMessage(TextChannel channel, Object message) {
        queueMessage(channel.getIdLong(), message);
    }

    /**
     * sends message
     *
     * @param channel the channel
     * @param message the message
     */
    public static CompletableFuture<Message> submitMessage(TextChannel channel, Object message) {
        return submitMessage(channel.getIdLong(), message);
    }

    /**
     * registers all commands
     *
     * @param commands the commands
     */
    private void registerCommand(Command... commands) {
        commandList.addAll(Arrays.asList(commands));
    }

    /**
     * loads the config file
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadConfig() throws IOException {
        File dir = new File("config");
        File file = new File(dir, "config.json");

        if (!dir.exists())
            dir.mkdirs();

        if (!file.exists()) {
            try (Closer closer = new Closer()) {
                InputStream stream = closer.register(this.getResourceAsStream("config.json"));

                Files.copy(stream, file.toPath());
            }
        }

        config = new Configuration(file);

        discordToken = config.getString("bot-token");
        mainChannelId = config.getLong("main-channel-id");
        commandPrefix = config.getString("command-prefix");
    }

    /**
     * loads the libraries
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadLibraries() throws Exception {
        DependencyHelper helper = new DependencyHelper(this.getClass());

        File libDir = new File("libraries");
        if (!libDir.exists())
            libDir.mkdirs();

        Map<String, String> libraries = new HashMap<>();

        libraries.put("json-20190722.jar", "https://repo1.maven.org/maven2/org/json/json/20190722/json-20190722.jar");
        libraries.put("slf4j-api-1.7.29.jar", "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.29/slf4j-api-1.7.29.jar");
        libraries.put("slf4j-nop-1.7.29.jar", "https://repo1.maven.org/maven2/org/slf4j/slf4j-nop/1.7.29/slf4j-nop-1.7.29.jar");

        helper.download(libraries, libDir.toPath());
        helper.load(libraries, libDir.toPath());
    }

    /**
     * gets a resource stream
     *
     * @param name the name
     * @return the resource stream
     */
    public InputStream getResourceAsStream(String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }

    /**
     * shuts down the executor services
     */
    public void shutdownExecutors() {
        asyncService.shutdownNow();
        scheduledService.shutdownNow();

        asyncService = null;
        scheduledService = null;
    }

    /**
     * starts the executor services
     */
    public void startExecutors() {
        if (asyncService == null)
            asyncService = Executors.newFixedThreadPool(100);
        if (scheduledService == null)
            scheduledService = Executors.newSingleThreadScheduledExecutor();
    }

}
