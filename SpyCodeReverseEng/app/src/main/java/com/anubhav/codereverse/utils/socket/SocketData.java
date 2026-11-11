//package com.anubhav.codereverse.utils.socket;
//
//import org.json.JSONObject;
//
//import java.io.Serializable;
//
//public class SocketData implements Serializable {
//
//    private String deviceID;
//    private String data;
//    private int length;
//
//    public SocketData() {
//    }
//
//    public SocketData(String deviceID, String data, int length) {
//        this.deviceID = deviceID;
//        this.data = data;
//        this.length = length;
//    }
//
//    public String getDeviceID() {
//        return deviceID;
//    }
//
//    public void setDeviceID(String deviceID) {
//        this.deviceID = deviceID;
//    }
//
//    public String getData() {
//        return data;
//    }
//
//    public void setData(String data) {
//        this.data = data;
//    }
//
//    public int getLength() {
//        return length;
//    }
//
//    public void setLength(int length) {
//        this.length = length;
//    }
//
//    /**
//     * static methods
//     */
//    public String toJson() throws Exception {
//        JSONObject object = new JSONObject();
//        object.put("deviceID", this.deviceID);
//        object.put("data", this.data);
//        object.put("length", this.length);
//        System.out.println(object);
//        return object.toString();
//    }
//
//    public static SocketData fromJson(String json) throws Exception {
//        JSONObject object = new JSONObject(json);
//        SocketData data = new SocketData(object.getString("deviceID"), object.getString("data"), object.getInt("length"));
//        System.out.println(data.toString());
//        return data;
//    }
//
//}