package enums;

public enum Status {

    ABERTO("Aberto"),
    TRIAGEM("Em triagem"),
    EM_EXECUCAO("Em execução"),
    RESOLVIDO("Resolvido"),
    ENCERRADO("Encerrado");

    private String descricao;

    Status(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}