package org.example.repository;

import org.example.Model.Categoria;
import org.example.Model.Estoque;
import org.example.Model.Marca;
import org.example.banco.Banco;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositoryEstoque implements EstoqueRepository {

    private final Connection conn;

    public RepositoryEstoque() {
        conn = Banco.conexao();
    }

    @Override
    public void salvar(Estoque estoque) {
        if (estoque.getId() == null) {
            inserir(estoque);
        } else {
            atualizar(estoque);
        }
    }

    @Override
    public List<Estoque> listarTodas() {

        String sql = """
                SELECT * FROM estoque
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {

            ResultSet lista = statement.executeQuery();
            List<Estoque> listaEstoque = new ArrayList<>();

            while (lista.next()) {
                listaEstoque.add(estoqueDaLista(lista));
            }
            return listaEstoque;
        } catch (SQLException e) {
            throw new RuntimeException("Não foi possível listar o estoque", e);
        }
    }

    @Override
    public void deletar(Long id) {
        String sql = """
                DELETE FROM estoque
                WHERE id = ?
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Não foi possível excluir o produto", e);
        }

    }

    @Override
    public Optional<Estoque> buscaPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Nao tem id");
        }
        return sqlBuscaPorId(id);
    }


    private Optional<Estoque> sqlBuscaPorId(Long id) {
        String sql = """
                SELECT * FROM estoque
                WHERE id = ?
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet dados = statement.executeQuery();

            if (dados.next()) {
                return Optional.of(estoqueDaLista(dados));
            } else {
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Não foi possível buscar o produto", e);
        }

    }

    private Estoque estoqueDaLista(ResultSet rs) throws SQLException {

        return new Estoque(rs.getLong(1),
                rs.getString(2),
                Categoria.valueOf(rs.getString(3)),
                rs.getInt(4),
                rs.getInt(5),
                LocalDate.parse(rs.getString(6)),
                Marca.valueOf(rs.getString(7)));
    }

    private void preencherParametros(PreparedStatement statement, Estoque estoque) throws SQLException {
        statement.setString(1, estoque.getProduto());
        statement.setString(2, String.valueOf(estoque.getCategoria()));
        statement.setInt(3, estoque.getQuantidade());
        statement.setInt(4, estoque.getCaixa());
        statement.setString(5, String.valueOf(estoque.getDataVencimento()));
        statement.setString(6, String.valueOf(estoque.getMarca()));
    }

    private void atualizar(Estoque estoque) {
        String sql = """
                UPDATE estoque
                SET produto = ? , categoria = ?, quantidade = ?, caixa = ?, data_vencimento = ?, marca = ?
                WHERE id = ?
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            preencherParametros(statement, estoque);
            statement.setLong(7, estoque.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Não foi possível atualizar o produto", e);
        }
    }

    private void inserir(Estoque estoque) {
        String sql = """
                INSERT INTO estoque (produto, categoria, quantidade, caixa, data_vencimento, marca)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            preencherParametros(statement, estoque);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Não foi possível salvar o produto", e);
        }
    }

}
