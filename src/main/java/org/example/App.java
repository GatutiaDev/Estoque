package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.repository.EstoqueRepository;
import org.example.repository.RepositoryEstoque;
import org.example.service.EstoqueService;
import org.example.validations.ValidacoesEstoque;
import org.example.view.MainController;
import org.example.viewmodel.EstoqueViewModel;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        EstoqueRepository repository = new RepositoryEstoque();
        EstoqueService service = new EstoqueService(repository, ValidacoesEstoque.todas());
        EstoqueViewModel viewModel = new EstoqueViewModel(service);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
        Parent raiz = loader.load();

        MainController controller = loader.getController();
        controller.setViewModel(viewModel);

        Scene cena = new Scene(raiz);
        cena.getStylesheets().add(getClass().getResource("/css/estilo.css").toExternalForm());

        stage.setTitle("Estoque");
        stage.setScene(cena);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
