package br.com.acta.Model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(tableName = "tb_plano_5w2h")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Plano5W2H {

    @PrimaryKey
    @SerializedName("id")
    private Long id;

    @SerializedName("whatAcao")
    private String whatAcao;

    @SerializedName("whyJustificativa")
    private String whyJustificativa;

    @SerializedName("whereLocal")
    private String whereLocal;

    @SerializedName("whenInicio")
    private String whenInicio;

    @SerializedName("whenFim")
    private String whenFim;

    @SerializedName("howModoExecucao")
    private String howModoExecucao;

    @SerializedName("howMuchCusto")
    private Double howMuchCusto;

    @SerializedName("idWhoResponsavel")
    private Long idWhoResponsavel;

    @SerializedName("idPlanoAcao")
    private Long idPlanoAcao;

    @Ignore
    @SerializedName("whoResponsavel")
    private Usuario whoResponsavel;
}
