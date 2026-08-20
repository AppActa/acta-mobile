package br.com.acta.Model.Contatos;


import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Email {

    private Long id;
    private String email;
    private Boolean principal;

    private OffsetDateTime criadoEm;
}
