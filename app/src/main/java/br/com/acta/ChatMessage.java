package br.com.acta;

import lombok.Getter;

@Getter
public class ChatMessage {
    public enum Type {
        WELCOME,
        SUGGESTIONS,
        USER,
        CATO,
        LOADING
    }

    private final Type type;
    private final String text;
    private final boolean transcricaoAudio;

    public ChatMessage(Type type, String text) {
        this(type, text, false);
    }

    public ChatMessage(Type type, String text, boolean transcricaoAudio) {
        this.type = type;
        this.text = text;
        this.transcricaoAudio = transcricaoAudio;
    }
}
