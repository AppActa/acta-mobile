package br.com.acta;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
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
}
