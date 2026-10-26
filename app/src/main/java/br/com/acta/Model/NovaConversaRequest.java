package br.com.acta.Model;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NovaConversaRequest {

    @SerializedName("usuario_id")
    private Integer usuarioId;

    @SerializedName("empresa_id")
    private Integer empresaId;

    @SerializedName("session_id_atual")
    private String sessionIdAtual;
}
