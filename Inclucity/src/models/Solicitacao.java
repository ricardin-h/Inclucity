package models;

import enums.Status;

import java.util.ArrayList;
import java.util.List;

//SOLICITAÇÃO DE UM CIDADÃO
public class Solicitacao {

    private int protocolo;
    private String categoria;
    private String descricao;
    private String bairro;
    private String rua;
    private String estabelecimento;
    private Status status;
    private boolean anonimo;

    private List<String> comentarios = new ArrayList<>();

    //LISTA DE COMENTÁRIOS, REGISTRADOS DURANTE O ATENDIMENTO
    public void adicionarComentario(String comentario) {
        comentarios.add(comentario);
    }

    //CRIA SOLICITAÇÃO COM STATUS ABERTO
    public Solicitacao(int protocolo, String categoria, String descricao, String bairro, String rua, String estabelecimento, boolean anonimo) {
        this.protocolo = protocolo;
        this.categoria = categoria;
        this.descricao = descricao;
        this.bairro = bairro;
        this.rua = rua;
        this.estabelecimento = estabelecimento;
        this.anonimo = anonimo;
        this.status = Status.ABERTO; // COMEÇA SEMPRE COMO ABERTO
    }

    public List<String> getComentarios() {
        return comentarios;
    }

    //GETTERS E SETTERS
    public int getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(int protocolo) {
        this.protocolo = protocolo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getEstabelecimento() { return estabelecimento; }

    public void setEstabelecimento(String estabelecimento) { this.estabelecimento = estabelecimento; }

    public String getRua() { return rua; }

    public void setRua(String rua) { this.rua = rua; }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isAnonimo() {
        return anonimo;
    }

    public void setAnonimo(boolean anonimo) {
        this.anonimo = anonimo;
    }

    @Override
    public String toString() {
        return "Protocolo: " + protocolo +
                " | Categoria: " + categoria +
                " | Status: " + status.getDescricao() +
                " | Bairro: " + bairro +
                " | Rua : " + rua +
                " | Estabelecimento : " + estabelecimento +
                " | Descrição : " + descricao +
                (anonimo ? " | Anônimo" : " | Identificado") +
                " | Comentários: " + comentarios;
    }
}