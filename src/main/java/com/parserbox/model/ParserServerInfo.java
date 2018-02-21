package com.parserbox.model;

public class ParserServerInfo {

    String status;
    String message;
    String error;

    String serverEndPoint1;
    String serverEndPoint2;
    String serverEndPoint3;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getServerEndPoint1() {
        return serverEndPoint1;
    }

    public void setServerEndPoint1(String serverEndPoint1) {
        this.serverEndPoint1 = serverEndPoint1;

    }

    public String getServerEndPoint2() {
        return serverEndPoint2;
    }

    public void setServerEndPoint2(String serverEndPoint2) {
        this.serverEndPoint2 = serverEndPoint2;
    }

    public String getServerEndPoint3() {
        return serverEndPoint3;
    }

    public void setServerEndPoint3(String serverEndPoint3) {
        this.serverEndPoint3 = serverEndPoint3;
    }
}
