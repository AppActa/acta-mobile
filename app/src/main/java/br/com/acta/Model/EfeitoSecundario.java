package br.com.acta.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import br.com.acta.Model.Enum.TipoEfeitoSecundario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(tableName = "tb_efeito_secundario")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EfeitoSecundario {

    @PrimaryKey
    @SerializedName("id")
    private Long id;

    @SerializedName("descricao")
    private String descricao;

    @SerializedName("peso")
    private Double peso;

    @SerializedName("impactoEstimado")
    private String impactoEstimado;

    @SerializedName("idVerificacaoResultado")
    private Long idVerificacaoResultado;

    @SerializedName("tipo")
    private TipoEfeitoSecundario tipo;
}
