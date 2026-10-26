package br.com.acta.Model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MensagemItem {

    @SerializedName("_id")
    private String id;

    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("role")
    private String role;

    @SerializedName("content")
    private String content;

    @SerializedName("agent")
    private String agent;

    @SerializedName("metadata")
    private Map<String, Object> metadata;

    @SerializedName("criada_em")
    private String criadaEm;
}
