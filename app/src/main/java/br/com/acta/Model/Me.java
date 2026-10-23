package br.com.acta.Model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(tableName = "tb_me")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Me {
    private String firebaseUid;

    @PrimaryKey
    private Long idUsuario;
    private Long idEmpresa;
    private Long idColaborador;
    private String nome;
    private String email;
    private String nomeEmpresa;
    private String tipo;
    private boolean permissaoGestor;
    private String status;
    @Ignore
    private String estadoPerfil;

    public boolean temPerfilActa() {
        return "PERFIL_CRIADO".equals(estadoPerfil) && idUsuario != null && idEmpresa != null;
    }

    public boolean cadastroNaoIniciado() {
        return "CADASTRO_NAO_INICIADO".equals(estadoPerfil);
    }
}