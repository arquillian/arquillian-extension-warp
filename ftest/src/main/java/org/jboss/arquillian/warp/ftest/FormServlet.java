/**
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
    public static final String VALUE = "warp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        PrintWriter out = resp.getWriter();

        writeStart(out);
        out.write("<form action=\"http://127.0.0.1:8080/test/form\" method=\"post\">\n");
        out.write("<input type=\"text\" id=\"data\" value=\"" + VALUE + "\" />\n");
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
        out.write("form input set to:" + req.getParameter("data"));
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
