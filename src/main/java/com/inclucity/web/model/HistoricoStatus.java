package com.inclucity.web.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistoricoStatus {
    private StatusSolicitacao statusAnterior;
    private StatusSolicitacao statusAtual;
    private String comentario;
    private String responsavel;
    private LocalDateTime dataHora;

    public HistoricoStatus(StatusSolicitacao statusAnterior, StatusSolicitacao statusAtual, String comentario, String responsavel) {
        this.statusAnterior = statusAnterior;
        this.statusAtual = statusAtual;
        this.comentario = comentario;
        this.responsavel = responsavel;
        this.dataHora = LocalDateTime.now();
    }

    // Getters
    public StatusSolicitacao getStatusAnterior() {
        return statusAnterior;
    }

    public StatusSolicitacao getStatusAtual() {
        return statusAtual;
    }

    public String getComentario() {
        return comentario;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getFormattedDataHora() {
        return dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
