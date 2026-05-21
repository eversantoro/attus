import axios, { AxiosError } from 'axios';
import type {
  AtualizarPrazoRequisicao,
  CriarPrazoRequisicao,
  PaginaPrazos,
  PrazoProcessual,
  ProblemaApi,
} from '../tipos/prazo';

const api = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

export function extrairMensagemErro(erro: unknown): string {
  if (axios.isAxiosError(erro)) {
    const problema = (erro as AxiosError<ProblemaApi>).response?.data;
    if (problema?.detail) return problema.detail;
    if (problema?.title) return problema.title;
    if (problema?.erros) {
      return Object.values(problema.erros).join(' ');
    }
  }
  return 'Ocorreu um erro inesperado. Tente novamente.';
}

export async function listarPrazos(pagina = 0, tamanho = 10): Promise<PaginaPrazos> {
  const { data } = await api.get<PaginaPrazos>('/prazos', {
    params: { page: pagina, size: tamanho, sort: 'dataVencimento,asc' },
  });
  return data;
}

export async function buscarPrazo(id: string): Promise<PrazoProcessual> {
  const { data } = await api.get<PrazoProcessual>(`/prazos/${id}`);
  return data;
}

export async function criarPrazo(requisicao: CriarPrazoRequisicao): Promise<PrazoProcessual> {
  const { data } = await api.post<PrazoProcessual>('/prazos', requisicao);
  return data;
}

export async function atualizarPrazo(
  id: string,
  requisicao: AtualizarPrazoRequisicao,
): Promise<PrazoProcessual> {
  const { data } = await api.put<PrazoProcessual>(`/prazos/${id}`, requisicao);
  return data;
}

export async function excluirPrazo(id: string): Promise<void> {
  await api.delete(`/prazos/${id}`);
}
