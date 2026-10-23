package br.com.acta.Services;

import br.com.acta.Api.ChatbotApi;
import br.com.acta.Client.RepositoryCallback;
import br.com.acta.Model.ChatAudioRequest;
import br.com.acta.Model.ChatRequest;
import br.com.acta.Model.ChatResponse;
import br.com.acta.Model.ListarChatsResponse;
import br.com.acta.Model.MensagensResponse;
import br.com.acta.Model.NovaConversaRequest;
import br.com.acta.Model.NovaConversaResponse;

public class ChatbotService {
    private final ChatbotApi api;
    public ChatbotService(ChatbotApi api) {
        this.api = api;
    }
    public void novaConversa(
            NovaConversaRequest request,
            RepositoryCallback<NovaConversaResponse> callback
    ) {
        Enqueue.enqueue(api.novaConversa(request), callback);
    }
    public void listarChats(
            Integer usuarioId,
            Integer empresaId,
            Integer limit,
            RepositoryCallback<ListarChatsResponse> callback
    ) {
        Enqueue.enqueue(api.listarChats(usuarioId, empresaId, limit), callback);
    }
    public void listarMensagens(
            String sessionId,
            Integer usuarioId,
            Integer empresaId,
            Integer limit,
            RepositoryCallback<MensagensResponse> callback
    ) {
        Enqueue.enqueue(
                api.listarMensagens(sessionId, usuarioId, empresaId, limit),
                callback
        );
    }
    public void enviarMensagem(
            ChatRequest request,
            RepositoryCallback<ChatResponse> callback
    ) {
        Enqueue.enqueue(api.enviarMensagem(request), callback);
    }
    public void enviarAudio(
            ChatAudioRequest request,
            RepositoryCallback<ChatResponse> callback
    ) {
        Enqueue.enqueue(api.enviarAudio(request), callback);
    }
}
