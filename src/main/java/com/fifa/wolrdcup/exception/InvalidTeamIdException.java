package com.fifa.wolrdcup.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "invalid teamID")
public class InvalidTeamIdException extends RuntimeException {
}
