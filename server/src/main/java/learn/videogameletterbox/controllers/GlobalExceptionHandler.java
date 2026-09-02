package learn.videogameletterbox.controllers;

import learn.videogameletterbox.external.igdb.IgdbApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IgdbApiException.class)
    public ResponseEntity<ErrorResponse> handleIgdbApiException(IgdbApiException ex) {
        return new ResponseEntity<>(
                new ErrorResponse(ex.getMessage()),
                HttpStatus.BAD_GATEWAY);
    }
}
