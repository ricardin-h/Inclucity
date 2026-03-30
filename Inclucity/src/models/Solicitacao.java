package models;

import enums.Status;

public class Solicitacao {

    private int protocolo;
    private String categoria;
    private String descricao;
    private String bairro;
    private Status status;
    private boolean anonimo;

    public Solicitacao(int protocolo, String categoria, String descricao, String bairro, boolean anonimo) {
        this.protocolo = protocolo;
        this.categoria = categoria;
        this.descricao = descricao;
        this.bairro = bairro;
        this.anonimo = anonimo;
        this.status = Status.ABERTO; // começa sempre como ABERTO
    }

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
                (anonimo ? " | Anônimo" : " | Identificado");
    }
}