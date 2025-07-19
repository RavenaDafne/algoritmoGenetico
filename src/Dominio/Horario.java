package Dominio;

import java.time.LocalTime;

public class Horario {
    private String id;
    private String diaSemana;
    private String horaInicio;
    private String horaFim;
    private String turno;

    public Horario(String id, String diaSemana, String horaInicio, String horaFim, String turno) {
        this.id = id;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.turno = turno;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(String horaFim) {
        this.horaFim = horaFim;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public boolean sobrepoe(Horario outroHorario) {
        if (!this.diaSemana.equals(outroHorario.getDiaSemana())) {
            return false; // Dias diferentes, não há sobreposição
        }
        LocalTime inicioEste = LocalTime.parse(this.horaInicio);
        LocalTime fimEste = LocalTime.parse(this.horaFim);
        LocalTime inicioOutro = LocalTime.parse(outroHorario.getHoraInicio());
        LocalTime fimOutro = LocalTime.parse(outroHorario.getHoraFim());
        return inicioEste.isBefore(fimOutro) && inicioOutro.isBefore(fimEste);
    }
}