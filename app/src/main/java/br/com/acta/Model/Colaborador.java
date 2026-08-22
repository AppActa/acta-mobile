package br.com.acta.Model;

import java.time.LocalDate;
import java.util.List;

import br.com.acta.Model.Contatos.Email;
import br.com.acta.Model.Contatos.Telefone;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Colaborador {
    private Long id;
    private String nome;
    private List<Email> email;
    private List<Telefone> telefone;
    private String cpf;
    private String cargo;
    private String area;
    private LocalDate dataNascimento;
    private LocalDate dataContratacao;
    private Boolean permissaoGestor;


}
