package br.com.acta.Model;

import com.google.gson.annotations.SerializedName;

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
    @SerializedName("idUsuario")
    private Long idUsuario;

    @SerializedName("idTreinamento")
    private Long idTreinamento;

    @SerializedName("nomeUsuario")
    private String nomeUsuario;

    @SerializedName("status")
    private StatusTreinamento status;

    @SerializedName("obrigatorio")
    private Boolean obrigatorio;

    @SerializedName("terminadoEm")
    private String terminadoEm;
}
