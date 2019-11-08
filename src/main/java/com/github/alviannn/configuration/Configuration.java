package com.github.alviannn.configuration;

import com.github.alviannn.utils.Closer;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;

@Getter
public class Configuration {

    private final File configFile;
    private JSONObject json;

    public Configuration(String path) {
        this(new File(path));
    }

    public Configuration(File configFile) {
        if (!configFile.getName().endsWith(".json"))
            throw new RuntimeException("Config file must be a JSON file!");

        this.configFile = configFile;

        try (Closer closer = new Closer()) {
            FileInputStream stream = closer.register(new FileInputStream(configFile));

            JSONTokener tokener = new JSONTokener(stream);
            this.json = new JSONObject(tokener);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void set(String path, Object value) {
        json.put(path, value);
    }

    public void saveConfig() throws IOException {
        try (Closer closer = new Closer()) {
            FileWriter writer = closer.register(new FileWriter(configFile));

            json.write(writer, 4, 0);
        }
    }

    public Object get(String path, Object def) {
        if (json.has(path)) return json.get(path);

        return def;
    }

    public Object get(String path) {
        return this.get(path, null);
    }

    public String getString(String path) {
        return json.getString(path);
    }

    public int getInt(String path) {
        return json.getInt(path);
    }

    public long getLong(String path) {
        return json.getLong(path);
    }

    public double getDouble(String path) {
        return json.getDouble(path);
    }

    public float getFloat(String path) {
        return json.getFloat(path);
    }

    public boolean getBoolean(String path) {
        return json.getBoolean(path);
    }

    public JSONObject getJsonObject(String path) {
        return json.getJSONObject(path);
    }

    public JSONArray getJsonArray(String path) {
        return json.getJSONArray(path);
    }

}
