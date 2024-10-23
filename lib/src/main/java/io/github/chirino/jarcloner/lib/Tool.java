package io.github.chirino.jarcloner.lib;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Tool {

    public static void create(String[] directories, String metaFile, String archiveFile) throws IOException {
        YAMLMapper yamlMapper = new YAMLMapper();
        Structure structure = yamlMapper.readValue(new File(metaFile), Structure.class);

        byte[] buffer = new byte[1024];

        try (FileOutputStream fos = new FileOutputStream(archiveFile);
             JarOutputStream jaros = new JarOutputStream(fos)) {
            jaros.setComment(structure.comment);
            for (Entry entry : structure.entries) {
                JarEntry je = new JarEntry(entry.name);
                je.setComment(entry.comment);
                je.setTime(entry.time);
                je.setExtra(entry.extra);
                je.setMethod(entry.method);
                je.setCrc(entry.crc);
                je.setSize(entry.size);
                jaros.putNextEntry(je);

                if (!je.isDirectory()) {

                    File file = null;
                    for (String dir : directories) {
                        File t = new File(dir, entry.name);
                        if (t.exists()) {
                            file = t;
                            break;
                        }
                    }
                    if (file == null) {
                        throw new FileNotFoundException("File not found: " + entry.name);
                    }

                    // we could check that the file size and crc match the expected values here...
                    try (FileInputStream fis = new FileInputStream(file)) {
                        // Read the file content and write it to the zip file
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            jaros.write(buffer, 0, length);
                        }
                    }
                }
                jaros.closeEntry();
            }
        }
    }

    public static void extract(String archiveFile, String metaFile, String outputDir) throws IOException {

        Path outputDirPath;
        if (outputDir != null) {
            // Create output directory if it doesn't exist
            outputDirPath = Paths.get(outputDir);
            if (Files.notExists(outputDirPath)) {
                Files.createDirectories(outputDirPath);
            }
        } else {
            outputDirPath = null;
        }

        // Read each entry of the jar file
        Structure structure = new Structure();

        try (JarFile archive = new JarFile(archiveFile)) {

            structure.comment = archive.getComment();
            archive.stream().forEach(entry -> {

                if (outputDir != null) {
                    Path entryOutputPath = outputDirPath.resolve(entry.getName());

                    try {
                        // If the entry is a directory, create the directory
                        if (entry.isDirectory()) {
                            Files.createDirectories(entryOutputPath);
                        } else {
                            // Ensure the parent directories for the entry exist
                            Files.createDirectories(entryOutputPath.getParent());

                            // Extract the file content
                            try (InputStream inputStream = archive.getInputStream(entry)) {
                                Files.copy(inputStream, entryOutputPath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }

                if (metaFile != null) {
                    Entry e = new Entry();
                    e.name = entry.getName();
                    e.comment = entry.getComment();
                    e.time = entry.getTime();
                    e.extra = entry.getExtra();
                    e.method = entry.getMethod();
                    e.crc = entry.getCrc();
                    e.size = entry.getSize();
                    structure.entries.add(e);
                }
            });

            if (metaFile != null) {
                YAMLMapper yamlMapper = new YAMLMapper();
                yamlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                yamlMapper.writeValue(new File(metaFile), structure);
            }

        }
    }

}

