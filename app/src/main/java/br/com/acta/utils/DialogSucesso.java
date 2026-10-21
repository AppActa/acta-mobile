package br.com.acta.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import br.com.acta.R;

public class DialogSucesso {

    public interface OnConcluidoListener {
        void onConcluido();
    }

    public static void exibir(@NonNull Context context, String mensagem, OnConcluidoListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_sucesso_formulario, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView txtMensagem = view.findViewById(R.id.txtMensagemDialogSucesso);
        if (txtMensagem != null && mensagem != null && !mensagem.trim().isEmpty()) {
            txtMensagem.setText(mensagem);
        }

        View btnConcluido = view.findViewById(R.id.btnConcluidoDialogSucesso);
        if (btnConcluido != null) {
            btnConcluido.setOnClickListener(v -> {
                dialog.dismiss();
                if (listener != null) {
                    listener.onConcluido();
                }
            });
        }

        dialog.setCancelable(false);
        dialog.show();
    }
}
