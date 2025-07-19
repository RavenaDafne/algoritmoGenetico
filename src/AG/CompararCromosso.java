package AG;

import java.util.Comparator;

class CompararCromosso implements Comparator<Cromossomo> {
        boolean crescente = true;

        public CompararCromosso(boolean crscente) {
            this.crescente = crscente;
        }
        @Override
    public int compare(Cromossomo c1, Cromossomo c2){
            if(crescente){
                return Double.compare(c1.getFitness(),c2.getFitness());
            }else{
                return Double.compare(c2.getFitness(), c1.getFitness());
            }
    }
}
