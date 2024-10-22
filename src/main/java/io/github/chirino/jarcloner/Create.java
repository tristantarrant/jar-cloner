package io.github.chirino.jarcloner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

@CommandLine.Command(name = "create", description = "creates the zip file with the structure of the meta file")
public class Create implements Runnable {

    @CommandLine.Option(names = {"-C", "--dir"}, description = "directory to change to before creating the archive", defaultValue = ".")
    private String directory;

    @CommandLine.Parameters(description = "the yaml file that holds the structure information")
    private String metaFile;

    @CommandLine.Parameters(description = "the zip/jar file to create")
    private String archiveFile;


    @Override
    public void run() {

        System.out.println("creating zip/jar structure from: " + metaFile + ", to: " + archiveFile);

        Structure structure;
        YAMLMapper yamlMapper = new YAMLMapper();
        try {
            structure = yamlMapper.readValue(new File(metaFile), Structure.class);
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            System.exit(2);
            return;
        }

        File dir = new File(directory);

        byte[] buffer = new byte[1024];

        try (FileOutputStream fos = new FileOutputStream(archiveFile);
            JarOutputStream jaros = new JarOutputStream(fos)) {
            jaros.setComment(structure.comment);
            for (Entry entry : structure.entries) {
                System.out.println("adding: " + entry.name);

                JarEntry je = new JarEntry(entry.name);
                je.setComment(entry.comment);
                je.setTime(entry.time);
                je.setExtra(entry.extra);
                je.setMethod(entry.method);
                je.setCrc(entry.crc);
                je.setSize(entry.size);
                jaros.putNextEntry(je);

                if (!je.isDirectory()) {
                    File file = new File(dir, entry.name);
                    // we could check that the file size and crc match the expected values here...
                    try (FileInputStream fis = new FileInputStream(file)) {
                        // Read the file content and write it to the zip file
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            jaros.write(buffer, 0, length);
                        }
                    }
                }

                // Close the zip entry
                jaros.closeEntry();
            }

        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            System.exit(2);
        }


    }

    private Long toLong(FileTime creationTime) {
        if (creationTime != null) {
            return creationTime.toMillis();
        }
        return null;
    }
}

