package br.com.acta.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import br.com.acta.Model.Enum.TipoResposta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Pergunta {

    @SerializedName("id")
    private String id;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("tipo")
    private TipoResposta tipo;

    @SerializedName("obrigatoria")
    private Boolean obrigatoria;

    @SerializedName("opcoes")
    private List<String> opcoes;
}
