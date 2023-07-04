package model.DAO;

import controller.MedicoController;
import controller.PessoaController;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Medico;
import model.ConnectionFactory;
import model.Pessoa;

public class MedicoDao {

    private Connection connection = null;

    public MedicoDao() {
        this.connection = ConnectionFactory.getConnection();
    }
    private static List<Pessoa> medicos = new ArrayList<>();

    public boolean cadastrarMedico(String nome, String endereco, String cpf, String telefone, String login,
            String senha, String tipoUsuario, int crm, String especialidade) {
        PessoaDAO dao = new PessoaDAO();
        Pessoa pessoa = new Pessoa();
        pessoa.setNome(nome);
        pessoa.setEndereco(endereco);
        pessoa.setCpf(cpf);
        pessoa.setTelefone(telefone);
        pessoa.setLogin(login);
        pessoa.setSenha(senha);
        pessoa.setTipoUsuario(tipoUsuario);
        pessoa.setDataCriacao(LocalDateTime.now());
        pessoa.setDataModificacao(LocalDateTime.now());

        Medico medico = new Medico(pessoa);
        medico.setCrm(crm);
        medico.setEspecialidade(especialidade);
        medico.setDataCriacao(LocalDateTime.now());
        medico.setDataModificacao(LocalDateTime.now());

        int id = dao.cadastrarPessoaMedico(pessoa);

        String sql = "INSERT INTO medicos "
                + "(idPessoas, crm, especialidade, dataCriacao, dataModificacao) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, String.valueOf(id));
            stmt.setString(2, String.valueOf(crm));
            stmt.setString(3, especialidade);
            LocalDateTime now = LocalDateTime.now();
            java.sql.Timestamp dateNow = java.sql.Timestamp.valueOf(now);
            stmt.setTimestamp(4, dateNow);
            stmt.setTimestamp(5, dateNow);

            stmt.executeUpdate();
            // Adicionar a pessoa ao ArrayList
            MedicoDao.medicos.add(medico);
            System.out.println("Pessoa adicionada com sucesso.");
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao cadastrar médico!");
        }
        return false;
    }

    public boolean editarMedico(int id, String login, String novoNome, String novoEndereco, String novoCpf, String novoTelefone, int novoCrm, String novaEspecialidade) {
        try (Connection connection = ConnectionFactory.getConnection(); 
                PreparedStatement statement = connection.prepareStatement("UPDATE medicos SET nome = ?, endereco = ?, cpf = ?, telefone = ?, crm = ?, especialidade = ? WHERE id = ?")) {
            statement.setString(1, novoNome);
            statement.setString(2, novoEndereco);
            statement.setString(3, novoCpf);
            statement.setString(4, novoTelefone);
            statement.setInt(5, novoCrm);
            statement.setString(6, novaEspecialidade);
            statement.setInt(7, id);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao editar médico!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean excluirMedico(int id) {
        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM medicos WHERE id = ?")) {
            statement.setInt(1, id);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Erro ao excluir médico!");
            e.printStackTrace();
            return false;
        }
    }

    public Medico buscarMedico(int id) {
        String sql = "SELECT * FROM medicos WHERE id = ?";
        PessoaController pessoaControl = new PessoaController();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Medico med = new Medico();
                med.setId(rs.getInt("id"));
                med.setCrm(rs.getInt("crm"));
                med.setEspecialidade(rs.getString("especialidade"));
                med.setPessoa(pessoaControl.buscarPessoaPorId(rs.getInt("idPessoas")));
                java.sql.Timestamp timestamp = rs.getTimestamp("DataCriacao");
                java.sql.Timestamp dataMod = rs.getTimestamp("DataModificacao");
                LocalDateTime dataCriacao = timestamp.toLocalDateTime();
                LocalDateTime dataModificacao = dataMod.toLocalDateTime();
                med.setDataCriacao(dataCriacao);
                med.setDataModificacao(dataModificacao);
                return med;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar medico por ID!");
        }
        return null; // Médico não encontrado
    }

    public Medico buscarMedicoPorCRM(int crm) {
        MedicoController medControl = new MedicoController();
        PessoaController pessoaControl = new PessoaController();
        try (Connection connection = ConnectionFactory.getConnection(); 
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM medicos WHERE crm = ?")) {
            statement.setInt(1, crm);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Medico medico = medControl.buscarMedico(rs.getInt("id"));
                    medico.setPessoa(pessoaControl.buscarPessoaPorId(rs.getInt("idPessoas")));
                    
                    return medico;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar médico por CRM!");
            e.printStackTrace();
        }
        return null; // Médico não encontrado
    }

    public List<Medico> listarMedicos() {
        List<Medico> medicos = new ArrayList<>();
        MedicoController medControl = new MedicoController();
        PessoaController pessoaControl = new PessoaController();
        try (Connection connection = ConnectionFactory.getConnection(); 
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM medicos")) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Medico medico = medControl.buscarMedico(rs.getInt("id"));
                    medico.setPessoa(pessoaControl.buscarPessoaPorId(rs.getInt("idPessoas")));
                    medicos.add(medico);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar médicos!");
            e.printStackTrace();
        }
        return medicos;
    }

    private Medico criarMedicoAPartirDoResultSet(ResultSet result) throws SQLException {
        int id = result.getInt("id");
        String nome = result.getString("nome");
        String endereco = result.getString("endereco");
        String cpf = result.getString("cpf");
        String telefone = result.getString("telefone");
        String login = result.getString("login");
        String senha = result.getString("senha");
        int crm = result.getInt("crm");
        java.sql.Timestamp timestamp = result.getTimestamp("DataCriacao");
        java.sql.Timestamp dataMod = result.getTimestamp("DataModificacao");
        LocalDateTime dataCriacao = timestamp.toLocalDateTime();
        LocalDateTime dataModificacao = dataMod.toLocalDateTime();
        String especialidade = result.getString("especialidade");

        return new Medico(id, nome, endereco, cpf, telefone, login, senha, crm, dataCriacao, dataModificacao, especialidade);
    }
}
