package services;

import enums.Status;
import models.Solicitacao;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServicoSolicitacoes {

    private List<Solicitacao> solicitacoes = new ArrayList<>();
    private int contadorProtocolo = 1;


    //CRIAR SOLICITAÇÕES
    public Solicitacao criarSolicitacao(String categoria, String descricao, String bairro, boolean anonimo) {
        if (categoria == null || categoria.isBlank()) {
            //CASO DADOS ESTEJAM INCORRETOS IMPRIME AVISO
            throw new IllegalArgumentException("Categoria inválida");
        }

        if (descricao == null || descricao.isBlank()) {
            //CASO DADOS ESTEJAM INCORRETOS IMPRIME AVISO
            throw new IllegalArgumentException("Descrição inválida");
        }

        if (bairro == null || bairro.isBlank()) {
            //CASO DADOS ESTEJAM INCORRETOS IMPRIME AVISO
            throw new IllegalArgumentException("Bairro inválido");
        }
        Solicitacao solicitacao = new Solicitacao(
                contadorProtocolo++,
                categoria,
                descricao,
                bairro,
                anonimo
        );

        solicitacoes.add(solicitacao);
        return solicitacao;
    }

    //LISTAR SOLICITACOES
    public List<Solicitacao> listarSolicitacoes(){
        return new ArrayList<>(solicitacoes);
    }

    //BUSCAR POR PROTOCOLO
    public Solicitacao buscarPorProtocolo(int protocolo){
        Optional <Solicitacao> resultado = solicitacoes.stream().filter(
                s -> s.getProtocolo() == protocolo).findFirst();

        return resultado.orElse(null);
    }

    //ADICIONAR COMENTÁRIOS
    public boolean adicionarComentario(int protocolo, String comentario){
        Solicitacao solicitacao = buscarPorProtocolo(protocolo);

        if(solicitacao == null || comentario == null || comentario.isBlank()){
            return false;
        }
        solicitacao.adicionarComentario(comentario);
        return true;
    }


    //ATUALIZAR STATUS
    public boolean atualizarStatus(int protocolo, Status novoStatus){
        Solicitacao solicitacao = buscarPorProtocolo(protocolo);

        if(solicitacao == null || novoStatus == null){
            return false;
        }

        if(!validarTransicao(solicitacao.getStatus(), novoStatus)){
            return false;
        }

        solicitacao.setStatus(novoStatus);
        return true;
    }

    private boolean validarTransicao(Status atual, Status novo){
        return switch (atual){
            case ABERTO -> novo == Status.TRIAGEM;
            case TRIAGEM -> novo == Status.EM_EXECUCAO;
            case EM_EXECUCAO -> novo == Status.RESOLVIDO;
            case RESOLVIDO -> novo == Status.ENCERRADO;
            default -> false;
        };
    }

}