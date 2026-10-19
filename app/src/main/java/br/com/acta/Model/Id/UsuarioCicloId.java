package br.com.acta.Model.Id;

import java.io.Serializable;
import java.util.Objects;

public class UsuarioCicloId implements Serializable {
    private Long idUsuario;

    private Long idCiclo;

    public UsuarioCicloId() {
    }

    public UsuarioCicloId(Long idUsuario, Long idCiclo) {
        this.idUsuario = idUsuario;
        this.idCiclo = idCiclo;
    }

    // Getters e Setters
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioCicloId that = (UsuarioCicloId) o;
        return Objects.equals(idUsuario, that.idUsuario) &&
                Objects.equals(idCiclo, that.idCiclo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario, idCiclo);
    }
}
