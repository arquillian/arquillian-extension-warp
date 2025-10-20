/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.arquillian.warp.ftest.installation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.arquillian.warp.ftest.configuration.IntegrationTestConfiguration;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * Installs the container distribution and configuration before it will be started
 */
public class ContainerInstaller {

    private Logger log = Logger.getLogger(ContainerInstaller.class.getName());

    @Inject
    private Instance<IntegrationTestConfiguration> configuration;

    public void unpackContainerDistribution(@Observes InstallContainer event) {
        Validate.notNull(configuration, "fundamental test configuration is not setup");

        String distribution = configuration.get().getContainerDistribution();

        if (distribution == null || distribution.isEmpty()) {
            return;
        }

        File containerHome = new File(configuration.get().getContainerHome());
        Validate.notNull(containerHome, "container home must be set");

        if (containerHome.exists()) {
            log.info(String.format("The container is already installed in '%s'", containerHome));
            return;
        }

        File unpackDestination = containerHome.getParentFile();
        InputStream inputStream;

        try {
            URL distributionUrl = new URL(distribution);
            log.info(String.format("The container distribution will be resolved from URL '%s'", distribution));
            inputStream = distributionUrl.openStream();
        } catch (MalformedURLException e) {
            log.info(String.format("The container distribution will be resolved from Maven artifact '%s'", distribution));
            inputStream = Maven.resolver().resolve(distribution).withoutTransitivity().asSingleInputStream();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to resolve the container distribution", e);
        }

        unzip(inputStream, unpackDestination, false);

        log.info(String.format("The container distribution '%s' was installed into '%s'", distribution,
            unpackDestination.getAbsolutePath()));

        // If we are running on a linux environment, maybe a launcher script that is required by the arquillian container plugin must be executable.
        String launcherScript = configuration.get().getContainerLinuxExecutePermissionFile();
        if (launcherScript != null && launcherScript.trim().length() > 0) {
            // Do this only on non-Windows-OS.
            if (!System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                File fileAsadmin = new File(unpackDestination.getAbsolutePath(), launcherScript);
                if (fileAsadmin.exists()) {
                    log.info(String.format("Preparing container: setting execute permisson for '%s'.", fileAsadmin.getAbsoluteFile()));
                    fileAsadmin.setExecutable(true);
                }
                else {
                    log.warning(String.format("Wrong configuration: Could not find file '%s'.", fileAsadmin.getAbsoluteFile()));
                }
            }
            else {
              log.info("Preparing container: No linux environment, skipping step.");
            }
        }

        if (!containerHome.exists()) {
            throw new IllegalStateException(String.format(
                "The container distribution was unpacked but the containerHome (%s) still doesn't exist", containerHome));
        }

        configuration.get().setContainerInstalledFromDistribution(true);
    }

    /*
     * Will unpack the maven artifact declared in the build\ftest-base\pom.xml property "arquillian.container.configuration".
     * Probably only valid for managed containers, but not for remote containers.
     */
    public void unpackContainerConfigurationFiles(@Observes ConfigureContainer event) {
        Validate.notNull(configuration, "fundamental test configuration is not setup");

        String configurationFiles = configuration.get().getContainerConfiguration();

        if (configurationFiles == null || configurationFiles.isEmpty()) {
            return;
        }

        Validate.notNull(configuration.get().getContainerHome(), "container home must be set");
        File containerHome = new File(configuration.get().getContainerHome());

        InputStream artifactStream = Maven.configureResolver().withClassPathResolution(false).resolve(configurationFiles)
            .withoutTransitivity().asSingleInputStream();
        unzip(artifactStream, containerHome, true);

        log.info(String.format("The container configuration '%s' was unpacked into '%s'", configurationFiles,
            containerHome.getAbsolutePath()));
    }

    public void uninstallContainer(@Observes UninstallContainer event) {
        Validate.notNull(configuration, "fundamental test configuration is not setup");

        if (configuration.get().containerShouldBeUninstalled()) {
            File containerHome = new File(configuration.get().getContainerHome());

            log.info(String.format("The container will be uninstalled from '%s'", containerHome.getAbsolutePath()));

            if (containerHome.exists()) {
                if (FileUtils.deleteQuietly(containerHome) == false) {
                    log.severe (String.format("could not delete container from '%s'", containerHome.getAbsolutePath()));
                }
            }
        }
    }

    private void unzip(InputStream inputStream, File destination, boolean overwrite) {
        try {
            byte[] buf = new byte[1024];
            ZipInputStream zipinputstream = null;
            ZipEntry zipentry;
            zipinputstream = new ZipInputStream(inputStream);

            zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) {
                int n;
                FileOutputStream fileoutputstream;
                File newFile = new File(destination, zipentry.getName());
                if (zipentry.isDirectory()) {
                    newFile.mkdirs();
                    zipentry = zipinputstream.getNextEntry();
                    continue;
                }

                //Create parent directory if it does not exist. This happens when expanding "apache-tomee-1.7.5-webprofile.zip".
                if (newFile.getParentFile().exists() == false) {
                  newFile.getParentFile().mkdir();
                }

                if (newFile.exists() && overwrite) {
                    log.info("Overwriting " + newFile);
                    newFile.delete();
                }

                fileoutputstream = new FileOutputStream(newFile);

                while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                    fileoutputstream.write(buf, 0, n);
                }

                fileoutputstream.close();
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();
            }

            zipinputstream.close();
        } catch (Exception e) {
            throw new IllegalStateException("Can't unzip input stream", e);
        }
    }
}