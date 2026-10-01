package br.com.acta.Model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import java.util.Set;

import br.com.acta.Model.Enum.StatusGeral;
import br.com.acta.Model.Enum.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity(tableName = "usuario_sistema")
public class Usuario {
    @PrimaryKey
    private Long id;

    private String nome;

    private String email;

    private TipoUsuario tipo;

    private Long idEmpresa;

    private StatusGeral status;
    @Ignore
    private Set<UsuarioCiclo> ciclos;

    private String criadoEm;

    private String atualizadoEm;
    @Ignore
    private Set<Meta> metas;

    @SerializedName(value = "fotoUrl", alternate = {"url_foto", "urlFoto", "foto_url"})
    private String fotoUrl;


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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public StatusGeral getStatus() {
        return status;
    }

    public void setStatus(StatusGeral status) {
        this.status = status;
    }

    public Set<UsuarioCiclo> getCiclos() {
        return ciclos;
    }

    public void setCiclos(Set<UsuarioCiclo> ciclos) {
        this.ciclos = ciclos;
    }

    public String getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(String criadoEm) {
        this.criadoEm = criadoEm;
    }

    public String getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(String atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public Set<Meta> getMetas() {
        return metas;
    }

    public void setMetas(Set<Meta> metas) {
        this.metas = metas;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }
}
