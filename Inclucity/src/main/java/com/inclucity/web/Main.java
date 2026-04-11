package com.inclucity.web;

import com.inclucity.web.model.Solicitacao;
import com.inclucity.web.model.StatusSolicitacao;
import com.inclucity.web.service.ServicoSolicitacoes;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;

public class Main {
    private static final ServicoSolicitacoes servicoSolicitacoes = new ServicoSolicitacoes();
    private static final int PORTA = 9090;

    // Caminhos baseados na estrutura padrão do projeto (Maven/IntelliJ)
    private static String STATIC_DIR = "src/main/resources/static";
    private static String TEMPLATES_DIR = "src/main/resources/templates";

    static {
        // Verifica se estamos rodando de dentro de 'src' ou da raiz e ajusta os caminhos
        if (!Files.exists(Paths.get(STATIC_DIR))) {
            // Tenta caminhos alternativos se o padrão falhar (ex: rodando de dentro de subpastas)
            String[] tentativas = {"Inclucity/" + STATIC_DIR, "../" + STATIC_DIR};
            for (String tentativa : tentativas) {
                if (Files.exists(Paths.get(tentativa))) {
                    STATIC_DIR = tentativa;
                    TEMPLATES_DIR = tentativa.replace("static", "templates");
                    break;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(PORTA), 0);

        // Servir arquivos estáticos (CSS/JS)
        servidor.createContext("/css", new StaticFileHandler(STATIC_DIR + "/css"));
        servidor.createContext("/js", new StaticFileHandler(STATIC_DIR + "/js"));

        // Rotas da Aplicação
        servidor.createContext("/", new IndexHandler());
        servidor.createContext("/solicitacoes/nova", new NovaSolicitacaoHandler());
        servidor.createContext("/solicitacoes/confirmacao", new ConfirmacaoSolicitacaoHandler());
        servidor.createContext("/solicitacoes/consultar", new ConsultarSolicitacaoHandler());
        servidor.createContext("/solicitacoes/detalhes", new DetalhesSolicitacaoHandler());
        servidor.createContext("/solicitacoes/painel", new PainelGestorHandler());

        servidor.setExecutor(Executors.newFixedThreadPool(10));
        servidor.start();

        System.out.println("-------------------------------------------");
        System.out.println("IncluCity rodando em: http://localhost:" + PORTA);
        System.out.println("Certifique-se de rodar a partir da raiz do projeto.");
        System.out.println("-------------------------------------------");
    }

    // --- Handlers de Base ---

    static class StaticFileHandler implements HttpHandler {
        private final String diretorio;
        public StaticFileHandler(String diretorio) { this.diretorio = diretorio; }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            // Remove o prefixo do contexto (ex: /css ou /js) para pegar o nome do arquivo
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            Path file = Paths.get(diretorio, fileName);

            if (Files.exists(file) && !Files.isDirectory(file)) {
                String type = Files.probeContentType(file);
                if (type == null) {
                    if (path.endsWith(".css")) type = "text/css";
                    else if (path.endsWith(".js")) type = "application/javascript";
                }
                exchange.getResponseHeaders().set("Content-Type", type != null ? type : "application/octet-stream");
                exchange.sendResponseHeaders(200, Files.size(file));
                try (OutputStream os = exchange.getResponseBody()) { Files.copy(file, os); }
            } else {
                String error = "Arquivo não encontrado: " + file.toAbsolutePath();
                exchange.sendResponseHeaders(404, error.length());
                try (OutputStream os = exchange.getResponseBody()) { os.write(error.getBytes()); }
            }
        }
    }

    static abstract class BaseHandler implements HttpHandler {
        protected void render(HttpExchange exchange, String templateName, Map<String, Object> data) throws IOException {
            Path path = Paths.get(TEMPLATES_DIR, templateName);
            if (!Files.exists(path)) {
                String error = "Template não encontrado: " + path.toAbsolutePath();
                exchange.sendResponseHeaders(404, error.length());
                try (OutputStream os = exchange.getResponseBody()) { os.write(error.getBytes()); }
                return;
            }

            String html = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            if (data != null) {
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    String valor = (entry.getValue() != null) ? String.valueOf(entry.getValue()) : "";
                    html = html.replace("[[${" + entry.getKey() + "}]]", valor);
                }
            }

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(response); }
        }

        protected Map<String, String> parseForm(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = new HashMap<>();
            for (String pair : body.split("&")) {
                String[] parts = pair.split("=");
                if (parts.length > 1) params.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
            }
            return params;
        }

        protected Map<String, String> parseQuery(String query) {
            Map<String, String> params = new HashMap<>();
            if (query == null) return params;
            for (String pair : query.split("&")) {
                String[] parts = pair.split("=");
                if (parts.length > 1) params.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
            }
            return params;
        }

        protected void redirect(HttpExchange exchange, String path) throws IOException {
            exchange.getResponseHeaders().set("Location", path);
            exchange.sendResponseHeaders(302, -1);
        }
    }

    // --- Handlers Específicos ---

    static class IndexHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException { render(exchange, "index.html", new HashMap<>()); }
    }

    static class NovaSolicitacaoHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, Object> data = new HashMap<>();
                data.put("categoriasOptions", "<option value=\"Iluminação Pública\">Iluminação Pública</option><option value=\"Limpeza Urbana\">Limpeza Urbana</option><option value=\"Buraco na Rua\">Buraco na Rua</option><option value=\"Outros\">Outros</option>");
                render(exchange, "nova-solicitacao.html", data);
            } else {
                Map<String, String> form = parseForm(exchange);
                Solicitacao s = servicoSolicitacoes.criarSolicitacao(form.get("categoria"), form.get("descricao"), form.get("localizacao"), "on".equals(form.get("anonima")), form.get("nomeCidadao"), form.get("contatoCidadao"));
                redirect(exchange, "/solicitacoes/confirmacao?protocolo=" + s.getProtocolo());
            }
        }
    }

    static class ConfirmacaoSolicitacaoHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            Map<String, Object> data = new HashMap<>();
            data.put("protocolo", query.getOrDefault("protocolo", "ERRO"));
            render(exchange, "confirmacao-solicitacao.html", data);
        }
    }

    static class ConsultarSolicitacaoHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, Object> data = new HashMap<>();
                data.put("mensagemErro", "");
                render(exchange, "consultar-solicitacao.html", data);
            } else {
                Map<String, String> form = parseForm(exchange);
                String p = form.get("protocolo");
                if (servicoSolicitacoes.buscarPorProtocolo(p).isPresent()) redirect(exchange, "/solicitacoes/detalhes?protocolo=" + p);
                else render(exchange, "consultar-solicitacao.html", Map.of("mensagemErro", "<div class='alert alert-danger'>Protocolo não encontrado.</div>"));
            }
        }
    }

    static class DetalhesSolicitacaoHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String p = parseQuery(exchange.getRequestURI().getQuery()).get("protocolo");
            Optional<Solicitacao> sOpt = servicoSolicitacoes.buscarPorProtocolo(p);
            if (sOpt.isPresent()) {
                Solicitacao s = sOpt.get();
                Map<String, Object> data = new HashMap<>();
                data.put("solicitacao.protocolo", s.getProtocolo());
                data.put("solicitacao.categoria", s.getCategoria());
                data.put("solicitacao.status", s.getStatusAtual());
                data.put("solicitacao.localizacao", s.getLocalizacao());
                data.put("solicitacao.formattedDataCriacao", s.getFormattedDataCriacao());
                data.put("solicitacao.descricao", s.getDescricao());
                data.put("solicitacao.nomeCidadao", s.getNomeCidadao());
                data.put("solicitacao.contatoCidadao", s.getContatoCidadao());
                data.put("if solicitacao.anonima == false", s.isEhAnonima() ? "<!--" : "");
                data.put("endif", s.isEhAnonima() ? "-->" : "");
                
                StringBuilder hist = new StringBuilder();
                for (var h : s.getHistoricoMovimentacoes()) {
                    hist.append("<div class='mb-2'><b>").append(h.getFormattedDataHora()).append("</b>: ").append(h.getStatusAtual()).append(" - ").append(h.getComentario()).append("</div>");
                }
                data.put("solicitacao.historicoHtml", hist.toString());
                render(exchange, "detalhes-solicitacao.html", data);
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }
    }

    static class PainelGestorHandler extends BaseHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                List<Solicitacao> lista = servicoSolicitacoes.listarTodasSolicitacoes();
                Map<String, Object> data = new HashMap<>();
                data.put("solicitacoesCount", lista.size());
                data.put("categoriasOptions", "");
                data.put("statusOptions", "");
                data.put("statusOptionsModal", "<option value='TRIAGEM'>TRIAGEM</option><option value='EM_EXECUCAO'>EM EXECUÇÃO</option><option value='RESOLVIDO'>RESOLVIDO</option>");
                data.put("localizacaoFilter", "");
                
                StringBuilder table = new StringBuilder("<table class='table'><thead><tr><th>Protocolo</th><th>Status</th><th>Ações</th></tr></thead><tbody>");
                for (Solicitacao s : lista) {
                    table.append("<tr><td>").append(s.getProtocolo()).append("</td><td>").append(s.getStatusAtual()).append("</td>")
                         .append("<td><button class='btn btn-sm btn-warning' onclick=\"preencherModal('").append(s.getProtocolo()).append("')\">Atualizar</button></td></tr>");
                }
                table.append("</tbody></table>");
                data.put("solicitacoesTableHtml", table.toString());
                render(exchange, "painel-atendente.html", data);
            } else {
                Map<String, String> form = parseForm(exchange);
                servicoSolicitacoes.atualizarStatusSolicitacao(form.get("protocolo"), StatusSolicitacao.valueOf(form.get("novoStatus")), form.get("comentario"), "Gestor");
                redirect(exchange, "/solicitacoes/painel");
            }
        }
    }
}
