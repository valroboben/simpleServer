package common;

public enum HttpStatusCodes {

    OK(200),
    NOT_FOUND(404);

    int code;

    HttpStatusCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
