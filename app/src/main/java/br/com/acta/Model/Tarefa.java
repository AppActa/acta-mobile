package br.com.acta.Model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import br.com.acta.Model.Enum.Prioridade;
import br.com.acta.Model.Enum.StatusTarefa;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(tableName = "tb_tarefa")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Tarefa {

    @PrimaryKey
    @SerializedName("id")
    private Long id;

    @SerializedName("prioridade")
    private Prioridade prioridade;

    @SerializedName("status")
    private StatusTarefa status;

    @SerializedName("dataInicioReal")
    private String dataInicioReal;

    @SerializedName("dataFimPrevista")
    private String dataFimPrevista;

    @SerializedName("dataFimReal")
    private String dataFimReal;

    @SerializedName("idPlanoAcao")
    private Long idPlanoAcao;

    @SerializedName("idResponsavel")
    private Long idResponsavel;

    @Ignore
    @SerializedName("planoAcao")
    private PlanoAcao planoAcao;

    @Ignore
    @SerializedName("responsavel")
    private Usuario responsavel;
}
