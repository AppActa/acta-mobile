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
public class MensagensResponse {

    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("mensagens")
    private List<MensagemItem> mensagens;
}
