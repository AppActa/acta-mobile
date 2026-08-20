package br.com.acta.Model.Contatos;
import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Telefone {
    private Long id;
    private String telefone;
    private Boolean principal;
    private OffsetDateTime criadoEm;
}
