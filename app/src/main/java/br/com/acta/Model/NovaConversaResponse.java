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
public class NovaConversaResponse {

    @SerializedName("usuario_id")
    private Integer usuarioId;

    @SerializedName("empresa_id")
    private Integer empresaId;

    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("conversa_anterior_encerrada")
    private Boolean conversaAnteriorEncerrada;
}
