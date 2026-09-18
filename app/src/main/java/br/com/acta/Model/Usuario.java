package br.com.acta.Model;

import java.time.OffsetDateTime;
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
public class Usuario {
    private Long id;

    private String nome;

    private String email;

    private TipoUsuario tipo;

    private Long idEmpresa;

    private StatusGeral status;

    private Set<UsuarioCiclo> ciclos;

    private OffsetDateTime criadoEm;

    private OffsetDateTime atualizadoEm;
    private Set<Meta> metas;
    private String fotoUrl;

    public Usuario(Long id, String nome, String email, TipoUsuario tipo, Long idEmpresa, StatusGeral status, Set<UsuarioCiclo> ciclos, OffsetDateTime criadoEm, OffsetDateTime atualizadoEm, Set<Meta> metas, String fotoUrl) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.tipo = tipo;
        this.idEmpresa = idEmpresa;
        this.status = status;
        this.ciclos = ciclos;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
        this.metas = metas;
        this.fotoUrl = fotoUrl;
    }

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

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public OffsetDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(OffsetDateTime atualizadoEm) {
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
