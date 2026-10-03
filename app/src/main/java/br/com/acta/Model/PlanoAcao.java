package br.com.acta.Model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import br.com.acta.Model.Enum.OrigemRegistro;
import br.com.acta.Model.Enum.Prioridade;
import br.com.acta.Model.Enum.StatusPlanoAcao;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(tableName = "tb_plano_acao")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PlanoAcao {

    @PrimaryKey
    @SerializedName("id")
    private Long id;

    @SerializedName("nome")
    private String nome;

    @SerializedName("objetivo")
    private String objetivo;

    @Ignore
    @SerializedName("prioridade")
    private Prioridade prioridade;

    @Ignore
    @SerializedName("status")
    private StatusPlanoAcao status;

    @Ignore
    @SerializedName("origem")
    private OrigemRegistro origem;

    @SerializedName("idCiclo")
    private Long idCiclo;

    @SerializedName("idCriadoPor")
    private Long idCriadoPor;

    @Ignore
    @SerializedName("ciclo")
    private Ciclo ciclo;

    @Ignore
    @SerializedName("criadoPor")
    private Usuario criadoPor;

    @Ignore
    @SerializedName("plano5W2H")
    private Plano5W2H plano5W2H;
}
