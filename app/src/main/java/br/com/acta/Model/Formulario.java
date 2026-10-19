package br.com.acta.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import br.com.acta.Model.Enum.StatusFormulario;
import br.com.acta.Model.Enum.TipoFormulario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Formulario {

    @SerializedName("id")
    private String id;

    @SerializedName("idEmpresa")
    private Long idEmpresa;

    @SerializedName("idCiclo")
    private Long idCiclo;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("descricao")
    private String descricao;

    @SerializedName("tipo")
    private TipoFormulario tipo;

    @SerializedName("status")
    private StatusFormulario status;

    @SerializedName("perguntas")
    private List<Pergunta> perguntas;

    @SerializedName("idIshikawa")
    private String idIshikawa;

    @SerializedName("idsUsuariosDestinatarios")
    private List<Long> idsUsuariosDestinatarios;

    @SerializedName("publicadoEm")
    private String publicadoEm;

    @SerializedName("criadoEm")
    private String criadoEm;

    @SerializedName("atualizadoEm")
    private String atualizadoEm;
}
