package entity;

import java.util.List;

public class Result {

    private Boolean success;

    private String message;

    private List<Long> haveChild;

    public Result() {
    }

    public Result(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Result(Boolean success, String message, List<Long> haveChild) {
        this.success = success;
        this.message = message;
        this.haveChild = haveChild;
    }

    public List<Long> getHaveChild() {
        return haveChild;
    }

    public void setHaveChild(List<Long> haveChild) {
        this.haveChild = haveChild;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
