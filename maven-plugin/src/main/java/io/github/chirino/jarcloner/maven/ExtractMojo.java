package io.github.chirino.jarcloner.maven;

import java.io.File;

import io.github.chirino.jarcloner.lib.Tool;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * A mojo to extract the jar structure yaml
 */
@Mojo(name = "extract")
public class ExtractMojo extends AbstractMojo {

    @Parameter(property = "jar-cloner.structure-yaml", defaultValue = "${basedir}/src/main/jar-cloner.yaml", required = true)
    private File yamlFile;

    @Parameter(property = "jar-cloner.jar", required = true)
    private File archiveFile;

    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("creating jar structure from: " + archiveFile + ", to: " + yamlFile);
        try {
            yamlFile.getParentFile().mkdirs();
            Tool.extract(archiveFile.getAbsolutePath(), yamlFile.getAbsolutePath(), null);
        } catch (Exception e) {
            throw new MojoExecutionException("Error creating yaml file", e);
        }
    }
}
