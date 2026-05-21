-- Prazos processuais de demonstração — Attus
INSERT INTO prazo_processual (
    id, numero_processo, descricao, data_vencimento, status,
    data_criacao, data_atualizacao, versao, excluido
) VALUES
(
    'a1000001-0001-4001-8001-000000000001',
    '0001234-56.2024.8.26.0100',
    'Prazo para apresentação de contestação na ação de cobrança',
    CURRENT_DATE + INTERVAL '15 days',
    'PENDENTE',
    NOW(), NOW(), 0, FALSE
),
(
    'a1000001-0001-4001-8001-000000000002',
    '0005678-12.2023.4.01.3400',
    'Manifestação sobre laudo pericial complementar do processo',
    CURRENT_DATE,
    'PENDENTE',
    NOW(), NOW(), 0, FALSE
),
(
    'a1000001-0001-4001-8001-000000000003',
    '1009876-54.2022.8.26.0053',
    'Recurso de apelação — prazo vencido aguardando providências',
    CURRENT_DATE - INTERVAL '3 days',
    'PENDENTE',
    NOW(), NOW(), 0, FALSE
),
(
    'a1000001-0001-4001-8001-000000000004',
    '5012345-67.2021.8.26.0001',
    'Impugnação ao cumprimento de sentença — concluída pelo procurador',
    CURRENT_DATE + INTERVAL '30 days',
    'CONCLUIDO',
    NOW(), NOW(), 0, FALSE
),
(
    'a1000001-0001-4001-8001-000000000005',
    '7004321-89.2020.8.26.0100',
    'Embargos de declaração — prazo cancelado por acordo homologado',
    CURRENT_DATE + INTERVAL '7 days',
    'CANCELADO',
    NOW(), NOW(), 0, FALSE
),
(
    'a1000001-0001-4001-8001-000000000006',
    '2008765-43.2024.8.26.0300',
    'Contrarrazões ao recurso especial interposto pela parte contrária',
    CURRENT_DATE + INTERVAL '5 days',
    'PENDENTE',
    NOW(), NOW(), 0, FALSE
),
(
    'a1000001-0001-4001-8001-000000000007',
    '3001122-33.2024.8.26.0600',
    'Réplica à contestação — fase de conhecimento do procedimento',
    CURRENT_DATE + INTERVAL '20 days',
    'PENDENTE',
    NOW(), NOW(), 0, FALSE
)
ON CONFLICT (id) DO NOTHING;
