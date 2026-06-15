package msp.community.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    private Instant timestamp;
    private int status;
    private String errorCode;
    private String message;
    private String path;
    private Map<String, String> details;

    public static ApiErrorResponse of(
            ErrorCode errorCode,
            String message,
            String path,
            Map<String, String> details
    ) {
        return new ApiErrorResponse(
                Instant.now(),
                errorCode.getHttpStatus().value(),
                errorCode.name(),
                message,
                path,
                details
        );
    }
}
