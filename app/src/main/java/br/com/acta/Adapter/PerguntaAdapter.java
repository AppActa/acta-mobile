package br.com.acta.Adapter;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.com.acta.Model.Enum.TipoResposta;
import br.com.acta.Model.Pergunta;
import br.com.acta.Model.RespostasFormulario;
import br.com.acta.R;
import br.com.acta.TelaMapa;

public class PerguntaAdapter extends RecyclerView.Adapter<PerguntaAdapter.PerguntaViewHolder> {
    private final List<Pergunta> perguntas;
    private final RespostasFormulario respostas;
    private boolean mostrarErros;
    private boolean habilitado = true;

    public PerguntaAdapter(List<Pergunta> perguntas, RespostasFormulario respostas) {
        this.perguntas = perguntas;
        this.respostas = respostas;
    }

    @Override
    public int getItemViewType(int position) {
        TipoResposta tipo = perguntas.get(position).getTipo();
        if (tipo == null) {
            return R.layout.item_pergunta_texto;
        }
        switch (tipo) {
            case RADIO: return R.layout.item_pergunta_radio;
            case CHECKBOX: return R.layout.item_pergunta_checkbox;
            case SIM_NAO: return R.layout.item_pergunta_sim_nao;
            case DATA: return R.layout.item_pergunta_data;
            case CEP: return R.layout.item_pergunta_cep;
            case EMAIL: return R.layout.item_pergunta_email;
            case NUMERO_INTEIRO: return R.layout.item_pergunta_numero_inteiro;
            case NUMERO_DECIMAL: return R.layout.item_pergunta_numero_decimal;
            case TELEFONE: return R.layout.item_pergunta_telefone;
            case CPF: return R.layout.item_pergunta_cpf;
            case ARQUIVO: return R.layout.item_pergunta_arquivo;
            case RANGE: return R.layout.item_pergunta_range;
            default: return R.layout.item_pergunta_texto;
        }
    }

    @NonNull
    @Override
    public PerguntaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PerguntaViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PerguntaViewHolder holder, int position) {
        holder.bind(perguntas.get(position));
    }

    @Override
    public int getItemCount() {
        return perguntas.size();
    }

    public int validar() {
        mostrarErros = true;
        int primeiroErro = -1;
        for (int i = 0; i < perguntas.size(); i++) {
            if (respostas.validar(perguntas.get(i)) != null && primeiroErro == -1) {
                primeiroErro = i;
            }
        }
        notifyItemRangeChanged(0, getItemCount());
        return primeiroErro;
    }

    public void setHabilitado(boolean habilitado) {
        if (this.habilitado != habilitado) {
            this.habilitado = habilitado;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    class PerguntaViewHolder extends RecyclerView.ViewHolder {
        private TextWatcher observador;

        PerguntaViewHolder(View itemView) {
            super(itemView);
        }

        void bind(Pergunta pergunta) {
            TextView titulo = itemView.findViewById(R.id.txtTituloPergunta);
            titulo.setText(pergunta.getTitulo() + (Boolean.TRUE.equals(pergunta.getObrigatoria()) ? " *" : ""));
            TipoResposta tipo = pergunta.getTipo();
            boolean disponivel = tipo != null && tipo != TipoResposta.ARQUIVO && tipo != TipoResposta.RANGE;
            habilitar(itemView, habilitado && disponivel);
            atualizarErro(pergunta);
            if (!disponivel) {
                TextView erro = itemView.findViewById(R.id.txtErroPergunta);
                erro.setText(R.string.formulario_campo_indisponivel);
                erro.setVisibility(View.VISIBLE);
                return;
            }
            EditText campo = itemView.findViewById(R.id.edtRespostaPergunta);
            if (campo != null) {
                configurarTexto(campo, pergunta);
                if (tipo == TipoResposta.CEP) {
                    itemView.findViewById(R.id.btnSelecionarEndereco).setOnClickListener(view -> {
                        Intent intent = new Intent(view.getContext(), TelaMapa.class);
                        view.getContext().startActivity(intent);
                    });
                }
            } else if (tipo == TipoResposta.CHECKBOX) {
                configurarCheckbox(pergunta);
            } else {
                configurarRadio(pergunta);
            }
        }

        private void configurarTexto(EditText campo, Pergunta pergunta) {
            if (observador != null) {
                campo.removeTextChangedListener(observador);
            }
            Object valor = respostas.obter(pergunta.getId());
            campo.setText(valor == null ? "" : valor.toString());
            campo.setOnClickListener(null);
            campo.setFocusableInTouchMode(true);
            if (pergunta.getTipo() == TipoResposta.TEXTO) {
                campo.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                campo.setHint(R.string.formulario_texto);
                campo.setMinLines(3);
            }
            if (pergunta.getTipo() == TipoResposta.CEP) {
                campo.setInputType(InputType.TYPE_CLASS_NUMBER);
                campo.setHint(R.string.formulario_cep);
                campo.setMinLines(1);
            }
            if (pergunta.getTipo() == TipoResposta.DATA) {
                campo.setFocusable(false);
                campo.setOnClickListener(view -> {
                    LocalDate data = LocalDate.now();
                    Object atual = respostas.obter(pergunta.getId());
                    if (atual != null && respostas.validar(pergunta) == null && !atual.toString().isEmpty()) {
                        data = LocalDate.parse(atual.toString().trim(), DateTimeFormatter.ofPattern("dd/MM/uuuu"));
                    }
                    new DatePickerDialog(view.getContext(), (picker, ano, mes, dia) ->
                            campo.setText(String.format(Locale.ROOT, "%02d/%02d/%04d", dia, mes + 1, ano)),
                            data.getYear(), data.getMonthValue() - 1, data.getDayOfMonth()).show();
                });
            }
            observador = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence texto, int inicio, int quantidade, int depois) {
                }

                @Override
                public void onTextChanged(CharSequence texto, int inicio, int antes, int quantidade) {
                    respostas.atualizar(pergunta.getId(), texto.toString());
                    atualizarErro(pergunta);
                }

                @Override
                public void afterTextChanged(Editable texto) {
                }
            };
            campo.addTextChangedListener(observador);
        }

        private void configurarRadio(Pergunta pergunta) {
            RadioGroup grupo = itemView.findViewById(R.id.grupoOpcoesPergunta);
            grupo.setOnCheckedChangeListener(null);
            grupo.clearCheck();
            Object valor = respostas.obter(pergunta.getId());
            if (pergunta.getTipo() == TipoResposta.SIM_NAO) {
                if (valor instanceof Boolean) {
                    grupo.check((Boolean) valor ? R.id.opcaoSim : R.id.opcaoNao);
                }
            } else {
                grupo.removeAllViews();
                if (pergunta.getOpcoes() != null) {
                    for (String opcao : pergunta.getOpcoes()) {
                        RadioButton botao = new RadioButton(itemView.getContext());
                        botao.setId(View.generateViewId());
                        botao.setText(opcao);
                        botao.setEnabled(habilitado);
                        grupo.addView(botao);
                        if (opcao.equals(valor)) {
                            grupo.check(botao.getId());
                        }
                    }
                }
            }
            grupo.setOnCheckedChangeListener((view, checkedId) -> {
                if (checkedId == View.NO_ID) {
                    return;
                }
                RadioButton selecionado = grupo.findViewById(checkedId);
                Object resposta = pergunta.getTipo() == TipoResposta.SIM_NAO
                        ? checkedId == R.id.opcaoSim : selecionado.getText().toString();
                respostas.atualizar(pergunta.getId(), resposta);
                atualizarErro(pergunta);
            });
        }

        private void configurarCheckbox(Pergunta pergunta) {
            LinearLayout container = itemView.findViewById(R.id.containerOpcoesPergunta);
            container.removeAllViews();
            if (pergunta.getOpcoes() == null) {
                return;
            }
            Object valor = respostas.obter(pergunta.getId());
            List<?> selecionadas = valor instanceof List ? (List<?>) valor : new ArrayList<>();
            for (String opcao : pergunta.getOpcoes()) {
                CheckBox checkbox = new CheckBox(itemView.getContext());
                checkbox.setText(opcao);
                checkbox.setEnabled(habilitado);
                checkbox.setChecked(selecionadas.contains(opcao));
                container.addView(checkbox);
                checkbox.setOnCheckedChangeListener((botao, checked) -> {
                    List<String> escolhas = new ArrayList<>();
                    for (int i = 0; i < container.getChildCount(); i++) {
                        CheckBox item = (CheckBox) container.getChildAt(i);
                        if (item.isChecked()) {
                            escolhas.add(item.getText().toString());
                        }
                    }
                    respostas.atualizar(pergunta.getId(), escolhas);
                    atualizarErro(pergunta);
                });
            }
        }

        private void atualizarErro(Pergunta pergunta) {
            TextView erro = itemView.findViewById(R.id.txtErroPergunta);
            String mensagem = mostrarErros ? respostas.validar(pergunta) : null;
            erro.setText(mensagem);
            erro.setVisibility(mensagem == null ? View.GONE : View.VISIBLE);
        }

        private void habilitar(View view, boolean enabled) {
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                ViewGroup grupo = (ViewGroup) view;
                for (int i = 0; i < grupo.getChildCount(); i++) {
                    habilitar(grupo.getChildAt(i), enabled);
                }
            }
        }
    }
}
