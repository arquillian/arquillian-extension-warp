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
package org.jboss.arquillian.warp.ftest;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
@WebServlet("/form")
public class FormServlet extends HttpServlet {
    /**
     * Eclipse requires a serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        PrintWriter out = resp.getWriter();

        writeStart(out);

        //TomEE does not replace "127.0.0.1:8080" with the Warp proxy URL, which makes "org.jboss.arquillian.warp.ftest.http.TestResponseContainsProxyUrl" fail.
        //But "localhost:8080" works.
        //With WildFly, it is vice versa.
        //So evaluate the server info:
        //WildFly 26: "WildFly Full 26.1.3.Final (WildFly Core 18.1.2.Final) - 2.2.19.Final"
        //TomEE 1.7.5: "Apache Tomcat (TomEE)/7.0.81 (1.7.5)"
        String serverInfo = this.getServletContext().getServerInfo();
        if (serverInfo.contains("TomEE") == true) {
            out.write("<form action=\"http://localhost:8080/test/form\" method=\"post\">\n");
        }
        else {
            out.write("<form action=\"http://127.0.0.1:8080/test/form\" method=\"post\">\n");
        }
        out.write("<input type=\"submit\" id=\"submit\" />\n");
        out.write("</form>\n");
        writeEnd(out);

        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        PrintWriter out = resp.getWriter();

        writeStart(out);
        out.write("congratulations\n");
        writeEnd(out);

        out.close();
    }

    private void writeStart(PrintWriter out) {
        out.write("<!DOCTYPE html>\n");
        out.write("<html>\n");
        out.write("<head>\n");
        out.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
        out.write("</head>\n");
        out.write("<body>\n");
    }

    private void writeEnd(PrintWriter out) {
        out.write("</body>\n");
        out.write("</html>\n");
    }
}
