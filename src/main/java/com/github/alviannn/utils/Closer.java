package com.github.alviannn.utils;

import lombok.Getter;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Closer implements AutoCloseable {

    @Getter
    private final List<AutoCloseable> closeableList;

    /**
     * constructs the closer instance
     */
    public Closer() {
        this.closeableList = new ArrayList<>();
    }

    /**
     * registers a closeable instance
     *
     * @param closeable the closeable instance
     */
    public <T extends AutoCloseable> T register(T closeable) {
        if (closeable == null)
            throw new NullPointerException("Closeable cannot be null!");

        closeableList.add(closeable);

        return closeable;
    }

    /**
     * closes all closeables
     */
    @Override
    public void close() {
        Iterator<AutoCloseable> iterator = closeableList.iterator();

        while (iterator.hasNext()) {
            AutoCloseable next = iterator.next();

            try {
                if (next instanceof OutputStream)
                    ((OutputStream) next).flush();

                next.close();
            } catch (Exception ignored) {
            }

            iterator.remove();
        }
    }

}
