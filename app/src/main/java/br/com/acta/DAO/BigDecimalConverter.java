package br.com.acta.DAO;

import androidx.room.TypeConverter;

import java.math.BigDecimal;

public class BigDecimalConverter {
    @TypeConverter
    public static String paraTexto(BigDecimal valor) {
        return valor == null ? null : valor.toPlainString();
    }

    @TypeConverter
    public static BigDecimal paraBigDecimal(String valor) {
        return valor == null ? null : new BigDecimal(valor);
    }
}
