package com.github.alviannn.utils;

import lombok.AllArgsConstructor;

@SuppressWarnings("unused")
@AllArgsConstructor
public final class Logger {

    private final String name;

    public void log(String message) {
        Utils.print("[" + name + "]: " + message);
    }

    public void info(String message) {
        Utils.print("[" + name + " - INFO]: " + message);
    }

    public void severe(String message) {
        Utils.print("[" + name + " - SEVERE]: " + message);
    }

    public void warning(String message) {
        Utils.print("[" + name + " - WARNING]: " + message);
    }

}
