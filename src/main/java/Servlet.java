/**
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/servlet")
@SuppressWarnings("serial")
public class Servlet extends HttpServlet {
    
    private static final String ENRICHMENT = "X-Arq-Enrichment";
    private static final String ENRICHMENT_REQUEST = ENRICHMENT + "-Request";
    private static final String ENRICHMENT_RESPONSE = ENRICHMENT + "-Response";
    

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        PrintWriter out = resp.getWriter();
        out.write("hello there\n");
        for (Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            out.write(entry.getKey() + " = ");
            for (String value : entry.getValue()) {
                out.write(value);
                out.write(", ");
            }
            out.write("\n");
        }
        if (req.getParameter(ENRICHMENT_REQUEST) != null) {
            
            String requestEnrichment = req.getParameter(ENRICHMENT_REQUEST);
            
            String responseEnrichment = "null";
            
            if (!"null".equals(requestEnrichment)) {
                AssertionObject assertionObject = SerializationUtils.deserializeFromBase64(requestEnrichment);
                assertionObject.method();
                
                assertionObject.setPayload("client");
                responseEnrichment = SerializationUtils.serializeToBase64(assertionObject);
            }
            
            out.write(ENRICHMENT_RESPONSE + "=" + responseEnrichment);
        }
        out.close();
    }
}
