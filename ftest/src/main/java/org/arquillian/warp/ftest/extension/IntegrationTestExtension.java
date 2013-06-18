package org.arquillian.warp.ftest.extension;

import org.arquillian.warp.ftest.configuration.IntegrationTestConfiguratorObserver;
import org.arquillian.warp.ftest.installation.ContainerInitializationObserver;
import org.arquillian.warp.ftest.installation.ContainerInstaller;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class IntegrationTestExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(IntegrationTestConfiguratorObserver.class);
        builder.observer(ContainerInitializationObserver.class);
        builder.observer(ContainerInstaller.class);
    }
}
