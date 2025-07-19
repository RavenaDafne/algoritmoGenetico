package Dominio;

import java.util.Objects;

public class Aula {
public String disciplinaId;
public  String horarioId;
public String salaId;
public String professorId;

    public Aula(String disciplinaId, String horarioId, String salaId, String professorId) {
        this.disciplinaId = disciplinaId;
        this.horarioId = horarioId;
        this.salaId = salaId;
        this.professorId = professorId;
    }
    @Override
    public  boolean equals(Object o) {
        if(this == o )return true;
        if(o == null || getClass() != o.getClass()) return false;
        Aula that = (Aula) o;
        return Objects.equals(disciplinaId,that.disciplinaId) &&
                Objects.equals(horarioId,that.horarioId) &&
                Objects.equals(salaId,that.salaId) &&
                Objects.equals(professorId,that.professorId);
        }

    public String getDisciplinaId() {
        return disciplinaId;
    }

    public void setDisciplinaId(String disciplinaId) {
        this.disciplinaId = disciplinaId;
    }

    public String getHorarioId() {
        return horarioId;
    }

    public void setHorarioId(String horarioId) {
        this.horarioId = horarioId;
    }

    public String getSalaId() {
        return salaId;
    }

    public void setSalaId(String salaId) {
        this.salaId = salaId;
    }

    public String getProfessorId() {
        return professorId;
    }

    public void setProfessorId(String professorId) {
        this.professorId = professorId;
    }

    @Override
public int hashCode(){
    return Objects.hash(disciplinaId,horarioId,salaId,professorId);
    }
@Override
public String toString(){
    return"(" + disciplinaId + "," + horarioId + "," + salaId + "," + professorId + ")";
    }
}