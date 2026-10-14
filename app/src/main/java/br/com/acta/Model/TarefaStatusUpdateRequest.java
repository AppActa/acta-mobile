package br.com.acta.Model;

import com.google.gson.annotations.SerializedName;

import br.com.acta.Model.Enum.StatusTarefa;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TarefaStatusUpdateRequest {

    @SerializedName("dataInicioReal")
    private String dataInicioReal;

    @SerializedName("dataFimReal")
    private String dataFimReal;

    @SerializedName("status")
    private StatusTarefa status;
}
