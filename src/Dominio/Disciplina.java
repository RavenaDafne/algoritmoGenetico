package Dominio;

import java.util.List;

public class Disciplina {
    private String id;
    private String nome;
    private int numeroAlunosEstimado;
    private List<String>  professoresPreferidosIds;

    public Disciplina(String id, String nome, int numeroAlunosEstimado, List<String> professoresPreferidosIds) {
        this.id = id;
        this.nome = nome;
        this.numeroAlunosEstimado = numeroAlunosEstimado;
        this.professoresPreferidosIds = professoresPreferidosIds;
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

    public int getNumeroAlunosEstimado() {
        return numeroAlunosEstimado;
    }

    public void setNumeroAlunosEstimado(int numeroAlunosEstimado) {
        this.numeroAlunosEstimado = numeroAlunosEstimado;
    }

    public List<String> getProfessoresPreferidosIds() {
        return professoresPreferidosIds;
    }

    public void setProfessoresPreferidosIds(List<String> professoresPreferidosIds) {
        this.professoresPreferidosIds = professoresPreferidosIds;
    }
}
