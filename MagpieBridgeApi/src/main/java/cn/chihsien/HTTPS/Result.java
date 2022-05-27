package cn.chihsien.HTTPS;

/**
 * @describe
 * @auther chihsiencheng
 * 是否扣除成功的响应类
 */
public class Result {
    private Boolean success;//成功扣除
    private String msg;//反馈的消息，等待超时、库存不足

    public Result(Boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", msg='" + msg + '\'' +
                '}';
    }
}
