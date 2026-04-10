# IncluCity - Java Web Puro (Sem Frameworks) 🌳🌐

Este projeto é a versão definitiva do **IncluCity**, desenvolvida em **Java SE Puro**, utilizando o servidor HTTP nativo do Java (`com.sun.net.httpserver`). Ele oferece uma experiência Web completa (HTML/CSS/JS) sem a necessidade de frameworks pesados como Spring Boot.

## 📋 Funcionalidades e Diferenciais

- **Interface Web Completa:** Páginas HTML estilizadas com a paleta verde (sustentabilidade/natureza).
- **Backend Vanilla Java:** Servidor HTTP nativo que processa rotas, formulários e arquivos estáticos.
- **POO Avançada:** Uso de modelos, serviços e handlers para organizar o código.
- **Acessibilidade (ODS 11):** Foco em usabilidade para cidadãos e ferramentas de gestão eficientes.
- **Zero Dependências:** Não precisa de Maven, Gradle ou bibliotecas externas.

## 📁 Estrutura do Projeto

```
IncluCityWebPureJava/
├── src/
│   └── main/
│       ├── java/com/inclucity/web/
│       │   ├── model/          # Classes de dados
│       │   ├── service/        # Lógica de negócio
│       │   └── Main.java       # Servidor HTTP e Handlers
│       └── resources/
│           ├── static/         # CSS e JS
│           └── templates/      # Páginas HTML
└── README.md
```

## 🚀 Como Executar

### Pré-requisitos
- **Java JDK 8** ou superior instalado.

### Passos
1. Abra o terminal na pasta raiz do projeto (`IncluCityWebPureJava`).
2. Compile o projeto:
   ```bash
   javac -d bin src/main/java/com/inclucity/web/model/*.java src/main/java/com/inclucity/web/service/*.java src/main/java/com/inclucity/web/Main.java
   ```
3. Execute a aplicação:
   ```bash
   java -cp bin com.inclucity.web.Main
   ```
4. Acesse no seu navegador:
   - `http://localhost:8080`

## 🛠️ Notas Técnicas
- O servidor processa os arquivos HTML e substitui placeholders simples (ex: `[[${protocolo}]]`) por dados dinâmicos.
- O sistema utiliza persistência em memória (`ConcurrentHashMap`), ideal para demonstrações e MVPs.

---
**IncluCity - Tecnologia pura a serviço da cidadania e sustentabilidade.** 🤝🌳
