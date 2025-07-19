package Dominio;

import java.util.Set;

public class Sala {
    private String id;
    private String nome;
    private int capacidade;
    private Set<String> horarioDisponiveisId;

    public Sala(String id, String nome, int capacidade, Set<String> horarioDisponiveisId) {
        this.id = id;
        this.nome = nome;
        this.capacidade = capacidade;
        this.horarioDisponiveisId = horarioDisponiveisId;
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

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public Set<String> getHorarioDisponiveisId() {
        return horarioDisponiveisId;
    }

    public void setHorarioDisponiveisId(Set<String> horarioDisponiveisId) {
        this.horarioDisponiveisId = horarioDisponiveisId;
    }
}
