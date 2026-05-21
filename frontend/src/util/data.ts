export function dataNaoEhRetroativa(dataIso: string): boolean {
  if (!dataIso) return false;
  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);
  const informada = new Date(dataIso + 'T00:00:00');
  return informada >= hoje;
}

export function formatarDataBr(dataIso: string): string {
  const [ano, mes, dia] = dataIso.split('-');
  return `${dia}/${mes}/${ano}`;
}
