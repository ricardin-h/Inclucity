package com.inclucity.web.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa um registro histórico de mudança de status de uma solicitação.
 * Armazena o rastro de auditoria, incluindo quem alterou, quando e o motivo.
 */
public class HistoricoStatus {
    private final StatusSolicitacao statusAnterior;
    private final StatusSolicitacao statusAtual;
    private final String comentario;
    private final String responsavel;
    private final LocalDateTime dataHoraRegistro;

    /**
     * Cria um novo registro de histórico no momento da transição de status.
     **/
    public HistoricoStatus(StatusSolicitacao statusAnterior, StatusSolicitacao statusAtual, String comentario, String responsavel) {
        this.statusAnterior = statusAnterior;
        this.statusAtual = statusAtual;
        this.comentario = comentario;
        this.responsavel = responsavel;
        this.dataHoraRegistro = LocalDateTime.now(); // Registra o momento exato da operação
    }

    // Getters para acesso aos dados do histórico
    public StatusSolicitacao getStatusAnterior() { return statusAnterior; }
    public StatusSolicitacao getStatusAtual() { return statusAtual; }
    public String getComentario() { return comentario; }
    public String getResponsavel() { return responsavel; }

    /**
     * Retorna a data e hora do registro formatada para exibição no padrão brasileiro.
     * Exemplo: 10/04/2026 19:30
     */
    public String getFormattedDataHora() {
        return dataHoraRegistro.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
