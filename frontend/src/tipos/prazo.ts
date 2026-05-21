export type StatusPrazo = 'PENDENTE' | 'CONCLUIDO' | 'CANCELADO';

export interface PrazoProcessual {
  id: string;
  numeroProcesso: string;
  descricao: string;
  dataVencimento: string;
  status: StatusPrazo;
  dataCriacao: string;
  dataAtualizacao: string;
  versao: number;
  vencidoOuVenceHoje: boolean;
}

export interface PaginaPrazos {
  content: PrazoProcessual[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface CriarPrazoRequisicao {
  numeroProcesso: string;
  descricao: string;
  dataVencimento: string;
}

export interface AtualizarPrazoRequisicao extends CriarPrazoRequisicao {
  status: StatusPrazo;
  versao: number;
}

export interface ProblemaApi {
  title?: string;
  detail?: string;
  status?: number;
  erros?: Record<string, string>;
}
