package common;

import java.io.Serializable;

public class Message implements Serializable {

    private HttpMethods httpMethod;
    private HttpStatusCodes httpStatusCode;
    private String fileName;
    private byte[] data;
    private int ID;

    public Message(HttpMethods httpMethod, HttpStatusCodes httpStatusCode, String fileName, byte[] data) {
        this.httpMethod = httpMethod;
        this.httpStatusCode = httpStatusCode;
        this.fileName = fileName;
        this.data = data;
    }

    public HttpMethods getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethods httpMethod) {
        this.httpMethod = httpMethod;
    }

    public HttpStatusCodes getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(HttpStatusCodes httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

}
