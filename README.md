# Estoque

Aplicação desktop para controle de estoque de produtos, feita em **Java** com **JavaFX** e **SQLite**.

Permite cadastrar produtos com categoria, marca, quantidade, local de armazenamento e data de vencimento, acompanhar o que está vencido ou perto de vencer e dar baixa ou repor unidades com um clique. Os dados ficam salvos localmente, sem precisar de servidor ou internet.

## Funcionalidades

- **Cadastro, edição e exclusão de produtos**. Para editar, use o botão *Editar selecionado* ou dê um duplo clique na linha.
- **Ajuste rápido de quantidade** com os botões **−1** e **+1**, sem abrir o formulário.
- **Alerta de vencimento** por cor na tabela:
  - 🔴 vermelho: produto vencido
  - 🟡 amarelo: vence nos próximos 6 meses
- **Busca por nome** enquanto você digita, sem diferenciar maiúsculas, minúsculas e acentos ("algodao" encontra "Algodão").
- **Filtros por categoria e por marca**, que funcionam em conjunto com a busca.
- **Ordenação** clicando no cabeçalho de qualquer coluna.
- **Totais no rodapé**: quantidade de produtos e de unidades, respeitando os filtros ativos.
- **Validação dos dados**, com mensagens claras (nome obrigatório, quantidade não negativa etc.).
- **Confirmação antes de excluir**.

## Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 17+ | Linguagem |
| JavaFX 21 (FXML + CSS) | Interface gráfica |
| SQLite (via `sqlite-jdbc`) | Banco de dados local em arquivo |
| Maven | Build e dependências |
| JUnit 5 | Testes (configurado) |

## Como executar

**Pré-requisitos:** JDK 17 ou superior e Maven instalados.

Na pasta do projeto (onde fica o `pom.xml`):

```bash
mvn javafx:run
```

Também é possível rodar pela IDE executando a classe `org.example.App`.

> Se aparecer o erro `No plugin found for prefix 'javafx'`, o comando foi executado fora da pasta do projeto.

## Onde os dados ficam salvos

O banco é um único arquivo SQLite criado automaticamente na pasta de dados do usuário (`%APPDATA%` no Windows), na primeira vez que o app é aberto. O caminho exato está em [`Banco.java`](src/main/java/org/example/banco/Banco.java).

Por ser um arquivo único, **fazer backup é só copiar esse arquivo**. Isso também serve para levar os dados para outro computador. Faça a cópia com o aplicativo fechado.

## Arquitetura

O projeto segue o padrão **MVVM** com camadas bem separadas:

```
View (FXML + Controller)  →  ViewModel  →  Service  →  Repository  →  SQLite
```

| Camada | Responsabilidade |
|---|---|
| **View** (`view/`, `fxml/`, `css/`) | Tela, eventos de clique, filtros e formatação para exibição |
| **ViewModel** (`viewmodel/`) | Mantém a lista observável de produtos que a tela exibe e recarrega após cada alteração |
| **Service** (`service/`) | Regras de negócio: roda as validações, busca o produto e aplica as alterações |
| **Validações** (`validations/`) | Uma classe por regra, todas implementando a mesma interface |
| **Repository** (`repository/`) | Acesso ao banco com SQL. O Service depende apenas da interface `EstoqueRepository` |
| **Model / DTO** (`Model/`, `dto/`) | A entidade do produto, os enums e o `record` com os dados vindos do formulário |

### Estrutura de pastas

```
src/main/
├── java/org/example/
│   ├── App.java                 # Ponto de entrada: monta as camadas e abre a janela
│   ├── Model/                   # Entidade Estoque e enums Categoria e Marca
│   ├── dto/                     # EstoqueDTO (dados do formulário)
│   ├── banco/                   # Conexão SQLite, criação e migração da tabela
│   ├── repository/              # Interface + implementação SQL
│   ├── service/                 # Regras de negócio
│   ├── validations/             # Validadores e a lista oficial (ValidacoesEstoque)
│   ├── exception/               # ValidacaoException e ProdutoNaoEncontradoException
│   ├── viewmodel/               # EstoqueViewModel
│   └── view/                    # MainController
└── resources/
    ├── fxml/main-view.fxml      # Layout da tela
    └── css/estilo.css           # Estilos
```

## Personalizando

**Categorias e marcas** são enums. Para adicionar opções, inclua um valor novo em [`Categoria.java`](src/main/java/org/example/Model/Categoria.java) ou [`Marca.java`](src/main/java/org/example/Model/Marca.java). Os campos da tela e os filtros são atualizados automaticamente, e `PERFUME_FEMININO` aparece como "Perfume feminino".

> ⚠️ Não renomeie nem remova um valor que já foi usado em produtos cadastrados. O banco guarda o nome do enum, e um valor que não existe mais impede a lista de carregar.

**Novas regras de validação:** crie uma classe que implemente `ValidationsServiceEstoque` e registre-a em [`ValidacoesEstoque.java`](src/main/java/org/example/validations/ValidacoesEstoque.java). A ordem da lista define a ordem das mensagens de erro.

**Prazo do alerta de vencimento:** constante `MESES_ALERTA_VENCIMENTO` em [`MainController.java`](src/main/java/org/example/view/MainController.java).

## Próximos passos

- [ ] Testes automatizados com JUnit (Service e validadores)
- [ ] Botões de exportar e importar backup do banco
- [ ] Preço de custo e de venda, com valor total do estoque
- [ ] Estoque mínimo, com alerta de reposição
- [ ] Aviso ao cadastrar um produto idêntico a um já existente
- [ ] Instalador para Windows com `jpackage`

## Autor

Desenvolvido por **Gabriel Tutia**.
