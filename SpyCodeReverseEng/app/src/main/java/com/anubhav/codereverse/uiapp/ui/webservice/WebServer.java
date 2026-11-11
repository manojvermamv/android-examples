package com.anubhav.codereverse.uiapp.ui.webservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {

    private final String filePath;

    public static WebServer start(String filePath) {
        WebServer server = new WebServer(filePath);
        try {
            if (!server.isAlive()) server.start();
        } catch (IOException e) {
            server.stopWebServer();
        }
        return server;
    }

    public WebServer() {
        this("");
    }

    public WebServer(String filePath) {
        super(8080);
        this.filePath = filePath;
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            System.out.println("\nRunning! Point your browsers to http://localhost:8080/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        return newFixedLengthResponse(readHtmlFile());
    }

    private String simpleMsg(String message) {
        String html = "<html><body><h1>Server is running...</h1>\n";
        if (!message.isEmpty()) {
            html += "<br></br>";
            html += "<p>" + message + "</p>";
            html += "<br></br>";
        }
        return html + "</body></html>\n";
    }

    private String readHtmlFile() {
        try {
            File serverFile = new File(filePath);
            if (!serverFile.exists()) {
                return simpleMsg("Html file not exits at " + filePath);
            }

            BufferedReader reader = new BufferedReader(new FileReader(serverFile));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return simpleMsg((e.getMessage() == null) ? "" : e.getMessage());
        }
    }

    public void stopWebServer() {
        if (isAlive()) {
            stop();
        }
    }

}