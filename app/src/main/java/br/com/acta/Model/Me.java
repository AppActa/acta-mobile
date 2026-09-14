package br.com.acta.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Me {
    private String firebaseUid;

    private Long idUsuario;

    private Long idEmpresa;

    private Long idColaborador;

    private String nome;

    private String email;

    private String nomeEmpresa;

    private String tipo;

    private boolean permissaoGestor;

    private String status;

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public Long getIdColaborador() {
        return idColaborador;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }

    public String getTipo() {
        return tipo;
    }

    public boolean isPermissaoGestor() {
        return permissaoGestor;
    }

    public String getStatus() {
        return status;
    }
}
