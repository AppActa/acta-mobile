package br.com.acta.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.acta.Model.Enum.TipoResposta;

public class RespostasFormulario {
    private final Map<String, Object> valores = new HashMap<>();

    public Object obter(String idPergunta) {
        return valores.get(idPergunta);
    }

    public void atualizar(String idPergunta, Object valor) {
        valores.put(idPergunta, valor);
    }

    public String validar(Pergunta pergunta) {
        TipoResposta tipo = pergunta.getTipo();
        boolean obrigatoria = Boolean.TRUE.equals(pergunta.getObrigatoria());
        if (tipo == null || tipo == TipoResposta.ARQUIVO || tipo == TipoResposta.RANGE) {
            return obrigatoria ? "Este campo ainda não está disponível. Não é possível enviar este formulário." : null;
        }
        Object valor = obter(pergunta.getId());
        if (vazio(valor)) {
            return obrigatoria ? "Preencha este campo." : null;
        }
        String texto = valor.toString().trim();
        switch (tipo) {
            case NUMERO_INTEIRO:
            case NUMERO_DECIMAL:
                try {
                    BigDecimal numero = new BigDecimal(texto.replace(',', '.'));
                    if (tipo == TipoResposta.NUMERO_INTEIRO) {
                        numero.longValueExact();
                    }
                } catch (NumberFormatException | ArithmeticException exception) {
                    return "Digite um número válido.";
                }
                break;
            case DATA:
                try {
                    LocalDate.parse(texto, DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));
                } catch (DateTimeParseException exception) {
                    return "Informe uma data válida no formato DD/MM/AAAA.";
                }
                break;
            case EMAIL:
                if (!texto.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
                    return "Informe um e-mail válido.";
                }
                break;
            case CEP:
                if (!texto.matches("\\d{5}-?\\d{3}")) {
                    return "Informe um CEP com 8 dígitos.";
                }
                break;
            case TELEFONE:
                if (!texto.replaceAll("[() +.-]", "").matches("\\d{10,13}")) {
                    return "Informe um telefone com DDD.";
                }
                break;
            case CPF:
                if (!cpfValido(texto)) {
                    return "Informe um CPF válido.";
                }
                break;
            case RADIO:
                if (pergunta.getOpcoes() == null || !pergunta.getOpcoes().contains(texto)) {
                    return "Selecione uma opção válida.";
                }
                break;
            case CHECKBOX:
                if (!(valor instanceof List) || pergunta.getOpcoes() == null || !pergunta.getOpcoes().containsAll((List<?>) valor)) {
                    return "Selecione opções válidas.";
                }
                break;
            default:
                break;
        }
        return null;
    }

    public List<RespostaPergunta> montar(List<Pergunta> perguntas) {
        List<RespostaPergunta> respostas = new ArrayList<>();
        for (Pergunta pergunta : perguntas) {
            Object valor = obter(pergunta.getId());
            if (vazio(valor)) {
                continue;
            }
            switch (pergunta.getTipo()) {
                case NUMERO_INTEIRO:
                    valor = new BigDecimal(valor.toString().trim().replace(',', '.')).longValueExact();
                    break;
                case NUMERO_DECIMAL:
                    valor = new BigDecimal(valor.toString().trim().replace(',', '.'));
                    break;
                case DATA:
                    valor = LocalDate.parse(valor.toString().trim(), DateTimeFormatter.ofPattern("dd/MM/uuuu")).toString();
                    break;
                default:
                    break;
            }
            respostas.add(new RespostaPergunta(pergunta.getId(), valor));
        }
        return respostas;
    }

    private boolean vazio(Object valor) {
        return valor == null || valor instanceof String && ((String) valor).trim().isEmpty()
                || valor instanceof List && ((List<?>) valor).isEmpty();
    }

    private boolean cpfValido(String texto) {
        String cpf = texto.replace(".", "").replace("-", "");
        if (!cpf.matches("\\d{11}") || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        for (int digito = 9; digito < 11; digito++) {
            int soma = 0;
            for (int i = 0; i < digito; i++) {
                soma += (cpf.charAt(i) - '0') * (digito + 1 - i);
            }
            int verificador = (soma * 10 % 11) % 10;
            if (cpf.charAt(digito) - '0' != verificador) {
                return false;
            }
        }
        return true;
    }
}
