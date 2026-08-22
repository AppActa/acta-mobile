package br.com.acta.Model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import br.com.acta.Model.Enum.StatusCiclo;

public class Ciclo {
    private Long id;

    private String titulo;

    private String descricao;

    private StatusCiclo status;

    private LocalDate dataInicio;

    private LocalDate dataEstimadaFim;
    private LocalDate dataFimReal;

    private Long idEmpresa;

    private Long idGestor;

    private List<UsuarioCiclo> colaboradores;

    private OffsetDateTime criadoEm;
    private OffsetDateTime atualizadoEm;

}
