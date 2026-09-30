package br.com.acta.Model;

import br.com.acta.Model.Enum.PapelCiclo;
import br.com.acta.Model.Id.UsuarioCicloId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class UsuarioCiclo {
    private UsuarioCicloId id;

    private Usuario usuario;

    private Ciclo ciclo;

    private PapelCiclo papelCiclo;
}
