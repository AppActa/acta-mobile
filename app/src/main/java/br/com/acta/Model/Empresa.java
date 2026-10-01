package br.com.acta.Model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.OffsetDateTime;
import java.util.List;

import br.com.acta.Model.Contatos.Email;
import br.com.acta.Model.Contatos.Telefone;
import br.com.acta.Model.Enum.StatusGeral;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(tableName = "tb_empresa")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Empresa {
    @PrimaryKey
    private Long id;

    private String cnpj;

    private String nome;

    private String setor;

    @Ignore
    private List<Telefone> telefones;

    @Ignore
    private List<Email> emails;

    @Ignore
    private StatusGeral status;

    @Ignore
    private OffsetDateTime criadoEm;

    @Ignore
    private OffsetDateTime atualizadoEm;
}
