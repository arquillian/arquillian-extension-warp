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
package org.jboss.arquillian.warp.impl.server.inspection;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.warp.Inspection;

/**
 * The registry for registered inspections registered for current requests.
 *
 * @author Lukas Fryc
 */
public class InspectionRegistry {

    private Set<Inspection> inspections = new LinkedHashSet<Inspection>(1);

    public void registerInspections(Inspection... inspections) {
        registerInspections(Arrays.asList(inspections));
    }

    public void registerInspections(Collection<Inspection> inspections) {
        validateNotNull(inspections);
        this.inspections.addAll(inspections);
    }

    public void unregisterInspections(Inspection... inspections) {
        unregisterInspections(Arrays.asList(inspections));
    }

    public void unregisterInspections(Collection<Inspection> inspections) {
        validateNotNull(inspections);
        this.inspections.removeAll(inspections);
    }

    public Collection<Inspection> getInspections() {
        return this.inspections;
    }

    private void validateNotNull(Collection<Inspection> inspections) {
        for (Inspection inspection : inspections) {
            Validate.notNull(inspection, "inspection must not be null");
        }
    }
}
