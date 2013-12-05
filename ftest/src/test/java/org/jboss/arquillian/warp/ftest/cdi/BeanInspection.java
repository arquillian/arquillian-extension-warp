package org.jboss.arquillian.warp.ftest.cdi;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.servlet.BeforeServlet;

public class BeanInspection extends Inspection {
        private static final long serialVersionUID = 1L;

        @Inject
        private Bean bean;

        @BeforeServlet
        public void setupExecute() {
            assertEquals("value", bean.getValue());
        }
    }