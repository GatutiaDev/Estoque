package org.example.view;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import org.example.Model.Categoria;
import org.example.Model.Estoque;
import org.example.Model.Marca;
import org.example.dto.EstoqueDTO;
import org.example.viewmodel.EstoqueViewModel;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MainController {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int MESES_ALERTA_VENCIMENTO = 6;

    @FXML
    private TextField campoProduto;
    @FXML
    private ComboBox<Categoria> campoCategoria;
    @FXML
    private ComboBox<Marca> campoMarca;
    @FXML
    private TextField campoQuantidade;
    @FXML
    private TextField campoCaixa;
    @FXML
    private DatePicker campoVencimento;

    @FXML
    private Button botaoSalvar;
    @FXML
    private Button botaoLimpar;

    @FXML
    private TreeTableView<LinhaEstoque> tabelaEstoque;
    @FXML
    private TreeTableColumn<LinhaEstoque, String> colunaProduto;
    @FXML
    private TreeTableColumn<LinhaEstoque, String> colunaCategoria;
    @FXML
    private TreeTableColumn<LinhaEstoque, String> colunaMarca;
    @FXML
    private TreeTableColumn<LinhaEstoque, Integer> colunaQuantidade;
    @FXML
    private TreeTableColumn<LinhaEstoque, String> colunaCaixa;
    @FXML
    private TreeTableColumn<LinhaEstoque, LocalDate> colunaVencimento;

    @FXML
    private TextField campoBusca;
    @FXML
    private ComboBox<Categoria> filtroCategoria;
    @FXML
    private ComboBox<Marca> filtroMarca;
    @FXML
    private Label labelTabelaVazia;

    @FXML
    private Label labelTotal;

    private EstoqueViewModel viewModel;
    private Long idEmEdicao;

    // Produtos que estão expandidos na árvore, para continuarem abertos quando a tabela é remontada
    private final Set<String> produtosExpandidos = new HashSet<>();

    public void setViewModel(EstoqueViewModel viewModel) {
        this.viewModel = viewModel;

        viewModel.getItens().addListener((ListChangeListener<Estoque>) mudanca -> reconstruirTabela());
        campoBusca.textProperty().addListener((observavel, antigo, novo) -> reconstruirTabela());
        filtroCategoria.valueProperty().addListener((observavel, antigo, novo) -> reconstruirTabela());
        filtroMarca.valueProperty().addListener((observavel, antigo, novo) -> reconstruirTabela());

        reconstruirTabela();
        Platform.runLater(campoProduto::requestFocus);
    }

    @FXML
    private void initialize() {
        campoCategoria.setItems(FXCollections.observableArrayList(Categoria.values()));
        campoCategoria.setCellFactory(lista -> criarCelulaEnum("Categoria"));
        campoCategoria.setButtonCell(criarCelulaEnum("Categoria"));

        campoMarca.setItems(FXCollections.observableArrayList(Marca.values()));
        campoMarca.setCellFactory(lista -> criarCelulaEnum("Marca"));
        campoMarca.setButtonCell(criarCelulaEnum("Marca"));

        filtroCategoria.getItems().add(null);
        filtroCategoria.getItems().addAll(Categoria.values());
        filtroCategoria.setCellFactory(lista -> criarCelulaEnum("Todas as categorias"));
        filtroCategoria.setButtonCell(criarCelulaEnum("Todas as categorias"));

        filtroMarca.getItems().add(null);
        filtroMarca.getItems().addAll(Marca.values());
        filtroMarca.setCellFactory(lista -> criarCelulaEnum("Todas as marcas"));
        filtroMarca.setButtonCell(criarCelulaEnum("Todas as marcas"));

        configurarColunas();
        configurarLinhas();

        tabelaEstoque.setRoot(new TreeItem<>());
        tabelaEstoque.setShowRoot(false);

        configurarDesselecao();
    }

    private void configurarColunas() {
        // Nos lotes que ficam dentro de um produto, nome, categoria e marca já aparecem na linha do produto
        colunaProduto.setCellValueFactory(dados -> {
            LinhaEstoque linha = dados.getValue().getValue();
            return new SimpleStringProperty(linha.isDentroDeProduto() ? "Lote" : linha.getProduto());
        });
        colunaProduto.setCellFactory(coluna -> new TreeTableCell<>() {
            @Override
            protected void updateItem(String texto, boolean vazio) {
                super.updateItem(texto, vazio);
                setText(vazio ? null : texto);
                LinhaEstoque linha = vazio || getTableRow() == null ? null : getTableRow().getItem();
                getStyleClass().remove("texto-lote");
                if (linha != null && linha.isDentroDeProduto()) {
                    getStyleClass().add("texto-lote");
                }
            }
        });

        colunaCategoria.setCellValueFactory(dados -> {
            LinhaEstoque linha = dados.getValue().getValue();
            return new SimpleStringProperty(linha.isDentroDeProduto() ? "" : formatarEnum(linha.getCategoria()));
        });
        colunaMarca.setCellValueFactory(dados -> {
            LinhaEstoque linha = dados.getValue().getValue();
            return new SimpleStringProperty(linha.isDentroDeProduto() ? "" : formatarEnum(linha.getMarca()));
        });

        colunaQuantidade.setCellValueFactory(dados -> new SimpleObjectProperty<>(dados.getValue().getValue().getQuantidade()));

        colunaCaixa.setCellValueFactory(dados -> new SimpleStringProperty(dados.getValue().getValue().getCaixas()));
        // "10" deve vir depois de "2": ordena pelo número da primeira caixa, não pelo texto
        colunaCaixa.setComparator(Comparator.comparingInt(this::primeiraCaixa));

        colunaVencimento.setCellValueFactory(dados -> new SimpleObjectProperty<>(dados.getValue().getValue().getVencimento()));
        colunaVencimento.setCellFactory(coluna -> new TreeTableCell<>() {
            @Override
            protected void updateItem(LocalDate data, boolean vazio) {
                super.updateItem(data, vazio);
                setText(vazio || data == null ? null : data.format(FORMATO_DATA));
            }
        });
    }

    private void configurarLinhas() {
        tabelaEstoque.setRowFactory(tabela -> {
            TreeTableRow<LinhaEstoque> linha = new TreeTableRow<>() {
                @Override
                protected void updateItem(LinhaEstoque item, boolean vazio) {
                    super.updateItem(item, vazio);
                    getStyleClass().removeAll("linha-vencida", "linha-vencendo", "linha-produto");

                    if (vazio || item == null) {
                        return;
                    }
                    if (item.isProduto()) {
                        getStyleClass().add("linha-produto");
                    }

                    // No produto, o vencimento é o do lote que vence primeiro: a cor fica a do pior lote
                    LocalDate vencimento = item.getVencimento();
                    if (vencimento == null) {
                        return;
                    }
                    LocalDate hoje = LocalDate.now();
                    if (vencimento.isBefore(hoje)) {
                        getStyleClass().add("linha-vencida");
                    } else if (!vencimento.isAfter(hoje.plusMonths(MESES_ALERTA_VENCIMENTO))) {
                        getStyleClass().add("linha-vencendo");
                    }
                }
            };

            // Duplo clique num lote abre para edição (num produto, o duplo clique expande/recolhe)
            linha.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !linha.isEmpty() && !linha.getItem().isProduto()) {
                    iniciarEdicao(linha.getItem().getLote());
                }
            });
            return linha;
        });
    }

    // Remonta a árvore a partir da lista do ViewModel, aplicando a busca e os filtros.
    // É chamada quando a lista muda (cadastro, edição, +1/−1, exclusão) ou quando um filtro muda.
    private void reconstruirTabela() {
        if (viewModel == null) {
            return;
        }

        // Guarda o que estava selecionado para selecionar de novo depois de remontar
        LinhaEstoque selecionadaAntes = linhaSelecionada();

        List<Estoque> visiveis = viewModel.getItens().stream()
                .filter(this::passaNosFiltros)
                .toList();

        Map<String, List<Estoque>> lotesPorProduto = visiveis.stream()
                .collect(Collectors.groupingBy(this::chaveProduto, LinkedHashMap::new, Collectors.toList()));

        TreeItem<LinhaEstoque> raiz = new TreeItem<>();
        lotesPorProduto.forEach((chave, lotes) -> raiz.getChildren().add(criarItemProduto(chave, lotes)));

        tabelaEstoque.setRoot(raiz);
        tabelaEstoque.sort();

        if (selecionadaAntes != null) {
            if (selecionadaAntes.isProduto()) {
                selecionarProduto(selecionadaAntes.getChaveProduto());
            } else {
                selecionarPorId(selecionadaAntes.getLote().getId());
            }
        }

        atualizarTotal(visiveis, lotesPorProduto.size());
    }

    private TreeItem<LinhaEstoque> criarItemProduto(String chave, List<Estoque> lotes) {
        if (lotes.size() == 1) {
            return new TreeItem<>(LinhaEstoque.loteUnico(chave, lotes.get(0)));
        }

        List<Estoque> ordenados = lotes.stream()
                .sorted(Comparator.comparing(Estoque::getDataVencimento).thenComparing(Estoque::getCaixa))
                .toList();

        TreeItem<LinhaEstoque> itemProduto = new TreeItem<>(LinhaEstoque.produto(chave, ordenados));
        for (Estoque lote : ordenados) {
            itemProduto.getChildren().add(new TreeItem<>(LinhaEstoque.loteDoProduto(chave, lote)));
        }

        itemProduto.setExpanded(produtosExpandidos.contains(chave));
        itemProduto.expandedProperty().addListener((observavel, antes, expandido) -> {
            if (expandido) {
                produtosExpandidos.add(chave);
            } else {
                produtosExpandidos.remove(chave);
            }
        });
        return itemProduto;
    }

    // Lotes com o mesmo nome (ignorando maiúsculas, acentos e espaços) e a mesma marca são o mesmo produto
    private String chaveProduto(Estoque lote) {
        return normalizar(lote.getProduto()) + "|" + lote.getMarca();
    }

    private boolean passaNosFiltros(Estoque item) {
        String termo = normalizar(campoBusca.getText());
        Categoria categoria = filtroCategoria.getValue();
        Marca marca = filtroMarca.getValue();

        return (categoria == null || item.getCategoria() == categoria)
                && (marca == null || item.getMarca() == marca)
                && (termo.isEmpty() || normalizar(item.getProduto()).contains(termo));
    }

    private void configurarDesselecao() {
        // A cena ainda não existe no initialize(), então registra o filtro assim que a tabela entrar numa cena
        tabelaEstoque.sceneProperty().addListener((observavel, cenaAntiga, cenaNova) -> {
            if (cenaNova != null) {
                cenaNova.addEventFilter(MouseEvent.MOUSE_PRESSED, this::desselecionarSeClicouFora);
            }
        });

        tabelaEstoque.setOnKeyPressed(evento -> {
            if (evento.getCode() == KeyCode.ESCAPE) {
                tabelaEstoque.getSelectionModel().clearSelection();
            }
        });
    }

    private void desselecionarSeClicouFora(MouseEvent evento) {
        // Sobe do elemento clicado até a raiz da tela para descobrir onde foi o clique
        for (Node no = evento.getPickResult().getIntersectedNode(); no != null; no = no.getParent()) {
            if (no instanceof ButtonBase) {
                return; // botões (Editar, −1, +1, Excluir) agem sobre o item selecionado
            }
            if (no instanceof TreeTableRow<?> linha) {
                if (linha.isEmpty()) {
                    tabelaEstoque.getSelectionModel().clearSelection(); // área vazia abaixo dos produtos
                }
                return;
            }
            if (no == tabelaEstoque) {
                return; // cabeçalho ou barra de rolagem da tabela
            }
        }
        tabelaEstoque.getSelectionModel().clearSelection();
    }

    @FXML
    private void aoClicarSalvar() {
        Integer quantidade = lerNumero(campoQuantidade, "Quantidade");
        if (quantidade == null) {
            return;
        }
        Integer caixa = lerNumero(campoCaixa, "Caixa");
        if (caixa == null) {
            return;
        }

        String produto = campoProduto.getText();
        Categoria categoria = campoCategoria.getValue();
        Marca marca = campoMarca.getValue();
        LocalDate vencimento = campoVencimento.getValue();

        EstoqueDTO dados = new EstoqueDTO(produto, categoria, quantidade, caixa, vencimento, marca);

        try {
            if (idEmEdicao == null) {
                viewModel.cadastrarEstoque(dados);
                limparFormulario();
            } else {
                Long idEditado = idEmEdicao;
                viewModel.atualizarEstoque(idEditado, dados);
                limparFormulario();
                selecionarPorId(idEditado);
            }
        } catch (RuntimeException e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    private void aoClicarLimpar() {
        limparFormulario();
    }

    @FXML
    private void aoClicarEditar() {
        LinhaEstoque selecionada = linhaSelecionada();
        if (selecionada == null) {
            mostrarErro("Selecione um produto para editar.");
            return;
        }
        if (selecionada.isProduto()) {
            expandirSelecionado();
            mostrarErro("Este produto tem vários lotes. Selecione o lote que deseja editar.");
            return;
        }
        iniciarEdicao(selecionada.getLote());
    }

    @FXML
    private void aoClicarAumentar() {
        LinhaEstoque selecionada = linhaSelecionada();
        if (selecionada == null) {
            mostrarErro("Selecione um produto na tabela.");
            return;
        }
        if (selecionada.isProduto()) {
            expandirSelecionado();
            mostrarErro("Selecione o lote (caixa e vencimento) que vai receber a unidade.\n"
                    + "Para um vencimento novo, cadastre um novo lote pelo formulário.");
            return;
        }
        alterarQuantidade(selecionada.getLote().getId(), true);
    }

    @FXML
    private void aoClicarDiminuir() {
        LinhaEstoque selecionada = linhaSelecionada();
        if (selecionada == null) {
            mostrarErro("Selecione um produto na tabela.");
            return;
        }

        Estoque lote = selecionada.isProduto() ? selecionada.loteQueVencePrimeiro() : selecionada.getLote();
        if (lote == null) {
            mostrarErro("Este produto não tem unidades em estoque.");
            return;
        }
        alterarQuantidade(lote.getId(), false);
    }

    private void alterarQuantidade(Long idLote, boolean aumentar) {
        try {
            if (aumentar) {
                viewModel.aumentarQuantidade(idLote);
            } else {
                viewModel.diminuirQuantidade(idLote);
            }
        } catch (RuntimeException e) {
            mostrarErro(e.getMessage());
        }

        // Se esse lote está aberto no formulário, mantém a quantidade do formulário em dia
        if (idLote.equals(idEmEdicao)) {
            viewModel.getItens().stream()
                    .filter(item -> item.getId().equals(idLote))
                    .findFirst()
                    .ifPresent(item -> campoQuantidade.setText(String.valueOf(item.getQuantidade())));
        }
    }

    private void iniciarEdicao(Estoque item) {
        idEmEdicao = item.getId();
        campoProduto.setText(item.getProduto());
        campoCategoria.setValue(item.getCategoria());
        campoMarca.setValue(item.getMarca());
        campoQuantidade.setText(String.valueOf(item.getQuantidade()));
        campoCaixa.setText(String.valueOf(item.getCaixa()));
        campoVencimento.setValue(item.getDataVencimento());

        botaoSalvar.setText("Salvar alteração");
        botaoLimpar.setText("Cancelar edição");
        campoProduto.requestFocus();
    }

    @FXML
    private void aoClicarExcluir() {
        LinhaEstoque selecionada = linhaSelecionada();
        if (selecionada == null) {
            mostrarErro("Selecione um produto para excluir.");
            return;
        }

        String pergunta = selecionada.isProduto()
                ? "Excluir \"" + selecionada.getProduto() + "\" e todos os seus "
                        + selecionada.getLotes().size() + " lotes do estoque?"
                : "Excluir \"" + selecionada.getProduto() + "\" (caixa " + selecionada.getCaixas()
                        + ", vence " + selecionada.getVencimento().format(FORMATO_DATA) + ") do estoque?";

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION, pergunta, ButtonType.YES, ButtonType.NO);
        confirmacao.setHeaderText(null);
        confirmacao.showAndWait()
                .filter(resposta -> resposta == ButtonType.YES)
                .ifPresent(resposta -> {
                    try {
                        for (Estoque lote : selecionada.getLotes()) {
                            viewModel.deletarEstoque(lote.getId());
                            if (lote.getId().equals(idEmEdicao)) {
                                limparFormulario();
                            }
                        }
                    } catch (RuntimeException e) {
                        mostrarErro(e.getMessage());
                    }
                });
    }

    @FXML
    private void aoClicarLimparBusca() {
        campoBusca.clear();
        filtroCategoria.setValue(null);
        filtroMarca.setValue(null);
    }

    private LinhaEstoque linhaSelecionada() {
        TreeItem<LinhaEstoque> item = tabelaEstoque.getSelectionModel().getSelectedItem();
        return item == null ? null : item.getValue();
    }

    private void expandirSelecionado() {
        TreeItem<LinhaEstoque> item = tabelaEstoque.getSelectionModel().getSelectedItem();
        if (item != null) {
            item.setExpanded(true);
        }
    }

    // Procura o lote na árvore (sozinho ou dentro de um produto), expande o produto se precisar e seleciona
    private void selecionarPorId(Long id) {
        for (TreeItem<LinhaEstoque> item : tabelaEstoque.getRoot().getChildren()) {
            if (!item.getValue().isProduto()) {
                if (item.getValue().getLote().getId().equals(id)) {
                    selecionar(item);
                    return;
                }
                continue;
            }
            for (TreeItem<LinhaEstoque> filho : item.getChildren()) {
                if (filho.getValue().getLote().getId().equals(id)) {
                    item.setExpanded(true);
                    selecionar(filho);
                    return;
                }
            }
        }
    }

    // Seleciona a linha do produto pela chave. Se o produto ficou com um lote só, seleciona esse lote.
    private void selecionarProduto(String chave) {
        for (TreeItem<LinhaEstoque> item : tabelaEstoque.getRoot().getChildren()) {
            if (item.getValue().getChaveProduto().equals(chave)) {
                selecionar(item);
                return;
            }
        }
    }

    private void selecionar(TreeItem<LinhaEstoque> item) {
        tabelaEstoque.getSelectionModel().select(item);
        int linha = tabelaEstoque.getRow(item);
        if (linha >= 0) {
            tabelaEstoque.scrollTo(linha);
        }
    }

    private int primeiraCaixa(String caixas) {
        try {
            return Integer.parseInt(caixas.split(",")[0].trim());
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    // Deixa minúsculo e remove acentos, para "algodão" encontrar "Algodao" e vice-versa
    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }

    // Só converte o texto em número; as regras (ex.: não pode ser negativo) ficam no Service
    private Integer lerNumero(TextField campo, String nomeCampo) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException e) {
            mostrarErro(nomeCampo + " precisa ser um número inteiro.");
            return null;
        }
    }

    private void limparFormulario() {
        idEmEdicao = null;
        botaoSalvar.setText("Salvar");
        botaoLimpar.setText("Limpar");
        campoProduto.clear();
        campoCategoria.getSelectionModel().clearSelection();
        campoMarca.getSelectionModel().clearSelection();
        campoQuantidade.clear();
        campoCaixa.clear();
        campoVencimento.setValue(null);
        campoProduto.requestFocus();
    }

    private void atualizarTotal(List<Estoque> visiveis, int produtosVisiveis) {
        long produtosCadastrados = viewModel.getItens().stream().map(this::chaveProduto).distinct().count();
        int totalUnidades = visiveis.stream().mapToInt(Estoque::getQuantidade).sum();
        String detalhe = visiveis.size() + " lote(s) · " + totalUnidades + " unidade(s)";

        if (produtosVisiveis == produtosCadastrados) {
            labelTotal.setText(produtosCadastrados + " produto(s) · " + detalhe);
        } else {
            labelTotal.setText("Mostrando " + produtosVisiveis + " de " + produtosCadastrados
                    + " produto(s) · " + detalhe);
        }

        labelTabelaVazia.setText(produtosCadastrados == 0
                ? "Nenhum produto cadastrado ainda."
                : "Nenhum produto encontrado para essa busca.");
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensagem);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // Transforma PERFUME_FEMININO em "Perfume feminino" e NATURA em "Natura" (serve para qualquer enum)
    private String formatarEnum(Enum<?> valor) {
        if (valor == null) {
            return "";
        }
        String texto = valor.name().replace('_', ' ').toLowerCase();
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private <T extends Enum<T>> ListCell<T> criarCelulaEnum(String textoPadrao) {
        return new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean vazio) {
                super.updateItem(item, vazio);
                setText(vazio || item == null ? textoPadrao : formatarEnum(item));
            }
        };
    }
}
