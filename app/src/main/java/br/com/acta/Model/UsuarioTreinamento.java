package br.com.acta.Model;

import java.time.OffsetDateTime;

import br.com.acta.Model.Enum.StatusTreinamento;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UsuarioTreinamento {
    private Long idUsuario;

    private String nomeUsuario;

    private StatusTreinamento status;

    private Boolean obrigatorio;

    private OffsetDateTime terminadoEm;
}
