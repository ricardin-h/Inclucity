package com.inclucity.web.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Solicitacao {
    private String protocolo;
    private String categoria;
    private String descricao;
    private String localizacao;
    private LocalDateTime dataCriacao;
    private StatusSolicitacao status;
    private boolean anonima;
    private String nomeCidadao;
    private String contatoCidadao;
    private List<HistoricoStatus> historico;

    public Solicitacao(String categoria, String descricao, String localizacao, boolean anonima, String nomeCidadao, String contatoCidadao) {
        this.protocolo = generateProtocolo();
        this.categoria = categoria;
        this.descricao = descricao;
        this.localizacao = localizacao;
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusSolicitacao.ABERTO;
        this.anonima = anonima;
        this.nomeCidadao = nomeCidadao;
        this.contatoCidadao = contatoCidadao;
        this.historico = new ArrayList<>();
        this.historico.add(new HistoricoStatus(null, StatusSolicitacao.ABERTO, "Solicitação criada.", "Sistema"));
    }

    private String generateProtocolo() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Getters
    public String getProtocolo() {
        return protocolo;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public String getFormattedDataCriacao() {
        return dataCriacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public boolean isAnonima() {
        return anonima;
    }

    public String getNomeCidadao() {
        return nomeCidadao;
    }

    public String getContatoCidadao() {
        return contatoCidadao;
    }

    public List<HistoricoStatus> getHistorico() {
        return historico;
    }

    // Setters
    public void setStatus(StatusSolicitacao status, String comentario, String responsavel) {
        StatusSolicitacao oldStatus = this.status;
        this.status = status;
        this.historico.add(new HistoricoStatus(oldStatus, status, comentario, responsavel));
    }

    @Override
    public String toString() {
        return "Protocolo: " + protocolo + ", Categoria: " + categoria + ", Status: " + status + ", Localização: " + localizacao;
    }
}
