package br.com.acta.Model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

import br.com.acta.Model.Contatos.Email;
import br.com.acta.Model.Contatos.Telefone;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(tableName = "tb_colaborador")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Colaborador {
    @PrimaryKey
    private Long id;
    private String nome;

    @Ignore
    private List<Email> email;

    @Ignore
    private List<Telefone> telefone;

    private String cpf;
    private String cargo;
    private String area;
    private String dataNascimento;
    private String dataContratacao;
    private Boolean permissaoGestor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Email> getEmail() {
        return email;
    }

    public void setEmail(List<Email> email) {
        this.email = email;
    }

    public List<Telefone> getTelefone() {
        return telefone;
    }

    public void setTelefone(List<Telefone> telefone) {
        this.telefone = telefone;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getDataContratacao() {
        return dataContratacao;
    }

    public void setDataContratacao(String dataContratacao) {
        this.dataContratacao = dataContratacao;
    }

    public Boolean getPermissaoGestor() {
        return permissaoGestor;
    }

    public void setPermissaoGestor(Boolean permissaoGestor) {
        this.permissaoGestor = permissaoGestor;
    }
}
