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

    /**
     * sets the value on config path
     *
     * @param path the path
     * @param value the value
     */
    public void set(String path, Object value) {
        json.put(path, value);
    }

    /**
     * saves the config
     */
    public void saveConfig() throws IOException {
        try (Closer closer = new Closer()) {
            FileWriter writer = closer.register(new FileWriter(configFile));

            json.write(writer, 4, 0);
        }
    }

    /**
     * @param path the path
     * @param def the default value
     * @return the config value
     */
    public Object get(String path, Object def) {
        if (json.has(path)) return json.get(path);

        return def;
    }

    /**
     * @return the config value
     */
    public Object get(String path) {
        return this.get(path, null);
    }

    /**
     * @return the config string value
     */
    public String getString(String path) {
        return json.getString(path);
    }

    /**
     * @return the config integer value
     */
    public int getInt(String path) {
        return json.getInt(path);
    }

    /**
     * @return the config long value
     */
    public long getLong(String path) {
        return json.getLong(path);
    }

    /**
     * @return the config double value
     */
    public double getDouble(String path) {
        return json.getDouble(path);
    }

    /**
     * @return the config float value
     */
    public float getFloat(String path) {
        return json.getFloat(path);
    }

    /**
     * @return the config value
     */
    public boolean getBoolean(String path) {
        return json.getBoolean(path);
    }

    /**
     * @return the config json object value
     */
    public JSONObject getJsonObject(String path) {
        return json.getJSONObject(path);
    }

    /**
     * @return the config json array value
     */
    public JSONArray getJsonArray(String path) {
        return json.getJSONArray(path);
    }

}
