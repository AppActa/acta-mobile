package br.com.acta.Model;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Treinamento {

    @SerializedName("id")
    private Long id;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("descricao")
    private String descricao;

    @SerializedName("dataTreinamento")
    private String dataTreinamento;

    @SerializedName("obrigatorio")
    private Boolean obrigatorio;

    @SerializedName("idCiclo")
    private Long idCiclo;

    @SerializedName("idResponsavel")
    private Long idResponsavel;

    @SerializedName("ciclo")
    private Ciclo ciclo;

    @SerializedName("responsavel")
    private Usuario responsavel;

    @SerializedName("participantes")
    private Set<UsuarioTreinamento> participantes;
}
