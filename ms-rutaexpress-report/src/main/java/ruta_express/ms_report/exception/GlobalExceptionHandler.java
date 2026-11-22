package ruta_express.ms_report.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// errores HTTP de report con ProblemDetail (contrato 5)
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400: dato de entrada inválido (por ejemplo, un range que no existe)
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Solicitud inválida");
        return problem;
    }

    // 500: cualquier otro error
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Error interno en report: " + ex.getMessage());
        problem.setTitle("Internal Server Error");
        return problem;
    }
}
