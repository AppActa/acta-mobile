package br.com.acta.Model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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

    @Ignore
    private StatusCiclo status;

    @Ignore
    private LocalDate dataInicio;

    @Ignore
    private LocalDate dataEstimadaFim;

    @Ignore
    private LocalDate dataFimReal;

    private Long idEmpresa;

    private Long idGestor;

    @Ignore
    private List<UsuarioCiclo> colaboradores;

    @Ignore
    private OffsetDateTime criadoEm;

    @Ignore
    private OffsetDateTime atualizadoEm;

    private String iconeUrl;
}
