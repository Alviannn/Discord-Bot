package com.github.alviannn.utils;

import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

@SuppressWarnings("unused")
@AllArgsConstructor
public final class DependencyHelper {

    private final ClassLoader classLoader;

    /**
     * constructs the dependency helper instance
     *
     * @param clazz the class
     */
    public DependencyHelper(Class<?> clazz) {
        this.classLoader = clazz.getClassLoader();
    }

    /**
     * handles dependency file downloading
     *
     * @param dependencies the dependencies
     * @param dirPath      the directory path
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void download(Map<String, String> dependencies, Path dirPath) throws IOException {
        File dir = dirPath.toFile();

        if (!dir.exists())
            dir.mkdirs();

        for (Map.Entry<String, String> entry : dependencies.entrySet()) {
            String fileName = entry.getKey();
            String fileUrl = entry.getValue();

            File file = new File(dir, fileName);
            if (file.exists())
                continue;

            try {
                URL url = new URL(fileUrl);
                try (InputStream is = url.openStream()) {
                    Files.copy(is, file.toPath());
                }
            } catch (Exception e) {
                throw new IOException("Failed to download " + fileName + "!");
            }
        }
    }

    /**
     * downloads a dependency file
     *
     * @param name    the file name
     * @param fileUrl the file url
     * @param dirPath the directory path
     * @throws Exception if the dependency file failed to download
     */
    public void download(String name, String fileUrl, Path dirPath) throws Exception {
        this.download(Collections.singletonMap(name, fileUrl), dirPath);
    }

    /**
     * loads the dependency file
     *
     * @param name    the name
     * @param fileUrl the file url
     * @param dirPath the directory path
     * @throws Exception if the dependency failed to load
     */
    public void load(String name, String fileUrl, Path dirPath) throws Exception {
        this.load(Collections.singletonMap(name, fileUrl), dirPath);
    }

    /**
     * handles dependencies loading
     *
     * @param dependencies the dependencies
     * @param dirPath      the directory path
     */
    public void load(Map<String, String> dependencies, Path dirPath) throws IOException {
        File dir = dirPath.toFile();

        if (!dir.exists())
            return;

        for (Map.Entry<String, String> entry : dependencies.entrySet()) {
            String fileName = entry.getKey();

            File file = new File(dir, fileName);
            if (!file.exists())
                throw new IOException("Cannot find file " + fileName + "!");

            ClassLoader previousLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);

            if (classLoader instanceof URLClassLoader) {
                URLClassLoader loader = (URLClassLoader) classLoader;
                try {
                    Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                    addURL.setAccessible(true);
                    addURL.invoke(loader, file.toURI().toURL());
                } catch (Exception e) {
                    Thread.currentThread().setContextClassLoader(previousLoader);
                    throw new IOException("Failed to load " + fileName + "!");
                }
            }
            else {
                Thread.currentThread().setContextClassLoader(previousLoader);
                throw new IOException("Failed to cast class loader!");
            }

            Thread.currentThread().setContextClassLoader(previousLoader);
        }


    }

}
