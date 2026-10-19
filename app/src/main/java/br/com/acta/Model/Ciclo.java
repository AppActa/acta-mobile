package br.com.acta.Model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

import br.com.acta.Model.Enum.StatusCiclo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(tableName = "tb_ciclo")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Ciclo {
    @PrimaryKey
    private Long id;

    private String titulo;

    private String descricao;

    private StatusCiclo status;

    private String dataInicio;

    private String dataEstimadaFim;

    private String dataFimReal;

    private Long idEmpresa;

    private Long idGestor;

    @Ignore
    private List<UsuarioCiclo> colaboradores;

    private String criadoEm;

    private String atualizadoEm;

    private String iconeUrl;
}
