import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import ModalPrazo from '../componentes/ModalPrazo';
import * as api from '../servicos/apiPrazos';

vi.mock('../servicos/apiPrazos', () => ({
  criarPrazo: vi.fn(),
  atualizarPrazo: vi.fn(),
  extrairMensagemErro: vi.fn(),
}));

describe('ModalPrazo', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('deve renderizar campos do formulário de criação', () => {
    render(
      <ModalPrazo aberto={true} prazoEdicao={null} onFechar={() => {}} onSalvo={() => {}} />,
    );
    expect(screen.getByText('Novo prazo processual')).toBeInTheDocument();
    expect(screen.getByLabelText(/Número do processo/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Descrição/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Data de vencimento/i)).toBeInTheDocument();
  });

  it('deve bloquear submissão com CNJ inválido', async () => {
    const usuario = userEvent.setup();
    render(
      <ModalPrazo aberto={true} prazoEdicao={null} onFechar={() => {}} onSalvo={() => {}} />,
    );

    await usuario.type(screen.getByLabelText(/Número do processo/i), '123');
    await usuario.type(screen.getByLabelText(/Descrição/i), 'Descrição válida de teste');
    const amanha = new Date();
    amanha.setDate(amanha.getDate() + 1);
    const data = amanha.toISOString().split('T')[0];
    await usuario.type(screen.getByLabelText(/Data de vencimento/i), data);
    await usuario.click(screen.getByRole('button', { name: /Salvar/i }));

    await waitFor(() => {
      expect(screen.getByText(/Formato CNJ inválido/i)).toBeInTheDocument();
    });
    expect(api.criarPrazo).not.toHaveBeenCalled();
  });

  it('deve chamar API ao submeter formulário válido', async () => {
    vi.mocked(api.criarPrazo).mockResolvedValue({
      id: '1',
      numeroProcesso: '0001234-56.2024.8.26.0100',
      descricao: 'Prazo de teste automatizado',
      dataVencimento: '2030-01-15',
      status: 'PENDENTE',
      dataCriacao: '',
      dataAtualizacao: '',
      versao: 0,
      vencidoOuVenceHoje: false,
    });

    const usuario = userEvent.setup();
    const onSalvo = vi.fn();
    render(
      <ModalPrazo aberto={true} prazoEdicao={null} onFechar={() => {}} onSalvo={onSalvo} />,
    );

    await usuario.type(
      screen.getByLabelText(/Número do processo/i),
      '0001234-56.2024.8.26.0100',
    );
    await usuario.type(
      screen.getByLabelText(/Descrição/i),
      'Prazo de teste automatizado',
    );
    await usuario.type(screen.getByLabelText(/Data de vencimento/i), '2030-01-15');
    await usuario.click(screen.getByRole('button', { name: /Salvar/i }));

    await waitFor(() => {
      expect(api.criarPrazo).toHaveBeenCalledWith({
        numeroProcesso: '0001234-56.2024.8.26.0100',
        descricao: 'Prazo de teste automatizado',
        dataVencimento: '2030-01-15',
      });
    });
  });
});
