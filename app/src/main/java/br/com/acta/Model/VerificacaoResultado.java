package br.com.acta.Model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import br.com.acta.Model.Enum.StatusVerificacao;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(tableName = "tb_verificacao_resultado")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VerificacaoResultado {

    @PrimaryKey
    @SerializedName("id")
    private Long id;

    @SerializedName("status")
    private StatusVerificacao status;

    @SerializedName("resumo")
    private String resumo;

    @SerializedName("observacao")
    private String observacao;

    @SerializedName("idCriadoPor")
    private Long idCriadoPor;

    @SerializedName("idCiclo")
    private Long idCiclo;

    @SerializedName("criadoEm")
    private String criadoEm;

    @Ignore
    @SerializedName("criadoPor")
    private Usuario criadoPor;

    @Ignore
    @SerializedName("ciclo")
    private Ciclo ciclo;

    @Ignore
    @SerializedName("efeitosSecundarios")
    private List<EfeitoSecundario> efeitosSecundarios;
}
