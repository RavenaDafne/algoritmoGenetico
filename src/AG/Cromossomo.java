package AG;

import Dominio.Aula;

import java.util.ArrayList;

public class Cromossomo {
    private ArrayList<Aula> sequencia;
    private double fitness;
    private int posicao;
    private int percentual;
    private boolean fitnessCalculado;

    public Cromossomo() {
        this.sequencia = new ArrayList<>();
        this.fitness = 0;
        this.posicao = 0;
        this.percentual = 0;
        this.fitnessCalculado = false;
    }

    public ArrayList<Aula> getSequencia() {
        return sequencia;
    }

    public void setSequencia(ArrayList<Aula> sequencia) {
        this.sequencia = sequencia;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
        this.fitnessCalculado = true;
    }

    public int getPosicao() {
        return posicao;
    }

    public void setPosicao(int posicao) {
        this.posicao = posicao;
    }

    public int getPercentual() {
        return percentual;
    }

    public void setPercentual(int percentual) {
        this.percentual = percentual;
    }

    public boolean isFitnessCalculado() {
        return fitnessCalculado;
    }

    public void setFitnessCalculado(boolean fitnessCalculado) {
        this.fitnessCalculado = fitnessCalculado;
    }
}
