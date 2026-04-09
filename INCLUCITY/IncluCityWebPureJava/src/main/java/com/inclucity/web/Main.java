package com.inclucity.web;

import com.inclucity.web.model.Solicitacao;
import com.inclucity.web.model.StatusSolicitacao;
import com.inclucity.web.service.ServicoSolicitacoes;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class Main {

    private static final ServicoSolicitacoes servico = new ServicoSolicitacoes();
    private static final int PORT = 8080;
    private static final String STATIC_DIR = "src/main/resources/static";
    private static final String TEMPLATES_DIR = "src/main/resources/templates";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Contextos para arquivos estáticos (CSS, JS)
        server.createContext("/css", new StaticFileHandler(STATIC_DIR + "/css"));
        server.createContext("/js", new StaticFileHandler(STATIC_DIR + "/js"));

        // Contextos para páginas HTML
        server.createContext("/", new IndexHandler());
        server.createContext("/solicitacoes/nova", new NovaSolicitacaoHandler());
        server.createContext("/solicitacoes/confirmacao", new ConfirmacaoSolicitacaoHandler());
        server.createContext("/solicitacoes/consultar", new ConsultarSolicitacaoHandler());
        server.createContext("/solicitacoes/detalhes", new DetalhesSolicitacaoHandler());
        server.createContext("/solicitacoes/painel", new PainelGestorHandler());

        server.setExecutor(Executors.newFixedThreadPool(10)); // Cria um pool de threads
        server.start();
        System.out.println("Servidor iniciado na porta " + PORT);
        System.out.println("Acesse: http://localhost:" + PORT);
    }

    // Handler para servir arquivos estáticos
    static class StaticFileHandler implements com.sun.net.httpserver.HttpHandler {
        private final String baseDir;

        public StaticFileHandler(String baseDir) {
            this.baseDir = baseDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            Path filePath = Paths.get(baseDir, requestPath.substring(requestPath.indexOf("/", 1)));

            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    if (requestPath.endsWith(".css")) contentType = "text/css";
                    else if (requestPath.endsWith(".js")) contentType = "application/javascript";
                    else contentType = "application/octet-stream";
                }
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, Files.size(filePath));
                try (OutputStream os = exchange.getResponseBody()) {
                    Files.copy(filePath, os);
                }
            } else {
                sendNotFound(exchange);
            }
        }
    }

    // Base Handler para páginas HTML
    static abstract class HtmlHandler implements com.sun.net.httpserver.HttpHandler {
        protected void sendHtmlResponse(HttpExchange exchange, String templateName, Map<String, Object> data) throws IOException {
            Path templatePath = Paths.get(TEMPLATES_DIR, templateName);
            if (!Files.exists(templatePath)) {
                sendNotFound(exchange);
                return;
            }

            String htmlContent = new String(Files.readAllBytes(templatePath), StandardCharsets.UTF_8);

            // Substituir placeholders simples (simulando Thymeleaf básico)
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                htmlContent = htmlContent.replace("[[${" + entry.getKey() + "}]]", String.valueOf(entry.getValue()));
                // Para listas, um tratamento mais complexo seria necessário, ou passar como JSON e renderizar com JS
            }

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, htmlContent.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(htmlContent.getBytes(StandardCharsets.UTF_8));
            }
        }

        protected Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
            Map<String, String> formData = new HashMap<>();
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String[] pairs = requestBody.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                    formData.put(key, value);
                }
            }
            return formData;
        }

        protected void redirectTo(HttpExchange exchange, String path) throws IOException {
            exchange.getResponseHeaders().set("Location", path);
            exchange.sendResponseHeaders(302, -1);
        }
    }

    static class IndexHandler extends HtmlHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                sendHtmlResponse(exchange, "index.html", new HashMap<>());
            } else {
                sendMethodNotAllowed(exchange);
            }
        }
    }

    static class NovaSolicitacaoHandler extends HtmlHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, Object> data = new HashMap<>();
                data.put("categorias", servico.getCategoriasDisponiveis());
                sendHtmlResponse(exchange, "nova-solicitacao.html", data);
            } else if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> formData = parseFormData(exchange);
                String categoria = formData.get("categoria");
                String descricao = formData.get("descricao");
                String localizacao = formData.get("localizacao");
                boolean anonima = "on".equals(formData.get("anonima"));
                String nomeCidadao = anonima ? "Anônimo" : formData.get("nomeCidadao");
                String contatoCidadao = anonima ? "N/A" : formData.get("contatoCidadao");

                try {
                    Solicitacao novaSolicitacao = servico.criarSolicitacao(categoria, descricao, localizacao, anonima, nomeCidadao, contatoCidadao);
                    redirectTo(exchange, "/solicitacoes/confirmacao?protocolo=" + novaSolicitacao.getProtocolo());
                } catch (IllegalArgumentException e) {
                    // Em um projeto real, você renderizaria a página com a mensagem de erro
                    sendBadRequest(exchange, e.getMessage());
                }
            } else {
                sendMethodNotAllowed(exchange);
            }
        }
    }

    static class ConfirmacaoSolicitacaoHandler extends HtmlHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
                String protocolo = params.get("protocolo");
                Map<String, Object> data = new HashMap<>();
                data.put("protocolo", protocolo != null ? protocolo : "N/A");
                sendHtmlResponse(exchange, "confirmacao-solicitacao.html", data);
            } else {
                sendMethodNotAllowed(exchange);
            }
        }
    }

    static class ConsultarSolicitacaoHandler extends HtmlHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, Object> data = new HashMap<>();
                Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
                if (params.containsKey("mensagemErro")) {
                    data.put("mensagemErro", params.get("mensagemErro"));
                }
                sendHtmlResponse(exchange, "consultar-solicitacao.html", data);
            } else if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> formData = parseFormData(exchange);
                String protocolo = formData.get("protocolo");
                Optional<Solicitacao> solicitacaoOpt = servico.buscarSolicitacaoPorProtocolo(protocolo);

                if (solicitacaoOpt.isPresent()) {
                    redirectTo(exchange, "/solicitacoes/detalhes?protocolo=" + protocolo);
                } else {
                    redirectTo(exchange, "/solicitacoes/consultar?mensagemErro=" + URLDecoder.decode("Solicitação com protocolo " + protocolo + " não encontrada.", StandardCharsets.UTF_8));
                }
            } else {
                sendMethodNotAllowed(exchange);
            }
        }
    }

    static class DetalhesSolicitacaoHandler extends HtmlHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
                String protocolo = params.get("protocolo");
                Optional<Solicitacao> solicitacaoOpt = servico.buscarSolicitacaoPorProtocolo(protocolo);

                if (solicitacaoOpt.isPresent()) {
                    Solicitacao solicitacao = solicitacaoOpt.get();
                    Map<String, Object> data = new HashMap<>();
                    data.put("solicitacao.protocolo", solicitacao.getProtocolo());
                    data.put("solicitacao.categoria", solicitacao.getCategoria());
                    data.put("solicitacao.status", solicitacao.getStatus());
                    data.put("solicitacao.localizacao", solicitacao.getLocalizacao());
                    data.put("solicitacao.formattedDataCriacao", solicitacao.getFormattedDataCriacao());
                    data.put("solicitacao.descricao", solicitacao.getDescricao());
                    data.put("solicitacao.anonima", solicitacao.isAnonima());
                    data.put("solicitacao.nomeCidadao", solicitacao.getNomeCidadao());
                    data.put("solicitacao.contatoCidadao", solicitacao.getContatoCidadao());

                    // Para o histórico, vamos simplificar para a primeira versão
                    StringBuilder historicoHtml = new StringBuilder();
                    if (solicitacao.getHistorico().isEmpty()) {
                        historicoHtml.append("<div class=\"alert alert-warning\" role=\"alert\">Aguardando a primeira movimentação do gestor.</div>");
                    } else {
                        for (com.inclucity.web.model.HistoricoStatus evento : solicitacao.getHistorico()) {
                            historicoHtml.append("<div class=\"mb-4 pb-3 border-bottom\">");
                            historicoHtml.append("<p class=\"text-muted mb-2\"><b>Data:</b> ").append(evento.getFormattedDataHora()).append("</p>");
                            historicoHtml.append("<p class=\"mb-2\"><b>Status:</b> ").append(evento.getStatusAnterior() != null ? evento.getStatusAnterior() + " -> " : "").append(evento.getStatusAtual()).append("</p>");
                            historicoHtml.append("<p class=\"mb-2\"><b>Comentário:</b> ").append(evento.getComentario()).append("</p>");
                            historicoHtml.append("<p class=\"mb-0\"><b>Responsável:</b> ").append(evento.getResponsavel()).append("</p>");
                            historicoHtml.append("</div>");
                        }
                    }
                    data.put("solicitacao.historicoHtml", historicoHtml.toString());

                    sendHtmlResponse(exchange, "detalhes-solicitacao.html", data);
                } else {
                    sendNotFound(exchange);
                }
            } else {
                sendMethodNotAllowed(exchange);
            }
        }
    }

    static class PainelGestorHandler extends HtmlHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
                String categoriaFilter = params.getOrDefault("categoria", "");
                String localizacaoFilter = params.getOrDefault("localizacao", "");
                StatusSolicitacao statusFilter = null;
                if (params.containsKey("status") && !params.get("status").isEmpty()) {
                    try {
                        statusFilter = StatusSolicitacao.valueOf(params.get("status").toUpperCase());
                    } catch (IllegalArgumentException e) {
                        // Ignorar status inválido
                    }
                }

                List<Solicitacao> solicitacoes = servico.filtrarSolicitacoes(categoriaFilter, localizacaoFilter, statusFilter);
                Map<String, Object> data = new HashMap<>();
                data.put("categorias", servico.getCategoriasDisponiveis());
                data.put("statusOptions", servico.getStatusDisponiveis());
                data.put("solicitacoes", solicitacoes);

                // Renderizar a tabela de solicitações manualmente para o placeholder
                StringBuilder solicitacoesTableHtml = new StringBuilder();
                if (solicitacoes.isEmpty()) {
                    solicitacoesTableHtml.append("<div class=\"alert alert-info\" role=\"alert\">Nenhuma solicitação encontrada com os filtros selecionados.</div>");
                } else {
                    solicitacoesTableHtml.append("<div class=\"table-responsive\"><table class=\"table table-hover\"><thead><tr><th>Protocolo</th><th>Categoria</th><th>Localização</th><th>Status</th><th>Data</th><th>Ações</th></tr></thead><tbody>");
                    for (Solicitacao sol : solicitacoes) {
                        solicitacoesTableHtml.append("<tr>");
                        solicitacoesTableHtml.append("<td><b>").append(sol.getProtocolo()).append("</b></td>");
                        solicitacoesTableHtml.append("<td>").append(sol.getCategoria()).append("</td>");
                        solicitacoesTableHtml.append("<td>").append(sol.getLocalizacao()).append("</td>");
                        solicitacoesTableHtml.append("<td><span class=\"badge bg-info\"> ").append(sol.getStatus()).append("</span></td>");
                        solicitacoesTableHtml.append("<td>").append(sol.getFormattedDataCriacao()).append("</td>");
                        solicitacoesTableHtml.append("<td><button class=\"btn btn-sm btn-warning\" data-bs-toggle=\"modal\" data-bs-target=\"#modalAtualizar\" onclick=\"preencherModal(\'").append(sol.getProtocolo()).append("\', \'").append(sol.getStatus()).append("\')\">Atualizar</button></td>");
                        solicitacoesTableHtml.append("</tr>");
                    }
                    solicitacoesTableHtml.append("</tbody></table></div>");
                }
                data.put("solicitacoesTableHtml", solicitacoesTableHtml.toString());

                sendHtmlResponse(exchange, "painel-atendente.html", data);
            } else if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> formData = parseFormData(exchange);
                String protocolo = formData.get("protocolo");
                StatusSolicitacao novoStatus = StatusSolicitacao.valueOf(formData.get("novoStatus").toUpperCase());
                String comentario = formData.get("comentario");
                String responsavel = "Gestor"; // Simplificado para o MVP

                try {
                    servico.atualizarStatusSolicitacao(protocolo, novoStatus, comentario, responsavel);
                    redirectTo(exchange, "/solicitacoes/painel");
                } catch (IllegalArgumentException e) {
                    sendBadRequest(exchange, e.getMessage());
                }
            } else {
                sendMethodNotAllowed(exchange);
            }
        }
    }

    // Métodos auxiliares para o HttpServer
    private static void sendNotFound(HttpExchange exchange) throws IOException {
        String response = "404 Not Found";
        exchange.sendResponseHeaders(404, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private static void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        String response = "405 Method Not Allowed";
        exchange.sendResponseHeaders(405, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private static void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        String response = "400 Bad Request: " + message;
        exchange.sendResponseHeaders(400, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return result;
        }
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(URLDecoder.decode(entry[0], StandardCharsets.UTF_8), URLDecoder.decode(entry[1], StandardCharsets.UTF_8));
            } else {
                result.put(URLDecoder.decode(entry[0], StandardCharsets.UTF_8), "");
            }
        }
        return result;
    }
}
