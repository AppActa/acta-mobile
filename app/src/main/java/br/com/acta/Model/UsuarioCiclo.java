package br.com.acta.Model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import br.com.acta.Model.Enum.PapelCiclo;
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

    @Ignore
    private UsuarioCicloId id;

    @Ignore
    private Usuario usuario;

    @Ignore
    private Ciclo ciclo;

    @Ignore
    private PapelCiclo papelCiclo;

    public Long getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(Long idLocal) {
        this.idLocal = idLocal;
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
