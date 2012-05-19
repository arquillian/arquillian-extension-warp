package org.jboss.arquillian.warp.extension.phaser.extension;

import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.warp.extension.phaser.Phase;
import org.jboss.arquillian.warp.spi.WarpLifecycleExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class PhaserExtension implements LoadableExtension, WarpLifecycleExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(WarpLifecycleExtension.class, this.getClass());
    }

    @Override
    public JavaArchive getEnrichmentLibrary() {
        return ShrinkWrap.create(JavaArchive.class, "warp-extension-phaser.jar").addPackage(Phase.class.getPackage())
                .addAsManifestResource("META-INF/warp-extensions/faces-config.xml", "faces-config.xml");
    }

    @Override
    public void enrichWebArchive(WebArchive webArchive) {
    }
}
