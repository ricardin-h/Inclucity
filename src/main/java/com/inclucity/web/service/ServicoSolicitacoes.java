package com.inclucity.web.service;

import com.inclucity.web.model.Solicitacao;
import com.inclucity.web.model.StatusSolicitacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServicoSolicitacoes {
    private final ConcurrentHashMap<String, Solicitacao> solicitacoes = new ConcurrentHashMap<>();
    private final List<String> categoriasDisponiveis;

    public ServicoSolicitacoes() {
        this.categoriasDisponiveis = new ArrayList<>();
        this.categoriasDisponiveis.add("Iluminação Pública");
        this.categoriasDisponiveis.add("Buraco na Rua");
        this.categoriasDisponiveis.add("Limpeza Urbana");
        this.categoriasDisponiveis.add("Acessibilidade (Calçadas/Rampas)");
        this.categoriasDisponiveis.add("Sinalização");
        this.categoriasDisponiveis.add("Outros");
    }

    public Solicitacao criarSolicitacao(String categoria, String descricao, String localizacao, boolean anonima, String nomeCidadao, String contatoCidadao) {
        // Validações básicas
        if (categoria == null || categoria.trim().isEmpty() || !categoriasDisponiveis.contains(categoria)) {
            throw new IllegalArgumentException("Categoria inválida.");
        }
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição não pode ser vazia.");
        }
        if (localizacao == null || localizacao.trim().isEmpty()) {
            throw new IllegalArgumentException("Localização não pode ser vazia.");
        }

        Solicitacao novaSolicitacao = new Solicitacao(categoria, descricao, localizacao, anonima, nomeCidadao, contatoCidadao);
        solicitacoes.put(novaSolicitacao.getProtocolo(), novaSolicitacao);
        return novaSolicitacao;
    }

    public Optional<Solicitacao> buscarSolicitacaoPorProtocolo(String protocolo) {
        return Optional.ofNullable(solicitacoes.get(protocolo));
    }

    public List<Solicitacao> listarTodasSolicitacoes() {
        List<Solicitacao> lista = new ArrayList<>(solicitacoes.values());
        // Ordenar por data de criação mais recente primeiro
        Collections.sort(lista, (s1, s2) -> s2.getDataCriacao().compareTo(s1.getDataCriacao()));
        return lista;
    }

    public List<Solicitacao> filtrarSolicitacoes(String categoria, String localizacao, StatusSolicitacao status) {
        return solicitacoes.values().stream()
                .filter(s -> (categoria == null || categoria.isEmpty() || s.getCategoria().equalsIgnoreCase(categoria)))
                .filter(s -> (localizacao == null || localizacao.isEmpty() || s.getLocalizacao().toLowerCase().contains(localizacao.toLowerCase())))
                .filter(s -> (status == null || s.getStatus().equals(status)))
                .sorted((s1, s2) -> s2.getDataCriacao().compareTo(s1.getDataCriacao())) // Mais recentes primeiro
                .collect(Collectors.toList());
    }

    public Solicitacao atualizarStatusSolicitacao(String protocolo, StatusSolicitacao novoStatus, String comentario, String responsavel) {
        Solicitacao solicitacao = solicitacoes.get(protocolo);
        if (solicitacao == null) {
            throw new IllegalArgumentException("Solicitação com protocolo " + protocolo + " não encontrada.");
        }
        if (comentario == null || comentario.trim().isEmpty()) {
            throw new IllegalArgumentException("Comentário é obrigatório ao atualizar o status.");
        }
        if (responsavel == null || responsavel.trim().isEmpty()) {
            throw new IllegalArgumentException("Responsável é obrigatório ao atualizar o status.");
        }
        solicitacao.setStatus(novoStatus, comentario, responsavel);
        return solicitacao;
    }

    public List<String> getCategoriasDisponiveis() {
        return Collections.unmodifiableList(categoriasDisponiveis);
    }

    public List<StatusSolicitacao> getStatusDisponiveis() {
        List<StatusSolicitacao> statusList = new ArrayList<>();
        Collections.addAll(statusList, StatusSolicitacao.values());
        return statusList;
    }
}
