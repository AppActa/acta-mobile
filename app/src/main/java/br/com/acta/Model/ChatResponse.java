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
public class ChatResponse {

    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("resposta")
    private String resposta;

    // Presente apenas na resposta de /chat/audio.
    @SerializedName("transcricao")
    private String transcricao;
}
