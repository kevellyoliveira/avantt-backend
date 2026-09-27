# avantt-backend
backend do projeto avantt

## Tratamento de Erros

A API padroniza respostas de erro usando o objeto `ErrorResponse`. O frontend deve usar o campo `code` (machine-readable) para decidir como apresentar a mensagem ao usuário e exibir o campo `message` como texto legível. Para erros de validação, o array `details` contém campos e mensagens específicas.

Exemplo de payload de erro:

```
{
  "status": 400,
  "error": "Bad Request",
  "message": "Campos inválidos",
  "code": "VALIDATION_ERROR",
  "timestamp": "2026-09-27T12:34:56.789",
  "details": [
    { "field": "name", "message": "Nome é obrigatório" }
  ]
}
```

Recomendações para o frontend:

1. Priorize o campo `code` para lógica de tratamento (ex.: `UNAUTHORIZED` -> redirect para login; `RESOURCE_NOT_FOUND` -> mostrar tela "não encontrado").
2. Exiba `message` ao usuário quando não houver uma mensagem específica do campo.
3. Para `VALIDATION_ERROR`, mostre cada item de `details` próximo ao respectivo campo de formulário.
4. Trate `500` (`INTERNAL_ERROR`) como erro do servidor e mostre uma mensagem genérica amigável ao usuário.
