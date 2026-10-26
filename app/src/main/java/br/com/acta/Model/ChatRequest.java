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
public class ChatRequest {

    @SerializedName("message")
    private String message;

    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("id_ciclo")
    private List<Integer> idCiclo;

    @SerializedName("ciclo_ativo")
    private Integer cicloAtivo;

    @SerializedName("usuario_id")
    private Integer usuarioId;

    @SerializedName("empresa_id")
    private Integer empresaId;
}
