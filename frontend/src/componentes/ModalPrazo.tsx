import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { esquemaPrazo, type FormularioPrazoValores } from '../esquemas/prazoEsquema';
import {
  atualizarPrazo,
  criarPrazo,
  extrairMensagemErro,
} from '../servicos/apiPrazos';
import type { PrazoProcessual, StatusPrazo } from '../tipos/prazo';
import { aplicarMascaraCnj } from '../util/cnj';

interface Props {
  aberto: boolean;
  prazoEdicao: PrazoProcessual | null;
  onFechar: () => void;
  onSalvo: () => void;
}

const STATUS_OPCOES: StatusPrazo[] = ['PENDENTE', 'CONCLUIDO', 'CANCELADO'];

export default function ModalPrazo({ aberto, prazoEdicao, onFechar, onSalvo }: Props) {
  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<FormularioPrazoValores>({
    resolver: zodResolver(esquemaPrazo),
    defaultValues: {
      numeroProcesso: '',
      descricao: '',
      dataVencimento: '',
      status: 'PENDENTE',
    },
  });

  const numeroProcesso = watch('numeroProcesso');

  useEffect(() => {
    if (prazoEdicao) {
      reset({
        numeroProcesso: prazoEdicao.numeroProcesso,
        descricao: prazoEdicao.descricao,
        dataVencimento: prazoEdicao.dataVencimento,
        status: prazoEdicao.status,
      });
    } else {
      reset({
        numeroProcesso: '',
        descricao: '',
        dataVencimento: '',
        status: 'PENDENTE',
      });
    }
  }, [prazoEdicao, reset, aberto]);

  useEffect(() => {
    if (numeroProcesso !== undefined) {
      const mascarado = aplicarMascaraCnj(numeroProcesso ?? '');
      if (mascarado !== numeroProcesso) {
        setValue('numeroProcesso', mascarado, { shouldValidate: true });
      }
    }
  }, [numeroProcesso, setValue]);

  if (!aberto) return null;

  const onSubmit = async (valores: FormularioPrazoValores) => {
    try {
      if (prazoEdicao) {
        await atualizarPrazo(prazoEdicao.id, {
          numeroProcesso: valores.numeroProcesso,
          descricao: valores.descricao,
          dataVencimento: valores.dataVencimento,
          status: (valores.status ?? 'PENDENTE') as StatusPrazo,
          versao: prazoEdicao.versao,
        });
        toast.success('Prazo atualizado com sucesso.');
      } else {
        await criarPrazo({
          numeroProcesso: valores.numeroProcesso,
          descricao: valores.descricao,
          dataVencimento: valores.dataVencimento,
        });
        toast.success('Prazo criado com sucesso.');
      }
      onSalvo();
      onFechar();
    } catch (erro) {
      toast.error(extrairMensagemErro(erro));
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-lg rounded-xl bg-white p-6 shadow-xl">
        <h2 className="mb-4 text-xl font-semibold text-slate-800">
          {prazoEdicao ? 'Editar prazo' : 'Novo prazo processual'}
        </h2>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label htmlFor="numeroProcesso" className="mb-1 block text-sm font-medium">Número do processo (CNJ)</label>
            <input
              id="numeroProcesso"
              {...register('numeroProcesso')}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-blue-500 focus:outline-none"
              placeholder="0000000-00.0000.0.00.0000"
            />
            {errors.numeroProcesso && (
              <p className="mt-1 text-sm text-red-600">{errors.numeroProcesso.message}</p>
            )}
          </div>
          <div>
            <label htmlFor="descricao" className="mb-1 block text-sm font-medium">Descrição</label>
            <textarea
              id="descricao"
              {...register('descricao')}
              rows={3}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-blue-500 focus:outline-none"
            />
            {errors.descricao && (
              <p className="mt-1 text-sm text-red-600">{errors.descricao.message}</p>
            )}
          </div>
          <div>
            <label htmlFor="dataVencimento" className="mb-1 block text-sm font-medium">Data de vencimento</label>
            <input
              id="dataVencimento"
              type="date"
              {...register('dataVencimento')}
              min={new Date().toISOString().split('T')[0]}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-blue-500 focus:outline-none"
            />
            {errors.dataVencimento && (
              <p className="mt-1 text-sm text-red-600">{errors.dataVencimento.message}</p>
            )}
          </div>
          {prazoEdicao && (
            <div>
              <label className="mb-1 block text-sm font-medium">Status</label>
              <select
                {...register('status')}
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
              >
                {STATUS_OPCOES.map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
            </div>
          )}
          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onFechar}
              className="rounded-lg border border-slate-300 px-4 py-2 text-slate-700 hover:bg-slate-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="rounded-lg bg-blue-700 px-4 py-2 text-white hover:bg-blue-800 disabled:opacity-60"
            >
              {isSubmitting ? 'Salvando...' : 'Salvar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
