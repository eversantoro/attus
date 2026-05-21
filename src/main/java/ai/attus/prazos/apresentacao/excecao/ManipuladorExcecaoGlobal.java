package ai.attus.prazos.apresentacao.excecao;

import ai.attus.prazos.dominio.excecao.ConcorrenciaPrazoException;
import ai.attus.prazos.dominio.excecao.PrazoNaoEncontradoException;
import ai.attus.prazos.dominio.excecao.RegraNegocioException;
import ai.attus.prazos.infraestrutura.configuracao.FiltroCorrelationId;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ManipuladorExcecaoGlobal {

    private static final Logger log = LoggerFactory.getLogger(ManipuladorExcecaoGlobal.class);
    private static final String TIPO_BASE = "https://attus.ai/problemas/";

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ProblemDetail> tratarRegraNegocio(
            RegraNegocioException ex,
            HttpServletRequest request) {
        log.error("Falha de validação de negócio | correlationId={} | uri={} | mensagem={}",
                correlationId(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest().body(criarProblema(
                HttpStatus.BAD_REQUEST,
                "regra-negocio-violada",
                "Regra de negócio violada",
                ex.getMessage(),
                request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> tratarValidacaoBean(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> erros = new HashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            erros.put(erro.getField(), erro.getDefaultMessage());
        }
        log.error("Falha de validação de entrada | correlationId={} | uri={} | erros={}",
                correlationId(), request.getRequestURI(), erros, ex);
        ProblemDetail problema = criarProblema(
                HttpStatus.BAD_REQUEST,
                "validacao-entrada",
                "Dados de entrada inválidos",
                "Corrija os campos informados e tente novamente.",
                request);
        problema.setProperty("erros", erros);
        return ResponseEntity.badRequest().body(problema);
    }

    @ExceptionHandler(PrazoNaoEncontradoException.class)
    public ResponseEntity<ProblemDetail> tratarNaoEncontrado(
            PrazoNaoEncontradoException ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(criarProblema(
                HttpStatus.NOT_FOUND,
                "prazo-nao-encontrado",
                "Prazo não encontrado",
                ex.getMessage(),
                request));
    }

    @ExceptionHandler({ConcorrenciaPrazoException.class, OptimisticLockingFailureException.class})
    public ResponseEntity<ProblemDetail> tratarConcorrencia(
            Exception ex,
            HttpServletRequest request) {
        log.error("Conflito de concorrência ao atualizar prazo | correlationId={} | uri={}",
                correlationId(), request.getRequestURI(), ex);
        String mensagem = ex instanceof ConcorrenciaPrazoException c
                ? c.getMessage()
                : "O prazo foi alterado por outro usuário. Atualize a página e tente novamente.";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(criarProblema(
                HttpStatus.CONFLICT,
                "concorrencia-otimista",
                "Conflito de concorrência",
                mensagem,
                request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> tratarGenerico(
            Exception ex,
            HttpServletRequest request) {
        log.error("Erro não tratado | correlationId={} | uri={}",
                correlationId(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(criarProblema(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "erro-interno",
                "Erro interno do servidor",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                request));
    }

    private ProblemDetail criarProblema(
            HttpStatus status,
            String tipo,
            String titulo,
            String detalhe,
            HttpServletRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        problema.setType(URI.create(TIPO_BASE + tipo));
        problema.setInstance(URI.create(request.getRequestURI()));
        problema.setProperty("correlationId", correlationId());
        return problema;
    }

    private String correlationId() {
        return MDC.get(FiltroCorrelationId.MDC_CORRELATION_ID);
    }
}
