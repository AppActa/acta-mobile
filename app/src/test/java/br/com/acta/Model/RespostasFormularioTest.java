package br.com.acta.Model;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import br.com.acta.Model.Enum.TipoResposta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RespostasFormularioTest {
    private final RespostasFormulario respostas = new RespostasFormulario();

    @Test
    public void naoContaComoRespostaValidaParaSimNaoObrigatorio() {
        Pergunta pergunta = pergunta(TipoResposta.SIM_NAO, true);
        assertNotNull(respostas.validar(pergunta));
        respostas.atualizar("pergunta", false);
        assertNull(respostas.validar(pergunta));
        assertEquals(false, respostas.montar(Collections.singletonList(pergunta)).get(0).getResposta());
    }

    @Test
    public void validaDataRealEConverteParaFormatoIso() {
        Pergunta pergunta = pergunta(TipoResposta.DATA, true);
        respostas.atualizar("pergunta", "29/02/2025");
        assertNotNull(respostas.validar(pergunta));
        respostas.atualizar("pergunta", "29/02/2024");
        assertNull(respostas.validar(pergunta));
        assertEquals("2024-02-29", respostas.montar(Collections.singletonList(pergunta)).get(0).getResposta());
    }

    @Test
    public void converteDecimalComVirgulaSemPerderPrecisao() {
        Pergunta pergunta = pergunta(TipoResposta.NUMERO_DECIMAL, true);
        respostas.atualizar("pergunta", "12,75");
        assertNull(respostas.validar(pergunta));
        assertEquals(new BigDecimal("12.75"), respostas.montar(Collections.singletonList(pergunta)).get(0).getResposta());
    }

    @Test
    public void rejeitaInteiroForaDoLimiteOuComFracao() {
        Pergunta pergunta = pergunta(TipoResposta.NUMERO_INTEIRO, true);
        respostas.atualizar("pergunta", "9223372036854775808");
        assertNotNull(respostas.validar(pergunta));
        respostas.atualizar("pergunta", "1.5");
        assertNotNull(respostas.validar(pergunta));
    }

    @Test
    public void validaOpcoesEListaObrigatoriaVazia() {
        Pergunta pergunta = pergunta(TipoResposta.CHECKBOX, true);
        pergunta.setOpcoes(Arrays.asList("A", "B"));
        respostas.atualizar("pergunta", Collections.emptyList());
        assertNotNull(respostas.validar(pergunta));
        respostas.atualizar("pergunta", Arrays.asList("A", "C"));
        assertNotNull(respostas.validar(pergunta));
        respostas.atualizar("pergunta", Arrays.asList("A", "B"));
        assertNull(respostas.validar(pergunta));
    }

    @Test
    public void campoIndisponivelObrigatorioBloqueiaEnvioEOpcionalPodeSerOmitido() {
        Pergunta pergunta = pergunta(TipoResposta.ARQUIVO, true);
        assertNotNull(respostas.validar(pergunta));
        pergunta.setObrigatoria(false);
        assertNull(respostas.validar(pergunta));
        assertEquals(0, respostas.montar(Collections.singletonList(pergunta)).size());
    }

    @Test
    public void validaDigitosDoCpf() {
        Pergunta pergunta = pergunta(TipoResposta.CPF, true);
        respostas.atualizar("pergunta", "111.111.111-11");
        assertNotNull(respostas.validar(pergunta));
        respostas.atualizar("pergunta", "529.982.247-25");
        assertNull(respostas.validar(pergunta));
    }

    private Pergunta pergunta(TipoResposta tipo, boolean obrigatoria) {
        Pergunta pergunta = new Pergunta();
        pergunta.setId("pergunta");
        pergunta.setTipo(tipo);
        pergunta.setObrigatoria(obrigatoria);
        return pergunta;
    }
}
