package io.github.chirino.jarcloner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import picocli.CommandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@CommandLine.Command(name = "extract", description = "Extracts the structure of a zip/jar file")
public class Extract implements Runnable {

    @CommandLine.Parameters(description = "the zip/jar file to extract the structure from")
    private String archiveFile;

    @CommandLine.Parameters(description = "the yaml file to hold the structure information")
    private String metaFile;

    @Override
    public void run() {

        System.out.println("extracting structure from: " + archiveFile + ", to: " + metaFile);

        // Read each entry of the jar file
        Structure structure = new Structure();

        try (JarFile archive = new JarFile(archiveFile)) {

            structure.comment = archive.getComment();
            archive.stream().forEach(entry -> {
                Entry e = new Entry();
                e.name = entry.getName();
                e.comment = entry.getComment();
                e.time = entry.getTime();
                e.extra = entry.getExtra();
                e.method = entry.getMethod();
                e.crc = entry.getCrc();
                e.size = entry.getSize();
                structure.entries.add(e);
            });


            YAMLMapper yamlMapper = new YAMLMapper();
            yamlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String yaml = null;

            yamlMapper.writeValue(new File(metaFile), structure);
            System.out.println("wrote: " + metaFile);

        } catch (Throwable e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            System.exit(2);
        }
    }
}

