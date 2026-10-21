package dk.library.rest;

import dk.library.db.DatabaseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Oversætter exceptions fra controllerne til fejlsvar i formatet Problem Details (RFC 9457), fx
 * {"status": 404, "title": "Not Found", "detail": "Bog 5 findes ikke", "instance": "/api/books/5"}.
 * <p>
 * Forælderklassen håndterer allerede Springs egne fejl (ugyldig JSON, forkert type i URL'en, ResponseStatusException
 * fra controllerne osv.). Her tilføjes feltfejl ved validering og 500 ved databasefejl.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = Logger.getLogger(ApiExceptionHandler.class.getName());

    /** @Valid på en request-body fejlede. Svaret får en liste med "felt: besked". */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .toList();
        return badRequest(ex, ex.getBody(), "Ugyldig request-body", errors, headers, status, request);
    }

    /** @Min, @Max osv. på en query-parameter fejlede, fx limit=500. */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> result.getMethodParameter().getParameterName() + ": " + error.getDefaultMessage()))
                .sorted()
                .toList();
        return badRequest(ex, ex.getBody(), "Ugyldige parametre", errors, headers, status, request);
    }

    @ExceptionHandler(DatabaseException.class)
    ProblemDetail handleDatabase(DatabaseException e) {
        LOG.log(Level.SEVERE, "Databasefejl", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    /** Alt andet uventet. Detaljerne logges, men sendes ikke til klienten. */
    @ExceptionHandler(RuntimeException.class)
    ProblemDetail handleUnexpected(RuntimeException e) {
        LOG.log(Level.SEVERE, "Uventet fejl", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Uventet fejl på serveren");
    }

    private ResponseEntity<Object> badRequest(Exception ex, ProblemDetail problem, String detail, Object errors,
                                              HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        problem.setDetail(detail);
        problem.setProperty("errors", errors);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }
}
