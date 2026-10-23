package br.com.acta.Api;

import br.com.acta.Model.ChatAudioRequest;
import br.com.acta.Model.ChatRequest;
import br.com.acta.Model.ChatResponse;
import br.com.acta.Model.ListarChatsResponse;
import br.com.acta.Model.MensagensResponse;
import br.com.acta.Model.NovaConversaRequest;
import br.com.acta.Model.NovaConversaResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatbotApi {

    @POST("nova_conversa")
    Call<NovaConversaResponse> novaConversa(@Body NovaConversaRequest body);

    @GET("listar_chats")
    Call<ListarChatsResponse> listarChats(
            @Query("usuario_id") Integer usuarioId,
            @Query("empresa_id") Integer empresaId,
            @Query("limit") Integer limit
    );

    @GET("chats/{session_id}/mensagens")
    Call<MensagensResponse> listarMensagens(
            @Path("session_id") String sessionId,
            @Query("usuario_id") Integer usuarioId,
            @Query("empresa_id") Integer empresaId,
            @Query("limit") Integer limit
    );

    @POST("chat")
    Call<ChatResponse> enviarMensagem(@Body ChatRequest body);

    @POST("chat/audio")
    Call<ChatResponse> enviarAudio(@Body ChatAudioRequest body);
}
