/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
            if (file.startsWith("file:")) {
                file = file.substring(file.indexOf(":") + 1);
            }
            file = file.substring(0, file.indexOf('!'));

            JavaArchive jar =
                ShrinkWrap.create(ZipImporter.class, file + "!").importFrom(new File(file)).as(JavaArchive.class);

            return jar;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot get JAR from class " + clazz.getName() + " from URL " + url, e);
        }
    }
}
