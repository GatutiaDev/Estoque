package org.example.view;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
    private TableView<Estoque> tabelaEstoque;
    @FXML
    private TableColumn<Estoque, String> colunaProduto;
    @FXML
    private TableColumn<Estoque, String> colunaCategoria;
    @FXML
    private TableColumn<Estoque, String> colunaMarca;
    @FXML
    private TableColumn<Estoque, Integer> colunaQuantidade;
    @FXML
    private TableColumn<Estoque, Integer> colunaCaixa;
    @FXML
    private TableColumn<Estoque, LocalDate> colunaVencimento;

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
    private FilteredList<Estoque> itensFiltrados;
    private Long idEmEdicao;

    public void setViewModel(EstoqueViewModel viewModel) {
        this.viewModel = viewModel;

        itensFiltrados = new FilteredList<>(viewModel.getItens());
        SortedList<Estoque> itensOrdenados = new SortedList<>(itensFiltrados);
        itensOrdenados.comparatorProperty().bind(tabelaEstoque.comparatorProperty());
        tabelaEstoque.setItems(itensOrdenados);

        campoBusca.textProperty().addListener((observavel, antigo, novo) -> aplicarFiltro());
        filtroCategoria.valueProperty().addListener((observavel, antigo, novo) -> aplicarFiltro());
        filtroMarca.valueProperty().addListener((observavel, antigo, novo) -> aplicarFiltro());
        itensFiltrados.addListener((ListChangeListener<Estoque>) mudanca -> atualizarTotal());

        atualizarTotal();
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

        colunaProduto.setCellValueFactory(dados -> new SimpleStringProperty(dados.getValue().getProduto()));
        colunaCategoria.setCellValueFactory(dados -> new SimpleStringProperty(formatarEnum(dados.getValue().getCategoria())));
        colunaMarca.setCellValueFactory(dados -> new SimpleStringProperty(formatarEnum(dados.getValue().getMarca())));
        colunaQuantidade.setCellValueFactory(dados -> new SimpleObjectProperty<>(dados.getValue().getQuantidade()));
        colunaCaixa.setCellValueFactory(dados -> new SimpleObjectProperty<>(dados.getValue().getCaixa()));
        colunaVencimento.setCellValueFactory(dados -> new SimpleObjectProperty<>(dados.getValue().getDataVencimento()));
        colunaVencimento.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate data, boolean vazio) {
                super.updateItem(data, vazio);
                setText(vazio || data == null ? null : data.format(FORMATO_DATA));
            }
        });

        tabelaEstoque.setRowFactory(tabela -> {
            TableRow<Estoque> linha = new TableRow<>() {
                @Override
                protected void updateItem(Estoque item, boolean vazio) {
                    super.updateItem(item, vazio);
                    getStyleClass().removeAll("linha-vencida", "linha-vencendo");

                    if (vazio || item == null || item.getDataVencimento() == null) {
                        return;
                    }

                    LocalDate hoje = LocalDate.now();
                    if (item.getDataVencimento().isBefore(hoje)) {
                        getStyleClass().add("linha-vencida");
                    } else if (!item.getDataVencimento().isAfter(hoje.plusMonths(MESES_ALERTA_VENCIMENTO))) {
                        getStyleClass().add("linha-vencendo");
                    }
                }
            };

            // Duplo clique na linha abre o produto para edição
            linha.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !linha.isEmpty()) {
                    iniciarEdicao(linha.getItem());
                }
            });
            return linha;
        });

        configurarDesselecao();
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
                return; // botões (Editar, −1, +1, Excluir) agem sobre o produto selecionado
            }
            if (no instanceof TableRow<?> linha) {
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
        Estoque selecionado = tabelaEstoque.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarErro("Selecione um produto para editar.");
            return;
        }
        iniciarEdicao(selecionado);
    }

    @FXML
    private void aoClicarAumentar() {
        alterarQuantidadeSelecionado(true);
    }

    @FXML
    private void aoClicarDiminuir() {
        alterarQuantidadeSelecionado(false);
    }

    private void alterarQuantidadeSelecionado(boolean aumentar) {
        Estoque selecionado = tabelaEstoque.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarErro("Selecione um produto na tabela.");
            return;
        }

        Long id = selecionado.getId();
        try {
            if (aumentar) {
                viewModel.aumentarQuantidade(id);
            } else {
                viewModel.diminuirQuantidade(id);
            }
        } catch (RuntimeException e) {
            mostrarErro(e.getMessage());
        }

        // A lista é recarregada do banco, então a seleção se perde: seleciona de novo pelo id
        Estoque atualizado = selecionarPorId(id);

        // Se esse produto está aberto no formulário, mantém a quantidade do formulário em dia
        if (atualizado != null && id.equals(idEmEdicao)) {
            campoQuantidade.setText(String.valueOf(atualizado.getQuantidade()));
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

    private Estoque selecionarPorId(Long id) {
        for (Estoque item : tabelaEstoque.getItems()) {
            if (item.getId().equals(id)) {
                tabelaEstoque.getSelectionModel().select(item);
                tabelaEstoque.scrollTo(item);
                return item;
            }
        }
        return null;
    }

    @FXML
    private void aoClicarExcluir() {
        Estoque selecionado = tabelaEstoque.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarErro("Selecione um produto para excluir.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Excluir \"" + selecionado.getProduto() + "\" do estoque?", ButtonType.YES, ButtonType.NO);
        confirmacao.setHeaderText(null);
        confirmacao.showAndWait()
                .filter(resposta -> resposta == ButtonType.YES)
                .ifPresent(resposta -> {
                    try {
                        viewModel.deletarEstoque(selecionado.getId());
                        if (selecionado.getId().equals(idEmEdicao)) {
                            limparFormulario();
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

    private void aplicarFiltro() {
        String termo = normalizar(campoBusca.getText());
        Categoria categoria = filtroCategoria.getValue();
        Marca marca = filtroMarca.getValue();

        itensFiltrados.setPredicate(item ->
                (categoria == null || item.getCategoria() == categoria)
                        && (marca == null || item.getMarca() == marca)
                        && (termo.isEmpty() || normalizar(item.getProduto()).contains(termo)));
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

    private void atualizarTotal() {
        int totalCadastrados = viewModel.getItens().size();
        int totalVisiveis = itensFiltrados.size();
        int totalUnidades = itensFiltrados.stream().mapToInt(Estoque::getQuantidade).sum();

        if (totalVisiveis == totalCadastrados) {
            labelTotal.setText(totalCadastrados + " produto(s) · " + totalUnidades + " unidade(s)");
        } else {
            labelTotal.setText("Mostrando " + totalVisiveis + " de " + totalCadastrados
                    + " produto(s) · " + totalUnidades + " unidade(s)");
        }

        labelTabelaVazia.setText(totalCadastrados == 0
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
