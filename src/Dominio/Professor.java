package Dominio;

import java.util.Set;

public class Professor {
    private String id;
    private String nome;
    private Set<String> horarioDisponiveis;
    private String preferenciaTurno;

    public Professor(String id, String nome, Set<String> horarioDisponiveis, String preferenciaTurno) {
        this.id = id;
        this.nome = nome;
        this.horarioDisponiveis = horarioDisponiveis;
        this.preferenciaTurno = preferenciaTurno;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Set<String> getHorarioDisponiveis() {
        return horarioDisponiveis;
    }

    public void setHorarioDisponiveis(Set<String> horarioDisponiveis) {
        this.horarioDisponiveis = horarioDisponiveis;
    }

    public String getPreferenciaTurno() {
        return preferenciaTurno;
    }

    public void setPreferenciaTurno(String preferenciaTurno) {
        this.preferenciaTurno = preferenciaTurno;
    }
}
