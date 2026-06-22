package pvz.model;

public class Result {

    private boolean success;
    private String message;
    private Object object;

    public Result(boolean success, String message, Object object) {
        this.success = success;
        this.message = message;
        this.object = object;
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }


    public boolean success() {
        return success;
    }

    public String message() {
        return message;
    }
}
