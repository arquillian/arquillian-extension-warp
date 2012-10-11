package org.jboss.arquillian.warp.impl.utils;

import java.io.File;
import java.net.URL;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public final class ShrinkWrapUtils {

    public static JavaArchive getJavaArchiveFromClass(Class<?> clazz) {
        URL url = clazz.getResource(clazz.getSimpleName() + ".class");

        try {

            // get a absolute file system path
            String file = url.getFile();
            file = file.substring(file.indexOf(":") + 1);
            file = file.substring(0, file.indexOf('!'));

            JavaArchive jar = ShrinkWrap.create(ZipImporter.class, file + "!").importFrom(new File(file)).as(JavaArchive.class);

            return jar;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot get JAR from class " + clazz.getName() + " from URL " + url, e);
        }
    }
}
