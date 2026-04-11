package com.inclucity.web.service;

import com.inclucity.web.model.Solicitacao;
import com.inclucity.web.model.StatusSolicitacao;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Camada de serviço responsável por orquestrar as regras de negócio das solicitações.
 * Atua como intermediária entre a interface web e o armazenamento de dados.
 */
public class ServicoSolicitacoes {

    // Simula um banco de dados em memória.
    // O uso de ConcurrentHashMap garante segurança em ambientes multithread (múltiplos acessos simultâneos).
    private final Map<String, Solicitacao> bancoDadosSolicitacoes = new ConcurrentHashMap<>();

    /**
     * Instancia e persiste uma nova solicitação no sistema.
     **/
    public Solicitacao criarSolicitacao(String categoria, String descricao, String localizacao, boolean ehAnonima, String nomeCidadao, String contatoCidadao) {
        Solicitacao nova = new Solicitacao(categoria, descricao, localizacao, ehAnonima, nomeCidadao, contatoCidadao);
        // Utiliza o protocolo único como chave de busca rápida no "banco"
        bancoDadosSolicitacoes.put(nova.getProtocolo(), nova);
        return nova;
    }

    /**
     * Busca uma solicitação específica através do seu protocolo único.
     **/
    public Optional<Solicitacao> buscarPorProtocolo(String protocolo) {
        return Optional.ofNullable(bancoDadosSolicitacoes.get(protocolo));
    }

    /**
     * Recupera todas as solicitações cadastradas.
     */
    public List<Solicitacao> listarTodasSolicitacoes() {
        return bancoDadosSolicitacoes.values().stream()
                // Ordenação decrescente: as mais novas aparecem primeiro no painel
                .sorted((s1, s2) -> s2.getDataCriacao().compareTo(s1.getDataCriacao()))
                .collect(Collectors.toList());
    }

    /**
     * Aplica múltiplos filtros opcionais para facilitar a busca no painel do gestor.
     */
    public List<Solicitacao> filtrarSolicitacoes(String categoria, String localizacao, StatusSolicitacao status) {
        return bancoDadosSolicitacoes.values().stream()
                // Aplica os filtros apenas se os parâmetros não forem nulos ou vazios
                .filter(s -> (categoria == null || categoria.isEmpty() || s.getCategoria().equalsIgnoreCase(categoria)))
                .filter(s -> (localizacao == null || localizacao.isEmpty() || s.getLocalizacao().toLowerCase().contains(localizacao.toLowerCase())))
                .filter(s -> (status == null || s.getStatusAtual().equals(status)))
                .sorted((s1, s2) -> s2.getDataCriacao().compareTo(s1.getDataCriacao()))
                .collect(Collectors.toList());
    }

    /**
     * Orquestra a transição de status de uma solicitação existente.
     **/
    public void atualizarStatusSolicitacao(String protocolo, StatusSolicitacao novoStatus, String comentario, String responsavel) {
        // Valida a existência antes de tentar atualizar
        Solicitacao s = buscarPorProtocolo(protocolo)
                .orElseThrow(() -> new IllegalArgumentException("Protocolo inválido."));

        s.atualizarStatus(novoStatus, comentario, responsavel);
    }
}
