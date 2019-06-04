package chain.fxgj.server.payroll.exception;

import chain.css.exception.ErrorMsg;

/**
 * Created  by syd on 2019/1/24 0024.
 */
public class ForbiddenServiceException extends RuntimeException {
    private ErrorMsg errorMsg;

    public ForbiddenServiceException(Throwable cause, ErrorMsg errorMsg) {
        super(cause);
        this.errorMsg = errorMsg;
    }

    public ForbiddenServiceException(ErrorMsg errorMsg) {
        super(errorMsg.getThrowableMessage());
        this.errorMsg = errorMsg;
    }

    public ErrorMsg getErrorMsg() {
        return errorMsg;
    }

}
