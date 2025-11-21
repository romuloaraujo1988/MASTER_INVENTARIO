# 📱 Sistema de Sincronização Offline - Parte 1

## Backend (API REST)

### Endpoints Criados

#### 1. Sincronização Completa
```
GET /api/mobile/sync/full?idInventario={id}
```

Retorna TODOS os dados necessários para modo offline:
- Patrimônios
- Salas
- Responsáveis
- Setores
- Inventário ativo

#### 2. Sincronização Parcial
```
GET /api/mobile/sync/patrimonios?ultimaAtualizacao={timestamp}
GET /api/mobile/sync/salas
GET /api/mobile/sync/responsaveis
```

#### 3. Verificar Atualizações
```
GET /api/mobile/sync/check-updates?ultimaSincronizacao={timestamp}
```

#### 4. Metadados
```
GET /api/mobile/sync/metadata
```

### Exemplo de Resposta

```json
{
  "success": true,
  "message": "Sincronização completa realizada com sucesso",
  "data": {
    "patrimonios": [
      {
        "id": 1,
        "numero": "123456",
        "descricao": "CADEIRA GIRATÓRIA",
        "idSala": 10,
        "nomeSala": "Sala 101",
        "idResponsavel": 5,
        "nomeResponsavel": "João Silva"
      }
    ],
    "salas": [
      {
        "id": 10,
        "nome": "Sala 101",
        "andar": "1º Andar",
        "bloco": "A"
      }
    ],
    "responsaveis": [
      {
        "id": 5,
        "nome": "João Silva",
        "cpf": "123.456.789-00"
      }
    ],
    "totalPatrimonios": 1500,
    "totalSalas": 50,
    "totalResponsaveis": 30,
    "timestamp": 1699483200000,
    "versao": "2.0.0"
  }
}
```

---

## Continua na Parte 2...
