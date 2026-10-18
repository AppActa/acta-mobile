package br.com.acta.utils;

import android.content.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

public final class FormatoData {
    private static final String CHAVE = "formato_data";
    private static final Locale LOCALE = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/uuuu", LOCALE).withResolverStyle(ResolverStyle.STRICT);

    public enum Formato {
        DIA_MES_ANO("dd/MM/yyyy"),
        ANO_MES_DIA("yyyy-MM-dd"),
        MES_DIA_ANO("MM/dd/yyyy");

        private final String padrao;

        Formato(String padrao) {
            this.padrao = padrao;
        }

        public String getPadrao() {
            return padrao + " HH:mm:ss";
        }
    }

    private FormatoData() {
    }

    public static Formato obterFormato(Context contexto) {
        String salvo = contexto.getSharedPreferences("config_app", Context.MODE_PRIVATE).getString(CHAVE, Formato.DIA_MES_ANO.name());
        try {
            return Formato.valueOf(salvo);
        } catch (IllegalArgumentException | NullPointerException erro) {
            return Formato.DIA_MES_ANO;
        }
    }

    public static void salvarFormato(Context contexto, Formato formato) {
        contexto.getSharedPreferences("config_app", Context.MODE_PRIVATE).edit().putString(CHAVE, formato.name()).apply();
    }

    public static String formatar(Context contexto, String valor) {
        return formatar(valor, obterFormato(contexto), ZoneId.systemDefault());
    }

    static String formatar(String valor, Formato formato, ZoneId fuso) {
        if (valor == null || valor.trim().isEmpty()) {
            return "—";
        }

        String data = valor.trim();
        DateTimeFormatter saida = DateTimeFormatter.ofPattern(formato.getPadrao(), LOCALE);
        try {
            TemporalAccessor instante = DateTimeFormatter.ISO_DATE_TIME.parseBest(data, ZonedDateTime::from, LocalDateTime::from);
            if (instante instanceof ZonedDateTime) {
                return saida.format(((ZonedDateTime) instante).withZoneSameInstant(fuso));
            }
            return saida.format(instante);
        } catch (DateTimeParseException erro) {
            // os serviços também retornam datas sem horário, em ISO ou no formato brasileiro
        }

        DateTimeFormatter saidaData = DateTimeFormatter.ofPattern(formato.padrao, LOCALE);
        try {
            return LocalDate.parse(data, DateTimeFormatter.ISO_LOCAL_DATE).format(saidaData);
        } catch (DateTimeParseException erro) {
            try {
                return LocalDate.parse(data, DATA_BR).format(saidaData);
            } catch (DateTimeParseException dataInvalida) {
                return "—";
            }
        }
    }
}