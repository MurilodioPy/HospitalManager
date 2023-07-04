package model.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import controller.FinanceiroADMController;
import controller.MedicoController;
import controller.PessoaController;
import controller.UnidadeController;
import java.sql.ResultSet;
import java.time.ZoneOffset;

import model.ConnectionFactory;
import model.Consulta;
import model.Medico;
import model.Pessoa;
import model.Unidade;
import model.enums.EstadoConsulta;
import model.enums.TipoMovimento;

public class ConsultaDAO {

    private Connection connection = null;

    public ConsultaDAO() {
        this.connection = ConnectionFactory.getConnection();
    }

    private static List<Consulta> consultas = new ArrayList<>();

    public void cadastrarConsulta(LocalDateTime data, String hora, String estadoConsulta, Medico medico, Pessoa paciente, double valor, Unidade unidade) {
        String sql = "INSERT INTO consultas (data, hora, estado, medico, paciente, valor, unidade, dataCriacao, dataModificacao) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, data);
            stmt.setString(2, hora);
            stmt.setString(3, estadoConsulta);
            stmt.setInt(4, medico.getId());
            stmt.setInt(5, paciente.getId());
            stmt.setDouble(6, valor);
            stmt.setInt(7, unidade.getIdUnidade());
            stmt.setObject(8, LocalDateTime.now());
            stmt.setObject(9, LocalDateTime.now());
            stmt.executeUpdate();

            Consulta consulta = new Consulta();
            double entradaFranquia = calcularEntradaFranquia(valor);
            FinanceiroADMController.cadastrarFinanceiro(TipoMovimento.ENTRADA, entradaFranquia, unidade.getNome(), "Consulta #" + consulta.getId());
        } catch (SQLException e) {
            System.out.println("Erro ao cadastrar a consulta no banco de dados: " + e.getMessage());
        }
    }

    private double calcularEntradaFranquia(double valor) {
        return valor * 0.2;
    }

    public void atualizarConsulta(int id, LocalDateTime data, String hora, String estado, Medico medico, Pessoa paciente, double valor, Unidade unidade) {
        String sql = "UPDATE consulta SET data = ?, hora = ?, estado = ?, medico = ?, paciente = ?, valor = ?, unidade = ?, dataModificacao = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, data);
            stmt.setString(2, hora);
            stmt.setString(3, estado);
            stmt.setInt(4, medico.getId());
            stmt.setInt(5, paciente.getId());
            stmt.setDouble(6, valor);
            stmt.setInt(7, unidade.getId());
            stmt.setObject(8, LocalDateTime.now());
            stmt.setInt(9, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar a consulta no banco de dados: " + e.getMessage());
        }
    }

    public static void removerConsulta(int id) {
        consultas.removeIf(consulta -> consulta.getId() == id);
    }

    public Consulta buscarConsulta(int id) {
        String sql = "SELECT * FROM consultas WHERE id = ?";
        MedicoController medicoControl = new MedicoController();
        PessoaController pessoaControl = new PessoaController();
        UnidadeController unidadeControl = new UnidadeController();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                java.sql.Timestamp data = rs.getTimestamp("data");
                java.sql.Timestamp timestamp = rs.getTimestamp("DataCriacao");
                java.sql.Timestamp dataMod = rs.getTimestamp("DataModificacao");
                LocalDateTime dataConsulta = data.toLocalDateTime();
                LocalDateTime dataCriacao = timestamp.toLocalDateTime();
                LocalDateTime dataModificacao = dataMod.toLocalDateTime();
                int idMedico = rs.getInt("medico");
                int idPaciente = rs.getInt("paciente");
                int idUnidade = rs.getInt("unidade");
                Medico med = medicoControl.buscarMedico(idMedico);
                Pessoa pac = pessoaControl.buscarPessoaPorId(idPaciente);
                Unidade uni = unidadeControl.buscarUnidade(idUnidade);

                Consulta c = new Consulta();

                c.setId(rs.getInt("id"));
                c.setData(dataConsulta);
                c.setHora(rs.getString("hora"));
                c.setEstado(rs.getString("estado"));
                c.setMedico(med);
                c.setPaciente(pac);
                c.setValor(rs.getDouble("valor"));
                c.setUnidade(uni);
                c.setDataCriacao(dataCriacao);
                c.setDataModificacao(dataModificacao);

                return c;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar consulta por ID!");
        }
        return null;
    }

    public List<Consulta> listarConsultas() {
        List<Consulta> consultas = new ArrayList<>();
        try {
            PreparedStatement stmt;
            stmt = connection.prepareStatement("select * from consultas");
            ResultSet rs;
            rs = stmt.executeQuery();
            // itera no ResultSet
            while (rs.next()) {
                Consulta c = new Consulta();
                c = buscarConsulta(rs.getInt("id"));
                consultas.add(c);
            }
            return consultas;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Consulta> listarConsultasPorMedico(Medico medico) {
        List<Consulta> consultasMedico = new ArrayList<>();
        MedicoController medControl = new MedicoController();
        try {
            PreparedStatement stmt;
            stmt = connection.prepareStatement("select * from consultas");
            ResultSet rs;
            rs = stmt.executeQuery();
            // itera no ResultSet
            while (rs.next()) {
                Consulta c = new Consulta();
                int idMedico = rs.getInt("medico");
                Medico med = medControl.buscarMedico(idMedico);
                if (med.getId() == medico.getId()) {
                    c = buscarConsulta(rs.getInt("id"));
                    consultasMedico.add(c);
                }
            }
            return consultasMedico;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Consulta> pesquisarConsultasPorMedicoNoPeriodo(Medico medico, LocalDateTime dataInicio, LocalDateTime dataFim) {
        List<Consulta> consultasMedico = new ArrayList<>();
        for (Consulta consulta : consultas) {
            LocalDateTime dataConsulta = consulta.getData();
            if (consulta.getMedico().equals(medico) && dataConsulta.isAfter(dataInicio) && dataConsulta.isBefore(dataFim)) {
                consultasMedico.add(consulta);
            }
        }
        return consultasMedico;
    }

    public static List<Consulta> pesquisarConsultasPorMedicoNoPeriodo(Medico medico, long dataInicio, long dataFim) {
        List<Consulta> consultasMedico = new ArrayList<>();
        for (Consulta consulta : consultas) {
            LocalDateTime dataConsulta = consulta.getData();
            if (consulta.getMedico().equals(medico) && dataConsulta.toEpochSecond(ZoneOffset.UTC)
                    >= dataInicio && dataConsulta.toEpochSecond(ZoneOffset.UTC) <= dataFim) {
                consultasMedico.add(consulta);
            }
        }
        return consultasMedico;
    }

    public static List<Consulta> listarConsultasPorPaciente(Pessoa paciente) {
        List<Consulta> consultasPaciente = new ArrayList<>();
        for (Consulta consulta : consultas) {
            if (consulta.getPaciente().equals(paciente)) {
                consultasPaciente.add(consulta);
            }
        }
        return consultasPaciente;
    }

    public static List<Consulta> listarConsultasPorFiltro(LocalDateTime dataInicio, LocalDateTime dataFim, Medico medico) {
        List<Consulta> consultasFiltradas = new ArrayList<>();
        for (Consulta consulta : consultas) {
            LocalDateTime dataConsulta = consulta.getData();
            if (dataConsulta.compareTo(dataInicio) >= 0 && dataConsulta.compareTo(dataFim) <= 0
                    && consulta.getMedico().equals(medico)) {
                consultasFiltradas.add(consulta);
            }
        }
        return consultasFiltradas;
    }
}
