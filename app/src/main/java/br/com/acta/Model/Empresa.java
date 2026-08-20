package br.com.acta.Model;

import java.time.OffsetDateTime;
import java.util.List;

import br.com.acta.Model.Contatos.Email;
import br.com.acta.Model.Contatos.Telefone;
import br.com.acta.Model.Enum.StatusGeral;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Empresa {
    private Long id;

    private String cnpj;

    private String nome;

    private String setor;

    private List<Telefone> telefones;

    private List<Email> emails;


    private StatusGeral status;

    private OffsetDateTime criadoEm;

    private OffsetDateTime atualizadoEm;
}
