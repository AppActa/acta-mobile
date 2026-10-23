package br.com.acta.Api;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
public interface ChatbotApi {
    @POST("nova_conversa")
    Call<NovaConversaResponse> novaConversa(
            @Body NovaConversaRequest body
    );

    @GET("listar_chats")
    Call<ListarChatsResponse> listarChats(
            @Query("user_id") String userId,
            @Query("empresa_id") String empresaId,
            @Query("limit") Integer limit
    );

    @GET("chats/{session}/mensagens")
    Call<MensagensResponse> listarMensagens(
            @Path("session") String sessionId,
            @Query("user_id") String userId,
            @Query("empresa_id") String empresaId,
            @Query("limit") Integer limit
    );

    @POST("chat")
    Call<ChatResponse> enviarMensagem(
            @Body ChatRequest body
    );

    @POST("chat/audio")
    Call<ChatResponse> enviarAudio(
            @Body ChatAudioRequest body
    );
}