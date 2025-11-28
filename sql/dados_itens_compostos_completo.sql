-- ============================================================================
-- SCRIPT DE DADOS - ITENS COMPOSTOS
-- Sistema de Inventário Patrimonial - IFMT
-- Gerado em: 28/11/2025
-- Total de registros: 1.588 itens compostos + 12 coletas de componentes
-- ============================================================================

-- Desabilitar verificação de foreign keys durante a importação
SET session_replication_role = 'replica';

-- ============================================================================
-- TABELA: tabela_item_composto
-- Estrutura: id, id_patrimonio_principal, tipo_componente, descricao_componente,
--            quantidade_esperada, obrigatorio, observacao, data_cadastro
-- ============================================================================

-- Limpar dados existentes (opcional - descomente se necessário)
-- TRUNCATE TABLE tabela_coleta_componente CASCADE;
-- TRUNCATE TABLE tabela_item_composto CASCADE;

-- ============================================================================
-- LOTE 1: IDs 1-200
-- ============================================================================

INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (1, 1, 'CADEIRA', 'Cadeira escolar', 1, true, NULL, '2025-11-27 09:15:00.52214');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (2, 1, 'MESA', 'Mesa individual', 1, true, NULL, '2025-11-27 09:15:00.542951');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (3, 2, 'MONITOR', 'Monitor LCD', 1, true, NULL, '2025-11-27 09:15:00.54538');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (6, 2, 'CPU', 'Gabinete CPU', 1, true, NULL, '2025-11-27 09:15:00.549776');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (7, 1201, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (8, 1201, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (9, 1202, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (10, 1202, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (11, 1203, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (12, 1203, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (13, 1204, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (14, 1204, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (15, 1205, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (16, 1205, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (17, 1206, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (18, 1206, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (19, 1207, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (20, 1207, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (21, 1208, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (22, 1208, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (23, 1209, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (24, 1209, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (25, 1210, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (26, 1210, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (27, 1211, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (28, 1211, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (29, 1212, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (30, 1212, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (31, 1213, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (32, 1213, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (33, 1214, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (34, 1214, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (35, 1215, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (36, 1215, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (37, 1216, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (38, 1216, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (39, 1217, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (40, 1217, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (41, 1218, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (42, 1218, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (43, 1219, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (44, 1219, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (45, 1220, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (46, 1220, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (47, 1221, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (48, 1221, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (49, 1222, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (50, 1222, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (51, 1223, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (52, 1223, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (53, 1224, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (54, 1224, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (55, 1225, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (56, 1225, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (57, 1226, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (58, 1226, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (59, 1227, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (60, 1227, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (61, 1228, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (62, 1228, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (63, 1229, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (64, 1229, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (65, 1230, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (66, 1230, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (67, 1231, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (68, 1231, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (69, 1232, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (70, 1232, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (71, 1233, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (72, 1233, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (73, 1234, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (74, 1234, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (75, 1235, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (76, 1235, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (77, 1236, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (78, 1236, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (79, 1237, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (80, 1237, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (81, 1238, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (82, 1238, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (83, 1239, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (84, 1239, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (85, 1240, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (86, 1240, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (87, 1241, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (88, 1241, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (89, 1242, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (90, 1242, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (91, 1243, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (92, 1243, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (93, 1244, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (94, 1244, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (95, 1245, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (96, 1245, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (97, 1246, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (98, 1246, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (99, 1247, 'MESA', 'Mesa com tampo MDP 18mm, 450x600mm', 1, true, NULL, '2025-11-27 09:19:21.941195');
INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (100, 1247, 'CADEIRA', 'Cadeira empilhável, assento e encosto em polipropileno', 1, true, NULL, '2025-11-27 09:19:21.941195');


-- ============================================================================
-- NOTA: Este arquivo contém os primeiros 100 registros como exemplo.
-- Para extrair TODOS os 1.588 registros, execute a query abaixo no PostgreSQL:
-- ============================================================================

/*
-- QUERY PARA EXTRAIR TODOS OS DADOS DE tabela_item_composto:

SELECT 'INSERT INTO tabela_item_composto (id, id_patrimonio_principal, tipo_componente, descricao_componente, quantidade_esperada, obrigatorio, observacao, data_cadastro) VALUES (' ||
       id || ', ' ||
       COALESCE(id_patrimonio_principal::text, 'NULL') || ', ' ||
       COALESCE('''' || REPLACE(tipo_componente, '''', '''''') || '''', 'NULL') || ', ' ||
       COALESCE('''' || REPLACE(descricao_componente, '''', '''''') || '''', 'NULL') || ', ' ||
       COALESCE(quantidade_esperada::text, '1') || ', ' ||
       COALESCE(obrigatorio::text, 'true') || ', ' ||
       COALESCE('''' || REPLACE(observacao, '''', '''''') || '''', 'NULL') || ', ' ||
       COALESCE('''' || data_cadastro::text || '''', 'NOW()') || ');' as insert_stmt
FROM tabela_item_composto
ORDER BY id;

*/

-- ============================================================================
-- TABELA: tabela_coleta_componente (12 registros)
-- Estrutura: id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada,
--            status_componente, observacao_coleta, data_coleta
-- ============================================================================

INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES (1, 1008, 2, 1, 1, 'COMPLETO', '', '2025-11-27 09:55:12.405109');
INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES (2, 1007, 2, 1, 1, 'COMPLETO', '', '2025-11-27 09:55:31.278282');
INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES (3, 1010, 2, 1, 1, 'COMPLETO', '', '2025-11-27 14:25:47.000657');
INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES (4, 1013, 2, 1, 1, 'COMPLETO', 'teste', '2025-11-27 14:26:16.919583');
INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES (5, 1016, 2, 1, 1, 'COMPLETO', '', '2025-11-27 14:49:39.819617');
INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES (6, 1015, 2, 1, 1, 'COMPLETO', '', '2025-11-27 14:49:55.500873');
INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES (7, 1024, 2, 1, 1, 'COMPLETO', '', '2025-11-27 14:55:34.464874');
INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES (8, 1023, 2, 1, 1, 'COMPLETO', '', '2025-11-27 14:55:47.406204');
INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES (9, 1011, 2, 1, 1, 'COMPLETO', '', '2025-11-27 14:56:50.474817');
INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES (10, 1012, 2, 1, 1, 'COMPLETO', '', '2025-11-27 20:01:24.385622');
INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES (11, 1020, 2, 1, 1, 'COMPLETO', '', '2025-11-27 15:06:43.649159');
INSERT INTO tabela_coleta_componente (id, id_item_composto, id_inventario, id_coletor, quantidade_encontrada, status_componente, observacao_coleta, data_coleta) VALUES (13, 1017, 2, 1, 1, 'COMPLETO', '', '2025-11-27 20:01:40.021468');

-- ============================================================================
-- ATUALIZAR SEQUENCES
-- ============================================================================

SELECT setval('tabela_item_composto_id_seq', (SELECT MAX(id) FROM tabela_item_composto));
SELECT setval('tabela_coleta_componente_id_seq', (SELECT MAX(id) FROM tabela_coleta_componente));

-- Reabilitar verificação de foreign keys
SET session_replication_role = 'origin';

-- ============================================================================
-- FIM DO SCRIPT
-- ============================================================================
