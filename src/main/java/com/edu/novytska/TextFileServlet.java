package com.edu.novytska;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;


@WebServlet(urlPatterns = "/*")
public class TextFileServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response){

        InputStream file = getServletContext().getResourceAsStream("/WEB-INF/classes/testfile.txt");
        if(file == null){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        String lengthStr = request.getParameter("length");
        String limitStr = request.getParameter("limit");
        String q = request.getParameter("q");

        if (q == null) {
            q = "";
        }

        Integer length = null;
        Integer limit = 10000;

        try {
            if (lengthStr != null && !lengthStr.isEmpty()) {
                length = Integer.parseInt(lengthStr);
            }
            if (limitStr != null && !limitStr.isEmpty()) {
                limit = Integer.parseInt(limitStr);
            }

            if ((length != null && length < 0) || limit < 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            response.setContentType("application/json");

            PrintWriter out = response.getWriter();
            out.print(buildBody(length, limit, q, file));
            out.flush();
        } catch (NumberFormatException e){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String buildBody(Integer length, Integer limit, String q, InputStream file){
        Scanner sc = new Scanner(file);

        int totalLength = 0;
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        while (sc.hasNextLine()){
            String line = sc.nextLine();
            if(line.contains(q)){
                if(length != null && line.length() >= length){
                    line = line.substring(0, length);
                }
                if(totalLength + line.length() > limit) {
                    break;
                }
                arrayBuilder.add(line);
                totalLength += line.length();
            }
        }
        return Json.createObjectBuilder().add("text", arrayBuilder.build()).build().toString();
    }
}
