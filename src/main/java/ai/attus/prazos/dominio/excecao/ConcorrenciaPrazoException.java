package ai.attus.prazos.dominio.excecao;

public class ConcorrenciaPrazoException extends RuntimeException {

    public ConcorrenciaPrazoException() {
        super("O prazo foi alterado por outro usuário. Atualize a página e tente novamente.");
    }
}
