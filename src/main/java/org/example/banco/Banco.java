package org.example.banco;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Banco {
    private static Connection conexao;

    private Banco() {
    }

    public static Connection conexao() {
        if (conexao == null) {
            conexao = criarConexao();
            criarTabelaSeNaoExistir(conexao);
            adicionarColunaMarcaSeNaoExistir(conexao);
        }
        return conexao;
    }

    private static Connection criarConexao() {
        try {
            String appData = System.getenv("APPDATA");
            Path pastaApp = Paths.get(appData, "estoqueNatura");
            Files.createDirectories(pastaApp);

            Path arquivoDb = pastaApp.resolve("estoqueNatura.db");
            String url = "jdbc:sqlite:" + arquivoDb;

            return DriverManager.getConnection(url);
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Não foi possível conectar ao banco de dados", e);
        }
    }

    private static void criarTabelaSeNaoExistir(Connection conexao) {
        String sql = """
                CREATE TABLE IF NOT EXISTS estoque (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    produto TEXT NOT NULL,
                    categoria TEXT NOT NULL,
                    quantidade INTEGER NOT NULL,
                    caixa INTEGER NOT NULL,
                    data_vencimento TEXT NOT NULL,
                    marca TEXT NOT NULL
                    
                )
                """;

        try (Statement statement = conexao.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Não foi possível criar a tabela de estoque", e);
        }
    }

    // Bancos criados antes da marca existir não têm essa coluna: adiciona e marca os produtos antigos como NATURA
    private static void adicionarColunaMarcaSeNaoExistir(Connection conexao) {
        try (Statement statement = conexao.createStatement();
             ResultSet colunas = statement.executeQuery("PRAGMA table_info(estoque)")) {

            while (colunas.next()) {
                if ("marca".equals(colunas.getString("name"))) {
                    return;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Não foi possível verificar a tabela de estoque", e);
        }

        try (Statement statement = conexao.createStatement()) {
            statement.execute("ALTER TABLE estoque ADD COLUMN marca TEXT NOT NULL DEFAULT 'NATURA'");
        } catch (SQLException e) {
            throw new RuntimeException("Não foi possível adicionar a coluna marca", e);
        }
    }
}
