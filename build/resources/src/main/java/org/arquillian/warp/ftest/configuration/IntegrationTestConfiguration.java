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
package org.arquillian.warp.ftest.configuration;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.configuration.ConfigurationMapper;
import org.jboss.arquillian.drone.spi.DroneConfiguration;

public class IntegrationTestConfiguration implements DroneConfiguration<IntegrationTestConfiguration> {

    private String containerHome;
    private String containerDistribution;
    private String containerConfiguration;
    private String containerLinuxExecutePermissionFile;
    private Boolean containerUninstall;
    private Boolean debug;

    private boolean containerInstalledFromDistribution = false;

    /**
     * Get the Maven dependency (GAV) for the container distribution artifact
     */
    public String getContainerDistribution() {
        return containerDistribution;
    }

    /**
     * Get the Maven dependency (GAV) for the artifact which contains a container configuration files
     */
    public String getContainerConfiguration() {
        return containerConfiguration;
    }

    /**
     * Get the directory in which the unpacked container distribution will be placed
     */
    public String getContainerHome() {
        return containerHome;
    }

    /**
     * If the arquillian container adapter calls a launcher script for starting the container, this file must have the "execute" permisson on linux environments.
     * This property must contain the relative path (subdir of "containerConfiguration").
     *
     * @return Relative path to the file that should be marked as "executable" if the test environment is not Windows.
     */
    public String getContainerLinuxExecutePermissionFile() {
        return containerLinuxExecutePermissionFile;
    }

    /**
     * Set the flag that the container was installed from distribution
     */
    public void setContainerInstalledFromDistribution(boolean containerInstalledFromDistribution) {
        this.containerInstalledFromDistribution = containerInstalledFromDistribution;
    }

    /**
     * Returns true if the container should be uninstalled after suite (default: true)
     */
    public boolean containerShouldBeUninstalled() {
        return containerInstalledFromDistribution && (containerUninstall == null || containerUninstall);
    }

    public boolean isDebug() {
        return debug != null && debug;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.DroneConfiguration#getConfigurationName()
     */
    @Override
    public String getConfigurationName() {
        return "suite";
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.arquillian.drone.spi.DroneConfiguration#configure(org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor
     * , java.lang.Class)
     */
    @Override
    public IntegrationTestConfiguration configure(ArquillianDescriptor descriptor,
        Class<? extends Annotation> qualifier) {
        return ConfigurationMapper.fromArquillianDescriptor(descriptor, this, qualifier);
    }
}
