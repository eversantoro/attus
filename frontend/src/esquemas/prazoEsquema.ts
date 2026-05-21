import { z } from 'zod';
import { numeroCnjEhValido } from '../util/cnj';
import { dataNaoEhRetroativa } from '../util/data';

export const esquemaPrazo = z.object({
  numeroProcesso: z
    .string()
    .min(1, 'Número do processo é obrigatório')
    .refine(numeroCnjEhValido, {
      message: 'Formato CNJ inválido (NNNNNNN-DD.AAAA.J.TR.OOOO)',
    }),
  descricao: z
    .string()
    .min(10, 'Descrição deve ter no mínimo 10 caracteres')
    .max(255, 'Descrição deve ter no máximo 255 caracteres'),
  dataVencimento: z
    .string()
    .min(1, 'Data de vencimento é obrigatória')
    .refine(dataNaoEhRetroativa, {
      message: 'Data de vencimento não pode ser no passado',
    }),
  status: z.enum(['PENDENTE', 'CONCLUIDO', 'CANCELADO']).optional(),
});

export type FormularioPrazoValores = z.infer<typeof esquemaPrazo>;
