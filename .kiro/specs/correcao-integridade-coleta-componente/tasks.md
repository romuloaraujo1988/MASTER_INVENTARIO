# Tasks - Correção de Integridade Referencial

> ✅ **CORREÇÃO JÁ EXECUTADA EM PRODUÇÃO** — fechada em 31/05/2026
>
> Este era um fix pontual de banco (remoção de órfãos + FK + trigger + view +
> índices). Tudo já está refletido no schema de produção
> (`infrastructure/schema_original_producao.sql` e `sql/setup_banco_completo.sql`):
> - Função `validar_id_item_composto()` e trigger `trg_validar_id_item_composto`
> - Constraint `fk_coleta_componente_item_composto ... ON DELETE CASCADE`
> - View `vw_auditoria_integridade_coleta_componente`
> - Tabela de backup `backup_coleta_componente_20260211`
>
> As tarefas de processo pontual (janela de manutenção, monitoramento por 7
> dias, retenção de backup por 30 dias, notificações) já se esgotaram no tempo
> e foram **retiradas** por não terem mais relevância. Mantido abaixo apenas o
> registro técnico do que foi aplicado.

## Correção aplicada (registro)

- [x] 2.1 Identificação de órfãos (IDs 4412, 4415, 4416, 4420)
- [x] 2.2 Tabela de backup `backup_coleta_componente_20260211`
- [x] 3.1 Script principal de correção (transação)
- [x] 3.2 Remoção de órfãos validada
- [x] 3.3 Foreign key `fk_coleta_componente_item_composto` recriada
- [x] 4.1 Trigger `trg_validar_id_item_composto` + função `validar_id_item_composto()`
- [x] 4.2 Índices de performance criados
- [x] 4.3 View `vw_auditoria_integridade_coleta_componente`

## Resíduo opcional (não bloqueante)

- [ ] 6.1 Documentar causa raiz da inserção de órfãos (lição aprendida para
  scripts de migração futuros) — único item com valor remanescente.

---

**Status:** ✅ Executada em 11/02/2026 — fechada em 31/05/2026.
