package br.com.acta.Model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import br.com.acta.Model.Enum.PapelCiclo;
import br.com.acta.Model.Enum.StatusCiclo;
import br.com.acta.Model.Id.UsuarioCicloId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(tableName = "tb_usuario_ciclo")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UsuarioCiclo {
    @PrimaryKey(autoGenerate = true)
    private Long idLocal;

    @SerializedName("idUsuario")
    private Long idUsuario;

    @SerializedName("idCiclo")
    private Long idCiclo;

    @SerializedName("nomeUsuario")
    private String nomeUsuario;

    @SerializedName("nomeCiclo")
    private String nomeCiclo;

    @SerializedName("statusCiclo")
    private StatusCiclo statusCiclo;

    @SerializedName("iconeUrl")
    private String iconeUrl;

    private PapelCiclo papelCiclo;

    @Ignore
    private UsuarioCicloId id;

    @Ignore
    private Usuario usuario;

    @Ignore
    private Ciclo ciclo;

    public Long getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(Long idLocal) {
        this.idLocal = idLocal;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Long getIdCiclo() {
        return idCiclo;
    }

    public void setIdCiclo(Long idCiclo) {
        this.idCiclo = idCiclo;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getNomeCiclo() {
        return nomeCiclo;
    }

    public void setNomeCiclo(String nomeCiclo) {
        this.nomeCiclo = nomeCiclo;
    }

    public StatusCiclo getStatusCiclo() {
        return statusCiclo;
    }

    public void setStatusCiclo(StatusCiclo statusCiclo) {
        this.statusCiclo = statusCiclo;
    }

    public String getIconeUrl() {
        return iconeUrl;
    }

    public void setIconeUrl(String iconeUrl) {
        this.iconeUrl = iconeUrl;
    }

    public UsuarioCicloId getId() {
        return id;
    }

    public void setId(UsuarioCicloId id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Ciclo getCiclo() {
        return ciclo;
    }

    public void setCiclo(Ciclo ciclo) {
        this.ciclo = ciclo;
    }

    public PapelCiclo getPapelCiclo() {
        return papelCiclo;
    }

    public void setPapelCiclo(PapelCiclo papelCiclo) {
        this.papelCiclo = papelCiclo;
    }
}
