package com.github.alviannn.scheduler;

import com.github.alviannn.DiscordBot;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class Scheduler implements Runnable {

    private final DiscordBot plugin;

    // ------------------------ Constructor ------------------------ //

    private final Runnable runnable;
    private final TimeUnit timeUnit;

    public Scheduler(TimeUnit timeUnit) {
        this.plugin = DiscordBot.getInstance();
        this.runnable = this;
        this.timeUnit = timeUnit;
    }

    public Scheduler() {
        this(TimeUnit.SECONDS);
    }

    // ------------------------ Handler ------------------------ //

    public synchronized TaskScheduled runTask() {
        Future<?> future = plugin.scheduledService.submit(runnable);
        TaskScheduled task = this.createNewTask(future);

        new Thread(() -> {
            future.cancel(true);

            if (!task.isCancelled())
                task.cancel();
        }).start();

        return task;
    }

    public synchronized TaskScheduled runTaskAsync() {
        Future<?> future = plugin.asyncService.submit(runnable);

        return this.createNewTask(future);
    }

    public synchronized TaskScheduled runTaskTimer(long delay, long period) {
        Future<?> future = plugin.scheduledService.scheduleAtFixedRate(runnable, delay, period, timeUnit);

        return this.createNewTask(future);
    }

    public synchronized TaskScheduled runTaskTimerAsync(long delay, long period) {
        Future<?> future = plugin.asyncService.submit(() -> plugin.scheduledService.scheduleAtFixedRate(runnable, delay, period, timeUnit));
        TaskScheduled task = this.createNewTask(future);

        new Thread(() -> {
            future.cancel(true);

            if (!task.isCancelled())
                task.cancel();
        }).start();

        return task;
    }

    public synchronized TaskScheduled runTaskLater(long delay) {
        Future<?> future = plugin.scheduledService.schedule(runnable, delay, timeUnit);
        TaskScheduled task = this.createNewTask(future);

        new Thread(() -> {
            long millis = System.currentTimeMillis();
            long duration = timeUnit.toMillis(delay);

            while (true) {
                if ((System.currentTimeMillis() - millis) >= duration) {
                    if (task.isCancelled()) task.cancel();
                    break;
                }
            }

        }).start();

        return task;
    }

    public synchronized TaskScheduled runTaskLaterAsync(long delay) {
        Future<?> future = plugin.asyncService.submit(() -> plugin.scheduledService.schedule(runnable, delay, timeUnit));
        TaskScheduled task = this.createNewTask(future);

        new Thread(() -> {
            long millis = System.currentTimeMillis();
            long duration = timeUnit.toMillis(delay);

            while (true) {
                if ((System.currentTimeMillis() - millis) >= duration) {
                    if (task.isCancelled()) task.cancel();
                    break;
                }
            }

        }).start();

        return task;
    }

    // ------------------------ Manager ------------------------ //

    public void cancelAllTask() {
        for (TaskScheduled task : DiscordBot.scheduledTasks.values()) {
            if (!task.isCancelled()) task.cancel();
        }

        plugin.shutdownExecutors();
        plugin.startExecutors();
    }

    public void cancelTask(long id) {
        try {
            DiscordBot.scheduledTasks.get(id).cancel();
        } catch (Exception ignored) {
            throw new SchedulerException("Failed to cancel task ID of " + id);
        }
    }

    private TaskScheduled createNewTask(Future<?> future) {
        long id = this.createNewId();

        TaskScheduled task = new TaskScheduled(id, future);
        DiscordBot.scheduledTasks.put(id, task);

        return task;
    }

    private long createNewId() {
        long id = 0L;

        while (DiscordBot.scheduledTasks.containsKey(id))
            id++;

        return id;
    }

}
