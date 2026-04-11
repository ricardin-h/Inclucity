package com.inclucity.web.model;

/**
 * Define os possíveis estados de uma solicitação no sistema IncluCity.
 * Representa o ciclo de vida completo, desde a abertura até o fechamento.
 */
public enum StatusSolicitacao {
    ABERTO,
    TRIAGEM,
    EM_EXECUCAO,
    RESOLVIDO,
    ENCERRADO
}
