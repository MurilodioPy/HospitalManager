package model;

import java.util.Date;
import java.util.Random;

public class Medico extends Pessoa {

    private int crm;
    private String especialidade;
    private Date dataCriacao;
    private Date dataModificacao;

    public Medico(int id, String nome, String endereco, String cpf, String telefone, String login, String senha,
            int crm, String especialidade, Date dataCriacao, Date dataModificacao) {
        super(nome, endereco, cpf, telefone, login, senha, especialidade, dataModificacao, dataModificacao);
        this.crm = crm;
        this.especialidade = especialidade;
        this.dataCriacao = dataCriacao;
        this.dataModificacao = dataModificacao;
    }

    public int getCrm() {
        return crm;
    }

    public void setCrm(int novoCrm) {
        this.crm = novoCrm;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Date getDataModificacao() {
        return dataModificacao;
    }

    public void setDataModificacao(Date dataModificacao) {
        this.dataModificacao = dataModificacao;
    }
    
    public static Medico gerarMedicoAleatorio() {
        String[] especialidades = {"Cardiologia", "Neurologia", "Ortopedia", "Pediatria", "Dermatologia"};
        String[] nomes = {"Ana", "Maria", "João", "Pedro", "Luiza", "Gabriel", "Lucas", "Juliana", "Renata", "Thiago"};
        String[] sobrenomes = {"Silva", "Souza", "Fernandes", "Almeida", "Costa", "Pereira", "Santos", "Lima", "Mendes", "Nascimento"};
        String[] logins = {"ana.silva", "joao.pereira", "lucia.costa", "renata.mendes", "thiago.nascimento"};
        String[] senhas = {"123456", "senha123", "senha1234", "senha12345", "senha123456"};

        Random random = new Random();

        String nome = nomes[random.nextInt(nomes.length)] + " " + sobrenomes[random.nextInt(sobrenomes.length)];
        String endereco = "Rua " + (random.nextInt(9999) + 1) + ", Bairro " + (random.nextInt(99) + 1);
        String cpf = String.format("%03d.%03d.%03d-%02d", random.nextInt(1000), random.nextInt(1000), random.nextInt(1000), random.nextInt(100));
        String telefone = "(" + (random.nextInt(99) + 1) + ") " + (random.nextInt(99999) + 1) + "-" + (random.nextInt(9999) + 1);
        String login = logins[random.nextInt(logins.length)] + (random.nextInt(99) + 1);
        String senha = senhas[random.nextInt(senhas.length)];
        String especialidade = especialidades[random.nextInt(especialidades.length)];
        int crm = random.nextInt(999999);

        Date dataCriacao = new Date();
        Date dataModificacao = new Date();

        Medico medico = new Medico(0, nome, endereco, cpf, telefone, login, senha, crm, especialidade, dataCriacao, dataModificacao);

        return medico;
    }
    @Override
    public String toString() {
        return getNome() + ": {"
                + "ID: " + getId()
                + ", Endereco: " + getEndereco()
                + ", CPF: " + getCpf()
                + ", Telefone : " + getTelefone()
                + ", Login: " + getLogin()
                + ", TipoUsuario: " + getTipoUsuario()
                +", CRM: "+crm
                +", Especialidade: "+especialidade
                + ", dataCriacao: " + dataCriacao
                + ", dataModificacao: " + dataModificacao
                + "}";
    }
}
