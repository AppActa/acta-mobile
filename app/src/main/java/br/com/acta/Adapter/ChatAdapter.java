package br.com.acta.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.acta.ChatMessage;
import br.com.acta.R;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private final List<ChatMessage> mensagens;
    private final SugestaoChatAdapter.OnSugestaoClickListener listener;

    public ChatAdapter(List<ChatMessage> mensagens, SugestaoChatAdapter.OnSugestaoClickListener listener) {
        this.mensagens = mensagens;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return mensagens.get(position).getType().ordinal();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ChatMessage.Type tipo = ChatMessage.Type.values()[viewType];
        int layout;
        switch (tipo) {
            case WELCOME:
                layout = R.layout.layout_cato_welcome;
                break;
            case SUGGESTIONS:
                layout = R.layout.layout_chat_suggestions;
                break;
            case USER:
                layout = R.layout.item_user_message;
                break;
            case LOADING:
                layout = R.layout.item_loading;
                break;
            case CATO:
            default:
                layout = R.layout.item_cato_message;
                break;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage mensagem = mensagens.get(position);
        if (mensagem.getType() == ChatMessage.Type.USER) {
            holder.userMessage.setText(mensagem.getText());
        } else if (mensagem.getType() == ChatMessage.Type.CATO) {
            holder.catoMessage.setText(mensagem.getText());
        } else if (mensagem.getType() == ChatMessage.Type.SUGGESTIONS) {
            RecyclerView sugestoes = holder.sugestoes;
            if (sugestoes.getLayoutManager() == null) {
                sugestoes.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 2) {
                    @Override
                    public boolean canScrollVertically() {
                        return false;
                    }
                });
            }
            sugestoes.setNestedScrollingEnabled(false);
            if (sugestoes.getAdapter() == null) {
                sugestoes.setAdapter(new SugestaoChatAdapter(listener));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        final TextView userMessage;
        final TextView catoMessage;
        final RecyclerView sugestoes;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessage = itemView.findViewById(R.id.userMessageText);
            catoMessage = itemView.findViewById(R.id.catoMessageText);
            sugestoes = itemView.findViewById(R.id.chatSuggestions);
        }
    }
}
