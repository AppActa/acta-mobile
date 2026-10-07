package br.com.acta.Model;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RespostaPergunta {

    @SerializedName("idPergunta")
    private String idPergunta;

    @SerializedName("resposta")
    private Object resposta;
}
