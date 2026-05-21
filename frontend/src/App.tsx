import { useCallback, useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import ModalPrazo from './componentes/ModalPrazo';
import TabelaPrazos from './componentes/TabelaPrazos';
import { excluirPrazo, extrairMensagemErro, listarPrazos } from './servicos/apiPrazos';
import type { PrazoProcessual } from './tipos/prazo';

export default function App() {
  const [prazos, setPrazos] = useState<PrazoProcessual[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [modalAberto, setModalAberto] = useState(false);
  const [prazoEdicao, setPrazoEdicao] = useState<PrazoProcessual | null>(null);
  const [pagina, setPagina] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(0);

  const carregarPrazos = useCallback(async () => {
    setCarregando(true);
    try {
      const resposta = await listarPrazos(pagina);
      setPrazos(resposta.content);
      setTotalPaginas(resposta.totalPages);
    } catch (erro) {
      toast.error(extrairMensagemErro(erro));
    } finally {
      setCarregando(false);
    }
  }, [pagina]);

  useEffect(() => {
    carregarPrazos();
  }, [carregarPrazos]);

  const abrirNovo = () => {
    setPrazoEdicao(null);
    setModalAberto(true);
  };

  const abrirEdicao = (prazo: PrazoProcessual) => {
    setPrazoEdicao(prazo);
    setModalAberto(true);
  };

  const confirmarExclusao = async (prazo: PrazoProcessual) => {
    if (!window.confirm(`Excluir o prazo do processo ${prazo.numeroProcesso}?`)) return;
    try {
      await excluirPrazo(prazo.id);
      toast.success('Prazo excluído com sucesso.');
      carregarPrazos();
    } catch (erro) {
      toast.error(extrairMensagemErro(erro));
    }
  };

  return (
    <div className="min-h-screen">
      <header className="border-b border-slate-200 bg-white shadow-sm">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-5">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">
              Gerenciador de Prazos Processuais
            </h1>
            <p className="text-sm text-slate-500">Attus Procuradoria Digital</p>
          </div>
          <button
            onClick={abrirNovo}
            className="rounded-lg bg-blue-700 px-4 py-2 text-sm font-medium text-white hover:bg-blue-800"
          >
            Novo prazo
          </button>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-8">
        {carregando ? (
          <p className="text-slate-500">Carregando prazos...</p>
        ) : (
          <>
            <TabelaPrazos
              prazos={prazos}
              onEditar={abrirEdicao}
              onExcluir={confirmarExclusao}
            />
            {totalPaginas > 1 && (
              <div className="mt-4 flex items-center justify-center gap-3">
                <button
                  disabled={pagina === 0}
                  onClick={() => setPagina((p) => p - 1)}
                  className="rounded border px-3 py-1 disabled:opacity-40"
                >
                  Anterior
                </button>
                <span className="text-sm text-slate-600">
                  Página {pagina + 1} de {totalPaginas}
                </span>
                <button
                  disabled={pagina >= totalPaginas - 1}
                  onClick={() => setPagina((p) => p + 1)}
                  className="rounded border px-3 py-1 disabled:opacity-40"
                >
                  Próxima
                </button>
              </div>
            )}
          </>
        )}
      </main>

      <ModalPrazo
        aberto={modalAberto}
        prazoEdicao={prazoEdicao}
        onFechar={() => setModalAberto(false)}
        onSalvo={carregarPrazos}
      />
    </div>
  );
}
