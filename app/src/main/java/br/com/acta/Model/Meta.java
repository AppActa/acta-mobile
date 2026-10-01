package br.com.acta.Model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(tableName = "tb_meta")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Meta {

    @PrimaryKey
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

    @Ignore
    @SerializedName("responsaveis")
    private List<Usuario> responsaveis;

}
