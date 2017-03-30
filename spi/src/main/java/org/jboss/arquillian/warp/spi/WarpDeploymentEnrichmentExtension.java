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
package org.jboss.arquillian.warp.spi;

import org.jboss.arquillian.warp.WarpTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * <p>
 * Service for adding {@link JavaArchive}s to final {@link WebArchive} and modifying final {@link WebArchive} in order to
 * provide extended Warp functionality.
 * </p>
 * <p>
 * <p>
 * This service will be applied only if test case is annotated with {@link WarpTest}.
 * </p>
 *
 * @author Lukas Fryc
 */
public interface WarpDeploymentEnrichmentExtension {

    /**
     * <p>Adds the library to enrich final {@link WebArchive} deployment.</p>
     * <p>
     * <p>Can return null when no enrichment library is provided by extension.</p>
     */
    JavaArchive getEnrichmentLibrary();

    /**
     * <p>llows enriching of final {@link WebArchive} (any resource in an archive can be manipulated).</p>
     * <p>
     * <p>Doesn't need to do any operation on archive.</p>
     */
    void enrichWebArchive(WebArchive webArchive);
}
