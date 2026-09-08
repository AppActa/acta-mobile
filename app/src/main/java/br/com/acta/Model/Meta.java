package br.com.acta.Model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Meta {

    @SerializedName("id")
    private Long id;

    @SerializedName("objetivo")
    private String objetivo;

    @SerializedName("valorBase")
    private Double valorBase;

    @SerializedName("valorAlvo")
    private Double valorAlvo;

    @SerializedName("unidadeMedida")
    private String unidadeMedida;

    @SerializedName("prazo")
    private String prazo;

    @SerializedName("status")
    private String status;

    @SerializedName("prioridade")
    private String prioridade;

    @SerializedName("area")
    private String area;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("idCiclo")
    private Long idCiclo;

    @SerializedName("idPlanoAcao")
    private Long idPlanoAcao;

    @SerializedName("responsaveis")
    private List<Usuario> responsaveis;

}