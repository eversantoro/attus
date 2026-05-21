import { describe, expect, it } from 'vitest';
import { aplicarMascaraCnj, numeroCnjEhValido } from '../util/cnj';

describe('util CNJ', () => {
  it('deve validar número CNJ correto', () => {
    expect(numeroCnjEhValido('0001234-56.2024.8.26.0100')).toBe(true);
  });

  it('deve aplicar máscara progressiva', () => {
    expect(aplicarMascaraCnj('00012345620248260100')).toContain('-');
  });
});
