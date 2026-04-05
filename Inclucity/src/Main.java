import enums.Status;
import models.Solicitacao;
import services.ServicoSolicitacoes;

import java.util.ArrayList;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ServicoSolicitacoes service = new ServicoSolicitacoes();

        int opcao;


        System.out.println("\n===== SISTEMA DE SOLICITAÇÕES =====");
        do{
            System.out.println(" \n 1 - Cria solicitação ");
            System.out.println(" 2 - Listar solicitações ");
            System.out.println(" 3 - Buscar por Protocolo ");
            System.out.println(" 4 - Atualizar Status ");
            System.out.println(" 5 - Adicionar Comentário ");
            System.out.println(" 0 - Sair ");

            opcao = sc.nextInt();
            sc.nextLine();

            switch (opcao){
                case 1:
                    System.out.println(" Categoria: ");
                    String categoria = sc.nextLine();

                    System.out.println(" Descrição: ");
                    String descricao = sc.nextLine();

                    System.out.println(" Bairro: ");
                    String bairro = sc.nextLine();

                    System.out.println(" Anonimo (true/false): ");
                    boolean anonimo = sc.nextBoolean();

                    Solicitacao s = service.criarSolicitacao(categoria, descricao, bairro, anonimo);
                    System.out.println(" Criando com protocolo: " + s.getProtocolo());
                    break;

                case 2:
                    service.listarSolicitacoes().forEach(System.out::println);
                    break;

                case 3:
                    System.out.println(" Protocolo ");
                    int protocoloBusca = sc.nextInt();

                    System.out.println(service.buscarPorProtocolo(protocoloBusca));
                    break;

                case 4:
                    System.out.println(" Protocolo ");
                    int protocoloStatus = sc.nextInt();

                    System.out.println(" Novo Status: ");
                    System.out.println(" 1- TRIAGEM | 2- EM_EXECUÇÃO | 3- RESOLVIDO | 4- ENCERRADO ");

                    int escolha = sc.nextInt();

                    Status novoStatus = Status.values()[escolha];

                    if (service.atualizarStatus(protocoloStatus, novoStatus)){
                        System.out.println(" Status atualizado ");
                    } else {
                        System.out.println(" Erro ao atualizar ");
                    }
                break;

                case 5:
                    System.out.println(" Protocolo: ");
                    int protocloComentario = sc.nextInt();
                    sc.nextLine();

                    System.out.println("Comentário: ");
                    String comentario = sc.nextLine();

                    service.adicionarComentario(protocloComentario, comentario);
                    break;
            }
        }while(opcao != 0);
        sc.close();
    }
}