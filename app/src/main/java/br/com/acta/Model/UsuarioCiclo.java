package br.com.acta.Model;

import br.com.acta.Model.Enum.PapelCiclo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class UsuarioCiclo {
    private Long idUsuario;

    private Long idCiclo;

    private String nomeUsuario;

    private PapelCiclo papelCiclo;
}
