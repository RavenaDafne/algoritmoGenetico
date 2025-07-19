package Dominio;

import java.util.List;

public class Aluno {
    private String id;
    private String nome;
    private List<String> disciplinaMatriculadasIds;

    public Aluno(String id, String nome, List<String> disciplinaMatriculadasIds) {
        this.id = id;
        this.nome = nome;
        this.disciplinaMatriculadasIds = disciplinaMatriculadasIds;
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

    public List<String> getDisciplinaMatriculadasIds() {
        return disciplinaMatriculadasIds;
    }

    public void setDisciplinaMatriculadasIds(List<String> disciplinaMatriculadasIds) {
        this.disciplinaMatriculadasIds = disciplinaMatriculadasIds;
    }
}
