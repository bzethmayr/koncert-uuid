package net.zethmayr.benjamin.demo.koncertuuid.controller;

import lombok.val;
import net.zethmayr.benjamin.demo.koncertuuid.model.SimUuidGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class SimUuidController {
    // We do not maintain a default instance - we could, I suppose.
    @ResponseBody
    @RequestMapping(value = "/simUuid", method = GET)
    public String simUuid(
            final @RequestParam(required = false) Integer x,
            final @RequestParam(required = false) Integer y,
            final @RequestParam(required = false) Integer z
    ) {
        val generatorBuilder = SimUuidGenerator.builder();
        if (x != null) {
            generatorBuilder.x(x);
        }
        if (y != null) {
            generatorBuilder.y(y);
        }
        if (z != null) {
            generatorBuilder.z(z);
        }
        val generator = generatorBuilder.build();
        return generator.generate();
    }

    @ControllerAdvice
    public static class ErrorHandler extends ResponseEntityExceptionHandler {
        @ExceptionHandler(IllegalArgumentException.class)
        protected ResponseEntity<Object> handleBadArguments(final RuntimeException re, final WebRequest request) {
            return handleExceptionInternal(re, re.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
        }
    }

}
