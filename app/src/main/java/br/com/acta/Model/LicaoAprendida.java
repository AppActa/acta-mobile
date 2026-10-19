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
public class LicaoAprendida {

    @SerializedName("id")
    private String id;

    @SerializedName("idUsuario")
    private Long idUsuario;

    @SerializedName("idEmpresa")
    private Long idEmpresa;

    @SerializedName("idCiclo")
    private Long idCiclo;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("licao")
    private String licao;

    @SerializedName("tags")
    private List<String> tags;

    @SerializedName("faseOrigem")
    private String faseOrigem;

    @SerializedName("resultado")
    private String resultado;

    @SerializedName("severidade")
    private String severidade;

    @SerializedName("area")
    private String area;

    @SerializedName("criadoEm")
    private String criadoEm;

    @SerializedName("atualizadoEm")
    private String atualizadoEm;
}
