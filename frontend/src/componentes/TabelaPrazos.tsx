import type { PrazoProcessual } from '../tipos/prazo';
import { formatarDataBr } from '../util/data';

interface Props {
  prazos: PrazoProcessual[];
  onEditar: (prazo: PrazoProcessual) => void;
  onExcluir: (prazo: PrazoProcessual) => void;
}

function classeLinha(prazo: PrazoProcessual): string {
  if (prazo.vencidoOuVenceHoje) {
    return 'bg-red-50 hover:bg-red-100';
  }
  return 'hover:bg-slate-50';
}

function badgeStatus(status: string): string {
  switch (status) {
    case 'CONCLUIDO':
      return 'bg-green-100 text-green-800';
    case 'CANCELADO':
      return 'bg-slate-200 text-slate-700';
    default:
      return 'bg-amber-100 text-amber-800';
  }
}

export default function TabelaPrazos({ prazos, onEditar, onExcluir }: Props) {
  if (prazos.length === 0) {
    return (
      <p className="rounded-lg border border-dashed border-slate-300 p-8 text-center text-slate-500">
        Nenhum prazo cadastrado. Clique em &quot;Novo prazo&quot; para começar.
      </p>
    );
  }

  return (
    <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
      <table className="min-w-full text-left text-sm">
        <thead className="border-b border-slate-200 bg-slate-100 text-slate-600">
          <tr>
            <th className="px-4 py-3">Processo (CNJ)</th>
            <th className="px-4 py-3">Descrição</th>
            <th className="px-4 py-3">Vencimento</th>
            <th className="px-4 py-3">Status</th>
            <th className="px-4 py-3 text-right">Ações</th>
          </tr>
        </thead>
        <tbody>
          {prazos.map((prazo) => (
            <tr key={prazo.id} className={`border-b border-slate-100 ${classeLinha(prazo)}`}>
              <td className="px-4 py-3 font-mono text-xs">{prazo.numeroProcesso}</td>
              <td className="px-4 py-3 max-w-xs truncate" title={prazo.descricao}>
                {prazo.descricao}
              </td>
              <td className="px-4 py-3">
                <span className={prazo.vencidoOuVenceHoje ? 'font-semibold text-red-700' : ''}>
                  {formatarDataBr(prazo.dataVencimento)}
                  {prazo.vencidoOuVenceHoje && (
                    <span className="ml-2 text-xs text-red-600">(vencido/hoje)</span>
                  )}
                </span>
              </td>
              <td className="px-4 py-3">
                <span className={`rounded-full px-2 py-1 text-xs font-medium ${badgeStatus(prazo.status)}`}>
                  {prazo.status}
                </span>
              </td>
              <td className="px-4 py-3 text-right space-x-2">
                <button
                  onClick={() => onEditar(prazo)}
                  className="text-blue-700 hover:underline"
                >
                  Editar
                </button>
                <button
                  onClick={() => onExcluir(prazo)}
                  className="text-red-700 hover:underline"
                >
                  Excluir
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
