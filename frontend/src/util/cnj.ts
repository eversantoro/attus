const PADRAO_CNJ = /^\d{7}-\d{2}\.\d{4}\.\d\.\d{2}\.\d{4}$/;

export function aplicarMascaraCnj(valor: string): string {
  const digitos = valor.replace(/\D/g, '').slice(0, 20);
  let resultado = '';

  for (let i = 0; i < digitos.length; i++) {
    const d = digitos[i];
    if (i < 7) {
      resultado += d;
      if (i === 6) resultado += '-';
    } else if (i < 9) {
      resultado += d;
      if (i === 8) resultado += '.';
    } else if (i < 13) {
      resultado += d;
      if (i === 12) resultado += '.';
    } else if (i === 13) {
      resultado += d + '.';
    } else if (i < 16) {
      resultado += d;
      if (i === 15) resultado += '.';
    } else {
      resultado += d;
    }
  }

  return resultado;
}

export function numeroCnjEhValido(valor: string): boolean {
  return PADRAO_CNJ.test(valor.trim());
}
