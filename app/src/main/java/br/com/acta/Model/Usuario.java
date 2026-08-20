package br.com.acta.Model;

import java.time.OffsetDateTime;

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

    private OffsetDateTime criadoEm;

    private OffsetDateTime atualizadoEm;
}
