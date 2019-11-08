package com.github.alviannn.scheduler;

import com.github.alviannn.DiscordBot;
import lombok.Getter;

import java.util.concurrent.Future;

public class TaskScheduled {

    @Getter
    private final long id;
    @Getter
    private boolean cancelled;

    private Future<?> future;
    private Future<?> otherFuture;

    public TaskScheduled(long id, Future<?> future) {
        this.id = id;
        this.cancelled = false;
        this.future = future;
    }

    public TaskScheduled(long id, Future<?> future, Future<?> otherFuture) {
        this.id = id;
        this.cancelled = false;
        this.future = future;
        this.otherFuture = otherFuture;
    }

    /**
     * cancels the task
     */
    public void cancel() {
        if (!DiscordBot.scheduledTasks.containsKey(id))
            return;

        DiscordBot.scheduledTasks.remove(id);

        if (future != null) future.cancel(true);
        if (otherFuture != null) otherFuture.cancel(true);

        if (cancelled)
            throw new SchedulerException("Task ID of " + id + " is already cancelled!");

        cancelled = true;
    }

}
