package AG;

import Dominio.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
public class AlgoritmoGenetico {

    private static AlgoritmoGenetico instancia = null;

    // Parâmetros do Algoritmo Genético
    public static int numeroGenes = 100; // Número de disciplinas a serem agendadas
    public static int numeroCromossomos = 40; // Tamanho inicial da população
    public static int numeroCromossomosAptos = 10; // Cromossomos mais aptos para reprodução (deve ser par e > 2)
    public static int numeroEvolucoes = 1000; // Número de gerações
    public static float taxaMortalidade = (float)0.1; // Taxa de mortalidade
    public static float taxaMutabilidade = (float)0.3; // Taxa de mutação

    public static int numeroMortos = 0; // Dados para gráfico de análise
    public static int numeroMutantes = 0; // Dados para gráfico de análise

    public static ArrayList<Cromossomo> populacao = null;

    // Dados da Universidade (Simulados para exemplo)
    public static List<Disciplina> todasDisciplinas;
    public static List<Professor> todosProfessores;
    public static List<Sala> todasSalas;
    public static List<Horario> todosHorarios;
    public static Map<String, List<String>> matriculasAlunos; // Lista de Disciplinas ID

    // Mapeamentos para busca rápida
    public static Map<String, Disciplina> mapaDisciplinas;
    public static Map<String, Professor> mapaProfessores;
    public static Map<String, Sala> mapaSalas;
    public static Map<String, Horario> mapaHorarios;


    public static void main(String[] args) throws IOException {
        // Inicializar dados da universidade 
        carregarDadosUniversidadeExemplo();

        FileWriter writer = new FileWriter(numeroCromossomos + "_"+ numeroEvolucoes + "_saida_" +".txt",true);

        PrintWriter saida = new PrintWriter(writer);

        AlgoritmoGenetico ag = AlgoritmoGenetico.getInstancia();

        // Criando lista de cromossomos
        ArrayList<Cromossomo> pop = ag.getPopulacao();

        // Criação da população (randomica)
        ag.gerarPopulacao();

        // Calcular Fitness dos cromossomos
        ag.definirFitnessScore();

        // Selecao cromossomos - Decrescente por valor de Fitness
        ag.selecao();

        // Início dos cruzamentos
        for (int i = 0; i < numeroEvolucoes; i++) {

            // Cruzamento
            ag.cruzamentos();

            // Calcular Fitness  - recalcula para novos e mutados
            ag.definirFitnessScore();

            // Selecao cromossomos
            ag.selecao();

            System.out.print("\n==================================================");

            System.out.print("\nPopulação inicial: " + numeroCromossomos);
            System.out.print("\nPopulação atual: "+ populacao.size());
            System.out.print("\nNúmero de gerações: " + (i + 1));
            System.out.print("\nGrau médio do fitness: " + ag.obterFitnessMedio());
            System.out.print("\nQuantidade de mortos: " + numeroMortos);
            System.out.print("\nQuantidade de mutação: " + numeroMutantes);

            saida.print((i + 1) +"  " +ag.obterFitnessMedio() );
            saida.print("\n");

            // Salvando resultados no arquivo RES
            ag.salvarPopulacaoApta(numeroCromossomos + "_"+ numeroEvolucoes + "_saida_" +".res", (i + 1));
            ag.imprimirPopulacaoApta();
        }

        saida.close();
        writer.close();

    }

    public static AlgoritmoGenetico getInstancia() {
        if (instancia == null) {
            instancia = new AlgoritmoGenetico();
        }
        return (instancia);
    }

    public ArrayList<Cromossomo> getPopulacao() {
        if (populacao == null) {
            populacao = new ArrayList<Cromossomo>();
        }
        return (populacao);
    }

    public void gerarPopulacao() {

        Cromossomo tmpCromossomo;

        for (int i = 0; i < numeroCromossomos; i++) {

            tmpCromossomo = new Cromossomo();

            ArrayList<Aula> sequenciaTemporaria = iniciarCromossomo();

            tmpCromossomo.setSequencia(sequenciaTemporaria);

            populacao.add(tmpCromossomo);
        }
    }

    public ArrayList<Aula> iniciarCromossomo() {

        ArrayList<Aula> sequencia = new ArrayList<>();
        Random random = new Random();

        // Atribuindo um horário, sala e professor aleatórios
        List<Disciplina> disciplinasNaoAtribuidas = new ArrayList<>(todasDisciplinas);

        for (int i = 0; i < numeroGenes; i++) {
            if (disciplinasNaoAtribuidas.isEmpty()) break; 

            Disciplina disciplina = disciplinasNaoAtribuidas.remove(random.nextInt(disciplinasNaoAtribuidas.size()));

            Horario horario = todosHorarios.get(random.nextInt(todosHorarios.size()));
            Sala sala = todasSalas.get(random.nextInt(todasSalas.size()));
            Professor professor = todosProfessores.get(random.nextInt(todosProfessores.size()));

            Aula atribuicao = new Aula(disciplina.getId(), horario.getId(), sala.getId(), professor.getId());
            sequencia.add(atribuicao);
        }

        return sequencia;
    }

    public void definirFitnessScore(){

        for (Cromossomo tempCromossomo : populacao) {
            // Recalculando o  fitness:  caso não foi calculado ou se foi alterado por mutação/cruzamento
            if (!tempCromossomo.isFitnessCalculado()) {
                double fitnessAtual = 100000.0; // Pontuação base alta

                // Verificando os conflitos
                // Checando se a sala estiver ocupada
                Map<String, List<Aula>> horarioSalaMap = new HashMap<>();
                // Checando se o professor estiver ocupado
                Map<String, List<Aula>> horarioProfessorMap = new HashMap<>();
                // Checando se aluno estiver ocupado
                Map<String, List<Horario>> alunoHorariosMap = new HashMap<>();

                // Povoando e aplicando restrições rígidas
                for (Aula aula : tempCromossomo.getSequencia()) {
                    // Os IDs existem nos dados da universidade
                    if (!mapaDisciplinas.containsKey(aula.getDisciplinaId()) ||
                            !mapaHorarios.containsKey(aula.getHorarioId()) ||
                            !mapaSalas.containsKey(aula.getSalaId()) ||
                            !mapaProfessores.containsKey(aula.getProfessorId())) {
                        fitnessAtual -= 2000; // Penalidade muito alta 
                        continue; 
                    }

                    Disciplina disciplina = mapaDisciplinas.get(aula.getDisciplinaId());
                    Horario horario = mapaHorarios.get(aula.getHorarioId());
                    Sala sala = mapaSalas.get(aula.getSalaId());
                    Professor professor = mapaProfessores.get(aula.getProfessorId());


                    // Penalidades Altas

                    // 1. Conflito a  sala ocupada
                    String chaveSalaHorario = aula.salaId + "_" + aula.getHorarioId();
                    horarioSalaMap.computeIfAbsent(chaveSalaHorario, k -> new ArrayList<>()).add(aula);
                    if (horarioSalaMap.get(chaveSalaHorario).size() > 1) {
                        fitnessAtual -= 1000; // Penalidade
                    }

                    // 2. Conflito professor ocupado
                    String chaveProfessorHorario = aula.getProfessorId() + "_" + aula.getHorarioId();
                    horarioProfessorMap.computeIfAbsent(chaveProfessorHorario, k -> new ArrayList<>()).add(aula);
                    if (horarioProfessorMap.get(chaveProfessorHorario).size() > 1) {
                        fitnessAtual -= 1000; // Penalidade
                    }

                    // 3. Disponibilidade do professor
                    if (!professor.getHorarioDisponiveis().contains(aula.getHorarioId())) {
                        fitnessAtual -= 1000; //Penalidade
                    }

                    // 4. Capacidade pequena da Sala
                    if (disciplina.getNumeroAlunosEstimado() > sala.getCapacidade()) {
                        fitnessAtual -= 1000; // Penalidade
                    }

                    // 5. Preferência de professor 
                    if (disciplina.getProfessoresPreferidosIds() != null &&
                            !disciplina.getProfessoresPreferidosIds().isEmpty() &&
                            !disciplina.getProfessoresPreferidosIds().contains(aula.getProfessorId())) {
                        fitnessAtual -= 200; // Penalidade 
                    }

                    // Penalidades Menores

                    // 6. Preferência do Turno do Professor
                    if (professor.getPreferenciaTurno() != null && !professor.getPreferenciaTurno().isEmpty() &&
                            !professor.getPreferenciaTurno().equals(horario.getTurno())) {
                        fitnessAtual -= 50; // Penalidade 
                    }

                    // 7. Conflitos de Horário para o aluno matriculado 
                    for (Map.Entry<String, List<String>> entry : matriculasAlunos.entrySet()) {
                        String alunoId = entry.getKey();
                        List<String> disciplinasDoAluno = entry.getValue();

                        if (disciplinasDoAluno.contains(aula.getDisciplinaId())) {
                            // Adiciona o horário ao mapa de horários do aluno
                            alunoHorariosMap.computeIfAbsent(alunoId, k -> new ArrayList<>()).add(horario);
                        }
                    }
                }
                for (List<Horario> horariosAluno : alunoHorariosMap.values()) {
                    for (int i = 0; i < horariosAluno.size(); i++) {
                        for (int j = i + 1; j < horariosAluno.size(); j++) {
                            if (horariosAluno.get(i).sobrepoe(horariosAluno.get(j))) {
                                fitnessAtual -= 500; // Penalidade 
                            }
                        }
                    }
                }

                // fitness não será negativo
                if (fitnessAtual < 0) {
                    fitnessAtual = 0;
                }
                tempCromossomo.setFitness(fitnessAtual);
            }
        }
    }

    public void selecao() {

        Collections.sort (populacao, new CompararCromosso(false)); // Será falso  pois queremos um fitness decrescente

        int percentual = 100;
        for (Cromossomo tempCromossomo : populacao) {
            tempCromossomo.setPosicao(populacao.indexOf(tempCromossomo));

            percentual = percentual - 10; //Decaimento
            if (percentual < 10){
                percentual = 5; // Mínimo
            }

            tempCromossomo.setPercentual(percentual);
        }

    }

    public void cruzamentos() {

        int paiIndex, maeIndex;

        // Clonando a população atual para evitar alterações em tempo de cruzamento(Exceções)
        ArrayList<Cromossomo> novaPopulacao = new ArrayList<>();
        // Cromossomos mais aptos adicionados a  nova população para o elitismo
        for(int i = 0; i < numeroCromossomosAptos; i++) {
            novaPopulacao.add(populacao.get(i));
        }


        for (int i = 0; i < (numeroCromossomos - numeroCromossomosAptos) / 2; i++) { // Gerar novos filhos para preencher o resto da população

            // Pegando 2 pais aleatoriamente : seleção por roleta
            paiIndex = obterPai();
            maeIndex = obterMae(paiIndex);

            Cromossomo cromossomoPai = populacao.get(paiIndex);
            Cromossomo cromossomoMae = populacao.get(maeIndex);

            // Geração de 2 filhos por cruzamento
            Cromossomo cromossomoFilho01;
            Cromossomo cromossomoFilho02;

            // Realiza o cruzamento
            // Crossover / permutação
            ArrayList<ArrayList<Aula>> filhosGerados = realizarCruzamento(cromossomoPai.getSequencia(), cromossomoMae.getSequencia());
            cromossomoFilho01 = new Cromossomo();
            cromossomoFilho01.setSequencia(filhosGerados.get(0));
            cromossomoFilho02 = new Cromossomo();
            cromossomoFilho02.setSequencia(filhosGerados.get(1));

            if (ocorreuMutacao()) {
                cromossomoFilho01 = obterMutante(cromossomoFilho01);
                numeroMutantes++;
            }
            if (ocorreuMutacao()) {
                cromossomoFilho02 = obterMutante(cromossomoFilho02);
                numeroMutantes++;
            }

            // fitness reavaliado
            cromossomoFilho01.setFitnessCalculado(false);
            cromossomoFilho02.setFitnessCalculado(false);

            novaPopulacao.add(cromossomoFilho01);
            novaPopulacao.add(cromossomoFilho02);
        }
        // Atualiza a população principal com a nova geração
        populacao = novaPopulacao;
    }

    public int obterPai() {
        //seleção 
        Random rand = new Random();
        int totalPercentual = populacao.stream().limit(numeroCromossomosAptos).mapToInt(Cromossomo::getPercentual).sum();
        int roletaSpin = rand.nextInt(totalPercentual) + 1; 

        int acumulado = 0;
        for (int i = 0; i < numeroCromossomosAptos; i++) {
            acumulado += populacao.get(i).getPercentual();
            if (roletaSpin <= acumulado) {
                return i; // índice do pai selecionado
            }
        }
        return 0; // pai válido
    }

    public int obterMae(int paiIndex) {
        // a mãe não pode ser o pai / diferente o suficiente
        Random rand = new Random();
        int maeIndex;
        do {
            int totalPercentual = populacao.stream().limit(numeroCromossomosAptos).mapToInt(Cromossomo::getPercentual).sum();
            int roletaSpin = rand.nextInt(totalPercentual) + 1;

            int acumulado = 0;
            maeIndex = 0;
            for (int i = 0; i < numeroCromossomosAptos; i++) {
                acumulado += populacao.get(i).getPercentual();
                if (roletaSpin <= acumulado) {
                    maeIndex = i;
                    break;
                }
            }
        } while (maeIndex == paiIndex && numeroCromossomosAptos > 1); // !=pai 

        return maeIndex;
    }

    public ArrayList<ArrayList<Aula>> realizarCruzamento(ArrayList<Aula> sequenciaPai, ArrayList<Aula> sequenciaMae) {
        Random random = new Random();
        int tamanho = sequenciaPai.size();

        // Copiando: seq. originais para modificação
        ArrayList<Aula> filho1 = new ArrayList<>(Collections.nCopies(tamanho, (Aula) null));
        ArrayList<Aula> filho2 = new ArrayList<>(Collections.nCopies(tamanho, (Aula) null));

        // 2 pontos de corte aleatórios
        int pontoCorte1 = random.nextInt(tamanho);
        int pontoCorte2 = random.nextInt(tamanho);

        if (pontoCorte1 > pontoCorte2) {
            int temp = pontoCorte1;
            pontoCorte1 = pontoCorte2;
            pontoCorte2 = temp;
        }
        for (int i = pontoCorte1; i < pontoCorte2; i++) {
            filho1.set(i, sequenciaPai.get(i));
            filho2.set(i, sequenciaMae.get(i));
        }

        for (int i = 0; i < tamanho; i++) {
            if (i < pontoCorte1 || i >= pontoCorte2) { 
                Aula geneMae = sequenciaMae.get(i);
                if (!filho1.contains(geneMae)) { // gene da mae não está no filho1
                    filho1.set(i, geneMae);
                } else {
                    // Mapeando para o gene duplicado
                    Aula mapeado = geneMae;
                    while (filho1.contains(mapeado)) {
                        int index = sequenciaPai.indexOf(mapeado);
                        if (index == -1 || (index >= pontoCorte1 && index < pontoCorte2)) {
                            mapeado = encontrarGeneUnicoParaPreencher(filho1, sequenciaMae, random); 
                            if(mapeado == null) { 
                                mapeado = gerarAtribuicaoAulaAleatoriaValida();
                            }
                        } else {
                            mapeado = sequenciaMae.get(index);
                        }
                    }
                    filho1.set(i, mapeado);
                }

                Aula genePai = sequenciaPai.get(i);
                if (!filho2.contains(genePai)) {
                    filho2.set(i, genePai);
                } else {
                    Aula mapeado = genePai;
                    while (filho2.contains(mapeado)) {
                        int index = sequenciaMae.indexOf(mapeado);
                        if (index == -1 || (index >= pontoCorte1 && index < pontoCorte2)) {
                            mapeado = encontrarGeneUnicoParaPreencher(filho2, sequenciaPai, random);
                            if(mapeado == null) {
                                mapeado = gerarAtribuicaoAulaAleatoriaValida();
                            }
                        } else {
                            mapeado = sequenciaPai.get(index);
                        }
                    }
                    filho2.set(i, mapeado);
                }
            }
        }

        //Garantindo que todas as disciplinas estejam presentes e não duplicadas.
        repararCromossomo(filho1);
        repararCromossomo(filho2);

        ArrayList<ArrayList<Aula>> filhos = new ArrayList<>();
        filhos.add(filho1);
        filhos.add(filho2);
        return filhos;
    }

    private void repararCromossomo(ArrayList<Aula> sequencia) {
        Set<String> disciplinasPresentes = new HashSet<>();
        ArrayList<Aula> duplicatas = new ArrayList<>();

        // Identificando as disciplinas duplicadas e faltantes
        for (Aula atribuicao : sequencia) {
            if (atribuicao != null) { 
                if (disciplinasPresentes.contains(atribuicao.getDisciplinaId())) {
                    duplicatas.add(atribuicao);
                } else {
                    disciplinasPresentes.add(atribuicao.getDisciplinaId());
                }
            }
        }

        List<Disciplina> disciplinasFaltantes = todasDisciplinas.stream()
                .filter(d -> !disciplinasPresentes.contains(d.getId()))
                .collect(Collectors.toList());

        // Substituindo duplicatas por disciplinas que faltam
        Random random = new Random();
        for (Aula dup : duplicatas) {
            if (!disciplinasFaltantes.isEmpty()) {
                Disciplina disciplinaFaltante = disciplinasFaltantes.remove(0); 
                Horario horario = todosHorarios.get(random.nextInt(todosHorarios.size()));
                Sala sala = todasSalas.get(random.nextInt(todasSalas.size()));
                Professor professor = todosProfessores.get(random.nextInt(todosProfessores.size()));

                Aula novaAtribuicao = new Aula(disciplinaFaltante.getId(), horario.getId(), sala.getId(), professor.getId());

                // A posição da disciplina repetida e e substitua
                int index = sequencia.indexOf(dup);
                if (index != -1) {
                    sequencia.set(index, novaAtribuicao);
                    disciplinasPresentes.add(disciplinaFaltante.getId());
                }
            } else {
                int index = sequencia.indexOf(dup);
                if (index != -1) {
                    sequencia.set(index, null); // Selecionado para a mutação ou descartado
                }
            }
        }

        // Adicionando disciplinas que faltam 
        while (sequencia.size() < numeroGenes && !disciplinasFaltantes.isEmpty()) {
            Disciplina disciplinaFaltante = disciplinasFaltantes.remove(0);
            sequencia.add(gerarAtribuicaoAulaAleatoriaValida(disciplinaFaltante.getId())); 
        }
        // Se a seq. ficou com menos genes do que o esperado nulos, preencher
        for (int i = 0; i < sequencia.size(); i++) {
            if (sequencia.get(i) == null) {
                Disciplina disciplinaParaAtribuir = disciplinasFaltantes.isEmpty() ? null : disciplinasFaltantes.remove(0);
                if (disciplinaParaAtribuir != null) {
                    sequencia.set(i, gerarAtribuicaoAulaAleatoriaValida(disciplinaParaAtribuir.getId()));
                } else {
                    sequencia.remove(i);
                    i--;
                }
            }
        }
        // Ajustando o tamanho final da sequência 
        while(sequencia.size() > numeroGenes) {
            sequencia.remove(sequencia.size() - 1);
        }
        while(sequencia.size() < numeroGenes) {
            sequencia.add(gerarAtribuicaoAulaAleatoriaValida(null)); // Gerando qualquer disciplina que precise
        }
    }

    private Aula encontrarGeneUnicoParaPreencher(ArrayList<Aula> filho, ArrayList<Aula> sourceSequence, Random random) {
        // Encontra um gene da seq. de origem que ainda não esteja no filho
        for (Aula gene : sourceSequence) {
            if (!filho.contains(gene)) {
                return gene;
            }
        }
        return gerarAtribuicaoAulaAleatoriaValida(); // Gerando uma tupla válida completamente nova
    }


    public Cromossomo obterMutante(Cromossomo cromossomoFilho) {
        if (cromossomoFilho.getSequencia().isEmpty()) return cromossomoFilho;

        Random random = new Random();
        int posicaoParaMutar = random.nextInt(cromossomoFilho.getSequencia().size());
        Aula atribuicaoParaMutar = cromossomoFilho.getSequencia().get(posicaoParaMutar);

        // Decide qual atributo mutar (ex: horário, sala, professor)
        int tipoMutacao = random.nextInt(3); // 0 para horário, 1 para sala, 2 para professor

        Aula novaAtribuicao = null;

        switch (tipoMutacao) {
            case 0: // Mutar Horário
                String novoIdHorario = todosHorarios.get(random.nextInt(todosHorarios.size())).getId();
                novaAtribuicao = new Aula(atribuicaoParaMutar.getDisciplinaId(), novoIdHorario,
                        atribuicaoParaMutar.getSalaId(), atribuicaoParaMutar.getProfessorId());
                break;
            case 1: // Mutar Sala
                String novoIdSala = todasSalas.get(random.nextInt(todasSalas.size())).getId();
                novaAtribuicao = new Aula(atribuicaoParaMutar.getDisciplinaId(), atribuicaoParaMutar.getHorarioId(),
                        novoIdSala, atribuicaoParaMutar.getProfessorId());
                break;
            case 2: // Mutar Professor
                String novoIdProfessor = todosProfessores.get(random.nextInt(todosProfessores.size())).getId();
                novaAtribuicao = new Aula(atribuicaoParaMutar.getDisciplinaId(), atribuicaoParaMutar.getHorarioId(),
                        atribuicaoParaMutar.getSalaId(), novoIdProfessor);
                break;
        }

        if (novaAtribuicao != null) {
            cromossomoFilho.getSequencia().set(posicaoParaMutar, novaAtribuicao);
            cromossomoFilho.setFitnessCalculado(false); // Marca para recalcular fitness
        }

        return cromossomoFilho;
    }

    public boolean ocorreuMutacao() {
        return new Random().nextFloat() < taxaMutabilidade;
    }

    public double obterFitnessMedio() {

        double acumulador = 0.0;

        for (Cromossomo tempCromossomo : populacao) {
            acumulador = acumulador + tempCromossomo.getFitness();
        }

        return acumulador / populacao.size();
    }

    public void salvarPopulacaoApta(String nomeArquivo, int geracao) throws IOException {

        FileWriter writer = new FileWriter(nomeArquivo,true);

        PrintWriter saida = new PrintWriter(writer);

        saida.print("\n==================================================");
        saida.print("\nGeracao: " + geracao);

        for (int i = 0; i < numeroCromossomosAptos; i++) {

            saida.print("\nPos: " + populacao.get(i).getPosicao() + " Fit: " + populacao.get(i).getFitness() + " Per: " + populacao.get(i).getPercentual() + " Seq: " + populacao.get(i).getSequencia());
        }

        saida.close();
        writer.close();
    }

    public void imprimirPopulacaoApta() {

        for (int i = 0; i < numeroCromossomosAptos; i++) {

            System.out.print("\nPos: " + populacao.get(i).getPosicao() + " Fit: " + populacao.get(i).getFitness() + " Per: " + populacao.get(i).getPercentual() + " Seq: " + populacao.get(i).getSequencia());
        }
    }

    private static void carregarDadosUniversidadeExemplo() {
        todasDisciplinas = new ArrayList<>();
        todosProfessores = new ArrayList<>();
        todasSalas = new ArrayList<>();
        todosHorarios = new ArrayList<>();
        matriculasAlunos = new HashMap<>();

        todasDisciplinas.add(new Disciplina("COMP101", "Introdução à Programação", 30, List.of("PROF_A", "PROF_B")));
        todasDisciplinas.add(new Disciplina("MAT201", "Cálculo I", 40, List.of("PROF_C")));
        todasDisciplinas.add(new Disciplina("FIS101", "Física Básica", 25, List.of("PROF_D", "PROF_E")));
        todasDisciplinas.add(new Disciplina("LIT301", "Literatura Brasileira", 20, List.of("PROF_B")));
        todasDisciplinas.add(new Disciplina("HIST401", "História Contemporânea", 35, List.of("PROF_A")));
        todasDisciplinas.add(new Disciplina("COMP202", "Estruturas de Dados", 30, List.of("PROF_F", "PROF_A"))); // Nova disciplina para mais genes
        todasDisciplinas.add(new Disciplina("MAT302", "Álgebra Linear", 25, List.of("PROF_C")));
        todasDisciplinas.add(new Disciplina("BIO101", "Biologia Geral", 30, List.of("PROF_D")));
        todasDisciplinas.add(new Disciplina("QUIM201", "Química Orgânica", 20, List.of("PROF_E")));
        todasDisciplinas.add(new Disciplina("ARQ101", "Desenho Arquitetônico", 15, List.of("PROF_F")));

        todosProfessores.add(new Professor("PROF_A", "Prof. Ana", Set.of("SEG_8-10AM", "QUA_10-12PM", "SEX_8-10AM"), "Manha"));
        todosProfessores.add(new Professor("PROF_B", "Prof. Bruno", Set.of("SEG_10-12PM", "QUA_14-16PM", "SEX_10-12PM"), "Tarde"));
        todosProfessores.add(new Professor("PROF_C", "Prof. Carlos", Set.of("TER_8-10AM", "QUI_10-12PM", "TER_14-16PM"), "Manha"));
        todosProfessores.add(new Professor("PROF_D", "Prof. Daniela", Set.of("SEG_14-16PM", "QUA_8-10AM", "QUI_14-16PM"), "Tarde"));
        todosProfessores.add(new Professor("PROF_E", "Prof. Eduardo", Set.of("TER_10-12PM", "QUI_8-10AM", "SEX_14-16PM"), "Manha"));
        todosProfessores.add(new Professor("PROF_F", "Prof. Fatima", Set.of("SEG_8-10AM", "TER_10-12PM", "QUA_14-16PM"), "Noite"));

        todasSalas.add(new Sala("SALA_A101", "Lab. Info 1", 30, Set.of("SEG_8-10AM", "SEG_10-12PM", "TER_8-10AM", "TER_10-12PM", "QUA_8-10AM", "QUA_10-12PM")));
        todasSalas.add(new Sala("SALA_A102", "Auditorio", 50, Set.of("SEG_8-10AM", "SEG_14-16PM", "TER_8-10AM", "TER_14-16PM", "QUI_8-10AM", "QUI_14-16PM")));
        todasSalas.add(new Sala("SALA_B201", "Dominio.Sala de Dominio.Aula 1", 25, Set.of("SEG_10-12PM", "SEG_14-16PM", "QUA_10-12PM", "QUA_14-16PM", "SEX_10-12PM", "SEX_14-16PM")));
        todasSalas.add(new Sala("SALA_B202", "Dominio.Sala de Dominio.Aula 2", 35, Set.of("TER_8-10AM", "TER_10-12PM", "QUI_8-10AM", "QUI_10-12PM", "SEX_8-10AM", "SEX_10-12PM")));

        todosHorarios.add(new Horario("SEG_8-10AM", "Segunda", "08:00", "10:00", "Manha"));
        todosHorarios.add(new Horario("SEG_10-12PM", "Segunda", "10:00", "12:00", "Manha"));
        todosHorarios.add(new Horario("SEG_14-16PM", "Segunda", "14:00", "16:00", "Tarde"));
        todosHorarios.add(new Horario("TER_8-10AM", "Terca", "08:00", "10:00", "Manha"));
        todosHorarios.add(new Horario("TER_10-12PM", "Terca", "10:00", "12:00", "Manha"));
        todosHorarios.add(new Horario("TER_14-16PM", "Terca", "14:00", "16:00", "Tarde"));
        todosHorarios.add(new Horario("QUA_8-10AM", "Quarta", "08:00", "10:00", "Manha"));
        todosHorarios.add(new Horario("QUA_10-12PM", "Quarta", "10:00", "12:00", "Manha"));
        todosHorarios.add(new Horario("QUA_14-16PM", "Quarta", "14:00", "16:00", "Tarde"));
        todosHorarios.add(new Horario("QUI_8-10AM", "Quinta", "08:00", "10:00", "Manha"));
        todosHorarios.add(new Horario("QUI_10-12PM", "Quinta", "10:00", "12:00", "Manha"));
        todosHorarios.add(new Horario("QUI_14-16PM", "Quinta", "14:00", "16:00", "Tarde"));
        todosHorarios.add(new Horario("SEX_8-10AM", "Sexta", "08:00", "10:00", "Manha"));
        todosHorarios.add(new Horario("SEX_10-12PM", "Sexta", "10:00", "12:00", "Manha"));
        todosHorarios.add(new Horario("SEX_14-16PM", "Sexta", "14:00", "16:00", "Tarde"));

        matriculasAlunos.put("ALUNO_001", List.of("COMP101", "MAT201"));
        matriculasAlunos.put("ALUNO_002", List.of("COMP101", "FIS101"));
        matriculasAlunos.put("ALUNO_003", List.of("MAT201", "LIT301"));
        matriculasAlunos.put("ALUNO_004", List.of("COMP101", "COMP202"));
        matriculasAlunos.put("ALUNO_005", List.of("FIS101", "BIO101"));
        matriculasAlunos.put("ALUNO_006", List.of("MAT201", "MAT302"));


        // Povoando os mapas
        mapaDisciplinas = todasDisciplinas.stream().collect(Collectors.toMap(Disciplina::getId, d -> d));
        mapaProfessores = todosProfessores.stream().collect(Collectors.toMap(Professor::getId, p -> p));
        mapaSalas = todasSalas.stream().collect(Collectors.toMap(Sala::getId, s -> s));
        mapaHorarios = todosHorarios.stream().collect(Collectors.toMap(Horario::getId, h -> h));

        // Ajusta numeroGenes para o número total de disciplinas
        numeroGenes = todasDisciplinas.size();
    }

    // Gerando uma tupla de aula aleatória e válida (dentro das possibilidades)
    private Aula gerarAtribuicaoAulaAleatoriaValida() {
        Random random = new Random();
        Disciplina disciplina = todasDisciplinas.get(random.nextInt(todasDisciplinas.size()));
        return gerarAtribuicaoAulaAleatoriaValida(disciplina.getId());
    }

    private Aula gerarAtribuicaoAulaAleatoriaValida(String disciplinaId) {
        Random random = new Random();
        Disciplina disciplina = disciplinaId != null ? mapaDisciplinas.get(disciplinaId) : todasDisciplinas.get(random.nextInt(todasDisciplinas.size()));

        Horario horario = todosHorarios.get(random.nextInt(todosHorarios.size()));
        Sala sala = todasSalas.get(random.nextInt(todasSalas.size()));
        Professor professor = todosProfessores.get(random.nextInt(todosProfessores.size()));

        // Verificando se professor está disponível no horário escolhido
        int tentativas = 0;
        while (!professor.getHorarioDisponiveis().contains(horario.getId()) && tentativas < 100) {
            professor = todosProfessores.get(random.nextInt(todosProfessores.size()));
            horario = todosHorarios.get(random.nextInt(todosHorarios.size())); // Tenta um novo horário também
            tentativas++;
        }
        tentativas = 0;
        // Verificando se sala está disponível no horário escolhido
        while (!sala.getHorarioDisponiveisId().contains(horario.getId()) && tentativas < 100) {
            sala = todasSalas.get(random.nextInt(todasSalas.size()));
            horario = todosHorarios.get(random.nextInt(todosHorarios.size())); // Tenta um novo horário também
            tentativas++;
        }
        return new Aula(disciplina.getId(), horario.getId(), sala.getId(), professor.getId());
    }
}
