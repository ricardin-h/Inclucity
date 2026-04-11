package com.inclucity.web.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa uma solicitação de serviço urbano no sistema IncluCity.
 * Centraliza as informações do problema reportado, dados do cidadão e o rastreio de status.
 */
public class Solicitacao {
    private final String protocolo;
    private final String categoria;
    private final String descricao;
    private final String localizacao;
    private final LocalDateTime dataCriacao;
    private StatusSolicitacao statusAtual;
    private final boolean ehAnonima;
    private final String nomeCidadao;
    private final String contatoCidadao;
    private final List<HistoricoStatus> historicoMovimentacoes;

    /**
     * Construtor para criar uma nova solicitação.
     * Gera automaticamente um protocolo único e inicia o histórico com o status ABERTO.
     */
    public Solicitacao(String categoria, String descricao, String localizacao, boolean ehAnonima, String nomeCidadao, String contatoCidadao) {
        // Gera um identificador único de 8 caracteres para facilitar a busca pelo cidadão
        this.protocolo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.categoria = categoria;
        this.descricao = descricao;
        this.localizacao = localizacao;
        this.dataCriacao = LocalDateTime.now();
        this.statusAtual = StatusSolicitacao.ABERTO;
        this.ehAnonima = ehAnonima;
        this.nomeCidadao = nomeCidadao;
        this.contatoCidadao = contatoCidadao;

        // Inicializa a lista de histórico e registra a criação da solicitação
        this.historicoMovimentacoes = new ArrayList<>();
        this.historicoMovimentacoes.add(new HistoricoStatus(null, StatusSolicitacao.ABERTO, "Solicitação criada.", "Sistema"));
    }

    // Getters básicos
    public String getProtocolo() { return protocolo; }
    public String getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public String getLocalizacao() { return localizacao; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }

    /**
     * Retorna a data de criação formatada para o padrão brasileiro (dd/MM/yyyy HH:mm).
     */
    public String getFormattedDataCriacao() {
        return dataCriacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public StatusSolicitacao getStatusAtual() { return statusAtual; }
    public boolean isEhAnonima() { return ehAnonima; }
    public String getNomeCidadao() { return nomeCidadao; }
    public String getContatoCidadao() { return contatoCidadao; }

    /**
     * Retorna uma cópia da lista de histórico para garantir a imutabilidade da lista original externa à classe.
     */
    public List<HistoricoStatus> getHistoricoMovimentacoes() {
        return new ArrayList<>(historicoMovimentacoes);
    }

    /**
     * Atualiza o status da solicitação e registra a movimentação no histórico.
     *
     * @param novoStatus O novo estado da solicitação.
     * @param comentario Justificativa técnica ou observação sobre a atualização.
     * @param responsavel Nome do agente público ou sistema que realizou a alteração.
     */
    public void atualizarStatus(StatusSolicitacao novoStatus, String comentario, String responsavel) {
        StatusSolicitacao statusAnterior = this.statusAtual;
        this.statusAtual = novoStatus;
        // Adiciona a transição ao histórico para auditoria futura
        this.historicoMovimentacoes.add(new HistoricoStatus(statusAnterior, novoStatus, comentario, responsavel));
    }
}
