package msp.community.user.exception;

public class UserNotFoundException extends ApiException {

    public UserNotFoundException(String message) {
        super(ErrorCode.USER_NOT_FOUND, message);
    }
}
