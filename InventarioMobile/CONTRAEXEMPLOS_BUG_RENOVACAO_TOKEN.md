# Contraexemplos - Bug de Renovação Automática de Token via Biometria

## 📋 Objetivo

Este documento registra os contraexemplos encontrados durante a exploração da bug condition no código UNFIXED, demonstrando que o bug existe e precisa ser corrigido.

---

## 🐛 Bug Condition

```
C(X) = (tokenExpirado OU offline) E biometriaHabilitada E 
       promptNãoMostrado E acessoBloqueado
```

## ✅ Comportamento Esperado (Após Correção)

```
P(result) = promptMostrado E renovacaoTentada E 
            (tokenRenovado OU acessoOfflinePermitido OU redirecionadoParaLogin)
```

---

## 📝 Contraexemplos Documentados

### Contraexemplo 1: LoginActivity com Token Expirado

**Entrada:**