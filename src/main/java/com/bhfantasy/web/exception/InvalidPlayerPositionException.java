package com.bhfantasy.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "invalid player position")
public class InvalidPlayerPositionException extends RuntimeException {
}
