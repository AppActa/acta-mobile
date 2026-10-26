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
public class ChatItem {

    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("status")
    private String status;

    @SerializedName("total_mensagens")
    private Integer totalMensagens;

    @SerializedName("iniciada_em")
    private String iniciadaEm;

    @SerializedName("atualizada_em")
    private String atualizadaEm;

    @SerializedName("encerrada_em")
    private String encerradaEm;
}
