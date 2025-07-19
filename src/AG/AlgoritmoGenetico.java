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
    public static Map<String, List<String>> matriculasAlunos; // Dominio.Aluno ID -> Lista de Disciplinas ID

    // Mapeamentos para busca rápida
    public static Map<String, Disciplina> mapaDisciplinas;
    public static Map<String, Professor> mapaProfessores;
    public static Map<String, Sala> mapaSalas;
    public static Map<String, Horario> mapaHorarios;


    public static void main(String[] args) throws IOException {
        // Inicializar dados da universidade (Exemplo - você precisará carregar seus dados reais)
        carregarDadosUniversidadeExemplo();

        FileWriter writer = new FileWriter(numeroCromossomos + "_"+ numeroEvolucoes + "_saida_" +".txt",true);

        PrintWriter saida = new PrintWriter(writer);

        // Obter instância da classe
        AlgoritmoGenetico ag = AlgoritmoGenetico.getInstancia();

        // Criando lista de cromossomos
        ArrayList<Cromossomo> pop = ag.getPopulacao();

        // Início criação da população (randomica)
        ag.gerarPopulacao();

        // Calcular Fitness dos cromossomos
        ag.definirFitnessScore();

        // Selecao cromossomos (Ordenar decrescente por valor de Fitness)
        ag.selecao();

        // Início dos cruzamentos
        for (int i = 0; i < numeroEvolucoes; i++) {

            // Cruzamento
            ag.cruzamentos();

            // Calcular Fitness dos cromossomos (recalcula para novos e mutados)
            ag.definirFitnessScore();

            // Selecao cromossomos (Ordenar decrescente por valor de Fitness)
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

            // Salvando resultados em arquivo RES
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

        // Para cada disciplina, tente atribuir um horário, sala e professor aleatórios
        // O número de genes (numeroGenes) deve ser igual ao número total de disciplinas OU ao número total de aulas/periodos
        // Para o problema dado, cada disciplina precisa de uma atribuição, então é o número de disciplinas.
        List<Disciplina> disciplinasNaoAtribuidas = new ArrayList<>(todasDisciplinas);

        for (int i = 0; i < numeroGenes; i++) {
            if (disciplinasNaoAtribuidas.isEmpty()) break; // Todas as disciplinas foram atribuídas

            Disciplina disciplina = disciplinasNaoAtribuidas.remove(random.nextInt(disciplinasNaoAtribuidas.size()));

            // Tenta encontrar uma atribuição válida aleatória
            // Isso pode ser complexo, pois envolve verificar compatibilidades desde o início.
            // Para simplicidade, faremos uma atribuição aleatória e deixaremos o fitness penalizar as inválidas.

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
            // Recalcular fitness apenas se não foi calculado ou se foi alterado por mutação/cruzamento
            if (!tempCromossomo.isFitnessCalculado()) {
                double fitnessAtual = 100000.0; // Pontuação base alta

                // Estruturas auxiliares para verificar conflitos
                // Dominio.Horario+Dominio.Sala -> Atribuicoes (para checar sala ocupada)
                Map<String, List<Aula>> horarioSalaMap = new HashMap<>();
                // Dominio.Horario+Dominio.Professor -> Atribuicoes (para checar professor ocupado)
                Map<String, List<Aula>> horarioProfessorMap = new HashMap<>();
                // AlunoId -> Horarios (para checar aluno ocupado)
                Map<String, List<Horario>> alunoHorariosMap = new HashMap<>();

                // Popular mapas auxiliares e verificar algumas restrições rígidas
                for (Aula aula : tempCromossomo.getSequencia()) {
                    // Verificar se os IDs existem nos dados da universidade
                    if (!mapaDisciplinas.containsKey(aula.getDisciplinaId()) ||
                            !mapaHorarios.containsKey(aula.getHorarioId()) ||
                            !mapaSalas.containsKey(aula.getSalaId()) ||
                            !mapaProfessores.containsKey(aula.getProfessorId())) {
                        fitnessAtual -= 2000; // Penalidade muito alta para atribuição inválida
                        continue; // Pula para a próxima atribuição
                    }

                    // Obter objetos completos
                    Disciplina disciplina = mapaDisciplinas.get(aula.getDisciplinaId());
                    Horario horario = mapaHorarios.get(aula.getHorarioId());
                    Sala sala = mapaSalas.get(aula.getSalaId());
                    Professor professor = mapaProfessores.get(aula.getProfessorId());


                    // Restrições Rígidas - Penalidades Altas

                    // 1. Conflito de Dominio.Sala (uma sala, um horário)
                    String chaveSalaHorario = aula.salaId + "_" + aula.getHorarioId();
                    horarioSalaMap.computeIfAbsent(chaveSalaHorario, k -> new ArrayList<>()).add(aula);
                    if (horarioSalaMap.get(chaveSalaHorario).size() > 1) {
                        fitnessAtual -= 1000; // Penalidade por sala ocupada
                    }

                    // 2. Conflito de Dominio.Professor (um professor, um horário)
                    String chaveProfessorHorario = aula.getProfessorId() + "_" + aula.getHorarioId();
                    horarioProfessorMap.computeIfAbsent(chaveProfessorHorario, k -> new ArrayList<>()).add(aula);
                    if (horarioProfessorMap.get(chaveProfessorHorario).size() > 1) {
                        fitnessAtual -= 1000; // Penalidade por professor ocupado
                    }

                    // 3. Disponibilidade do Dominio.Professor
                    if (!professor.getHorarioDisponiveis().contains(aula.getHorarioId())) {
                        fitnessAtual -= 1000; // Dominio.Professor não disponível
                    }

                    // 4. Capacidade da Dominio.Sala
                    if (disciplina.getNumeroAlunosEstimado() > sala.getCapacidade()) {
                        fitnessAtual -= 1000; // Dominio.Sala muito pequena
                    }

                    // 5. Preferência de Dominio.Professor (se a disciplina tem professores preferidos e não é um deles)
                    if (disciplina.getProfessoresPreferidosIds() != null &&
                            !disciplina.getProfessoresPreferidosIds().isEmpty() &&
                            !disciplina.getProfessoresPreferidosIds().contains(aula.getProfessorId())) {
                        fitnessAtual -= 200; // Penalidade média para professor não preferido
                    }

                    // Restrições Flexíveis - Penalidades Menores

                    // 6. Preferência de Turno do Dominio.Professor
                    if (professor.getPreferenciaTurno() != null && !professor.getPreferenciaTurno().isEmpty() &&
                            !professor.getPreferenciaTurno().equals(horario.getTurno())) {
                        fitnessAtual -= 50; // Penalidade menor por turno não preferido
                    }

                    // 7. Conflitos de Horário para Dominio.Aluno (Lógica mais complexa, pois depende de TODAS as disciplinas do aluno)
                    // Para cada aluno matriculado nesta disciplina, adicione o horário ao mapa de horários do aluno
                    for (Map.Entry<String, List<String>> entry : matriculasAlunos.entrySet()) {
                        String alunoId = entry.getKey();
                        List<String> disciplinasDoAluno = entry.getValue();

                        if (disciplinasDoAluno.contains(aula.getDisciplinaId())) {
                            // Adiciona o horário ao mapa de horários do aluno
                            alunoHorariosMap.computeIfAbsent(alunoId, k -> new ArrayList<>()).add(horario);
                        }
                    }
                }

                // Verificar conflitos de horário para Alunos após popular o mapa
                for (List<Horario> horariosAluno : alunoHorariosMap.values()) {
                    for (int i = 0; i < horariosAluno.size(); i++) {
                        for (int j = i + 1; j < horariosAluno.size(); j++) {
                            if (horariosAluno.get(i).sobrepoe(horariosAluno.get(j))) {
                                fitnessAtual -= 500; // Penalidade por conflito de aluno
                            }
                        }
                    }
                }

                // Garantir que o fitness não seja negativo
                if (fitnessAtual < 0) {
                    fitnessAtual = 0;
                }
                tempCromossomo.setFitness(fitnessAtual);
            }
        }
    }

    public void selecao() {

        Collections.sort (populacao, new CompararCromosso(false)); // False para decrescente (maior fitness = melhor)

        int percentual = 100;

        // Atribuir percentual de seleção (para seleção tipo roleta)
        for (Cromossomo tempCromossomo : populacao) {
            tempCromossomo.setPosicao(populacao.indexOf(tempCromossomo));

            // Lógica de percentual pode ser ajustada para sua necessidade
            percentual = percentual - 10; // Exemplo de decaimento
            if (percentual < 10){
                percentual = 5; // Mínimo
            }

            tempCromossomo.setPercentual(percentual);
        }

    }

    public void cruzamentos() {

        int paiIndex, maeIndex;

        // Clonar a população atual para evitar ConcurrentModificationException
        // e permitir que novos filhos sejam adicionados sem interferir na seleção de pais da geração atual.
        ArrayList<Cromossomo> novaPopulacao = new ArrayList<>();
        // Adiciona os cromossomos mais aptos da geração atual à nova população para elitismo
        for(int i = 0; i < numeroCromossomosAptos; i++) {
            novaPopulacao.add(populacao.get(i));
        }


        for (int i = 0; i < (numeroCromossomos - numeroCromossomosAptos) / 2; i++) { // Gerar novos filhos para preencher o resto da população

            // Pegando 2 pais aleatoriamente (usando seleção por percentual/roleta)
            paiIndex = obterPai();
            maeIndex = obterMae(paiIndex);

            Cromossomo cromossomoPai = populacao.get(paiIndex);
            Cromossomo cromossomoMae = populacao.get(maeIndex);

            // Geração de 2 filhos por cruzamento
            Cromossomo cromossomoFilho01;
            Cromossomo cromossomoFilho02;

            // Realiza o cruzamento
            // PMX (Partially Mapped Crossover) é uma boa opção para problemas de permutação (como este onde disciplinas precisam ser únicas)
            // ou Order Crossover. A lógica abaixo é uma adaptação de PMX/Order.
            ArrayList<ArrayList<Aula>> filhosGerados = realizarCruzamento(cromossomoPai.getSequencia(), cromossomoMae.getSequencia());
            cromossomoFilho01 = new Cromossomo();
            cromossomoFilho01.setSequencia(filhosGerados.get(0));
            cromossomoFilho02 = new Cromossomo();
            cromossomoFilho02.setSequencia(filhosGerados.get(1));

            // Aplica mutação
            if (ocorreuMutacao()) {
                cromossomoFilho01 = obterMutante(cromossomoFilho01);
                numeroMutantes++;
            }
            if (ocorreuMutacao()) {
                cromossomoFilho02 = obterMutante(cromossomoFilho02);
                numeroMutantes++;
            }

            // Marca fitness como não calculado para que seja reavaliado
            cromossomoFilho01.setFitnessCalculado(false);
            cromossomoFilho02.setFitnessCalculado(false);

            novaPopulacao.add(cromossomoFilho01);
            novaPopulacao.add(cromossomoFilho02);
        }
        // Atualiza a população principal com a nova geração
        populacao = novaPopulacao;
    }

    public int obterPai() {
        // Implementação da seleção por roleta
        Random rand = new Random();
        int totalPercentual = populacao.stream().limit(numeroCromossomosAptos).mapToInt(Cromossomo::getPercentual).sum();
        int roletaSpin = rand.nextInt(totalPercentual) + 1; // 1 a totalPercentual

        int acumulado = 0;
        for (int i = 0; i < numeroCromossomosAptos; i++) {
            acumulado += populacao.get(i).getPercentual();
            if (roletaSpin <= acumulado) {
                return i; // Retorna o índice do pai selecionado
            }
        }
        return 0; // Fallback, deve sempre retornar um pai válido
    }

    public int obterMae(int paiIndex) {
        // Implementação da seleção por roleta, garantindo que a mãe não seja o pai (ou que seja diferente o suficiente)
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
        } while (maeIndex == paiIndex && numeroCromossomosAptos > 1); // Evita pegar o mesmo pai se houver outras opções

        return maeIndex;
    }

    // Adaptação para o Cruzamento (PMX - Partially Mapped Crossover)
    public ArrayList<ArrayList<Aula>> realizarCruzamento(ArrayList<Aula> sequenciaPai, ArrayList<Aula> sequenciaMae) {
        Random random = new Random();
        int tamanho = sequenciaPai.size();

        // Cópia das sequências originais para modificação
        ArrayList<Aula> filho1 = new ArrayList<>(Collections.nCopies(tamanho, (Aula) null));
        ArrayList<Aula> filho2 = new ArrayList<>(Collections.nCopies(tamanho, (Aula) null));

        // Escolha de dois pontos de corte aleatórios
        int pontoCorte1 = random.nextInt(tamanho);
        int pontoCorte2 = random.nextInt(tamanho);

        if (pontoCorte1 > pontoCorte2) {
            int temp = pontoCorte1;
            pontoCorte1 = pontoCorte2;
            pontoCorte2 = temp;
        }

        // 1. Copia o segmento entre os pontos de corte diretamente
        for (int i = pontoCorte1; i < pontoCorte2; i++) {
            filho1.set(i, sequenciaPai.get(i));
            filho2.set(i, sequenciaMae.get(i));
        }

        // 2. Preenche o restante dos filhos, lidando com mapeamentos
        for (int i = 0; i < tamanho; i++) {
            if (i < pontoCorte1 || i >= pontoCorte2) { // Fora do segmento copiado
                // Para filho1, preenche com gene da mae, respeitando mapeamento
                Aula geneMae = sequenciaMae.get(i);
                if (!filho1.contains(geneMae)) { // Se o gene da mae não está no filho1
                    filho1.set(i, geneMae);
                } else {
                    // Encontrar o mapeamento para o gene duplicado
                    Aula mapeado = geneMae;
                    while (filho1.contains(mapeado)) {
                        int index = sequenciaPai.indexOf(mapeado);
                        if (index == -1 || (index >= pontoCorte1 && index < pontoCorte2)) {
                            // Se o gene não está no pai ou está no segmento copiado,
                            // precisamos encontrar um gene único para preencher.
                            // Isso é a parte mais complexa: encontrar uma atribuição válida
                            // que não esteja duplicada e preencha um slot vazio.
                            // Por simplicidade aqui, podemos pegar o gene de outro local do pai
                            // ou gerar um novo se necessário, mas o PMX original teria um mapeamento direto.
                            // Para agendamento, o reparo final após o cruzamento pode ser mais prático.
                            mapeado = encontrarGeneUnicoParaPreencher(filho1, sequenciaMae, random); // Função auxiliar complexa
                            if(mapeado == null) { // Fallback se não encontrar
                                mapeado = gerarAtribuicaoAulaAleatoriaValida();
                            }
                        } else {
                            mapeado = sequenciaMae.get(index);
                        }
                    }
                    filho1.set(i, mapeado);
                }

                // Para filho2, preenche com gene do pai, respeitando mapeamento
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

        // Reparo adicional pós-cruzamento: Garantir que todas as disciplinas estejam presentes e não duplicadas.
        // Isso é crucial, pois o PMX simples para sequências de itens únicos não é perfeito para AtribuicaoAula.
        repararCromossomo(filho1);
        repararCromossomo(filho2);

        ArrayList<ArrayList<Aula>> filhos = new ArrayList<>();
        filhos.add(filho1);
        filhos.add(filho2);
        return filhos;
    }

    // Método auxiliar para reparo (muito importante)
    private void repararCromossomo(ArrayList<Aula> sequencia) {
        Set<String> disciplinasPresentes = new HashSet<>();
        ArrayList<Aula> duplicatas = new ArrayList<>();

        // Identificar disciplinas duplicadas e faltantes
        for (Aula atribuicao : sequencia) {
            if (atribuicao != null) { // Pode haver nulos se o inicializarCromossomo for deficiente
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

        // Substituir duplicatas por disciplinas faltantes
        Random random = new Random();
        for (Aula dup : duplicatas) {
            if (!disciplinasFaltantes.isEmpty()) {
                Disciplina disciplinaFaltante = disciplinasFaltantes.remove(0); // Pega a primeira faltante
                // Crie uma nova atribuição para a disciplina faltante, talvez copiando os outros atributos da duplicata
                // Ou gerando novos aleatoriamente (e verificando validade)
                Horario horario = todosHorarios.get(random.nextInt(todosHorarios.size()));
                Sala sala = todasSalas.get(random.nextInt(todasSalas.size()));
                Professor professor = todosProfessores.get(random.nextInt(todosProfessores.size()));

                Aula novaAtribuicao = new Aula(disciplinaFaltante.getId(), horario.getId(), sala.getId(), professor.getId());

                // Encontre a posição da duplicata e substitua
                int index = sequencia.indexOf(dup);
                if (index != -1) {
                    sequencia.set(index, novaAtribuicao);
                    disciplinasPresentes.add(disciplinaFaltante.getId());
                }
            } else {
                // Se não há mais disciplinas faltantes, apenas remova a duplicata ou a torne nula
                int index = sequencia.indexOf(dup);
                if (index != -1) {
                    sequencia.set(index, null); // Marca para ser preenchido por mutação ou descartado
                }
            }
        }

        // Adicionar quaisquer disciplinas que ainda estejam faltando (se o tamanho da sequência for menor que numeroGenes)
        // Isso pode acontecer se houver muitos nulos ou remoções.
        while (sequencia.size() < numeroGenes && !disciplinasFaltantes.isEmpty()) {
            Disciplina disciplinaFaltante = disciplinasFaltantes.remove(0);
            sequencia.add(gerarAtribuicaoAulaAleatoriaValida(disciplinaFaltante.getId())); // Cria nova atribuição válida
        }
        // Se a sequência ficou com menos genes do que o esperado (por exemplo, após remover duplicatas e não conseguir repor)
        // ou se ainda houver nulos, preencher
        for (int i = 0; i < sequencia.size(); i++) {
            if (sequencia.get(i) == null) {
                Disciplina disciplinaParaAtribuir = disciplinasFaltantes.isEmpty() ? null : disciplinasFaltantes.remove(0);
                if (disciplinaParaAtribuir != null) {
                    sequencia.set(i, gerarAtribuicaoAulaAleatoriaValida(disciplinaParaAtribuir.getId()));
                } else {
                    // Se não há mais disciplinas faltantes, remover o null ou preencher com algo genérico
                    sequencia.remove(i);
                    i--; // Ajusta o índice
                }
            }
        }
        // Ajusta o tamanho final da sequência para numeroGenes
        while(sequencia.size() > numeroGenes) {
            sequencia.remove(sequencia.size() - 1);
        }
        while(sequencia.size() < numeroGenes) {
            sequencia.add(gerarAtribuicaoAulaAleatoriaValida(null)); // Gera para qualquer disciplina que precise
        }
    }

    // Auxiliar para cruzamento PMX - encontra um gene que não está em 'filho'
    private Aula encontrarGeneUnicoParaPreencher(ArrayList<Aula> filho, ArrayList<Aula> sourceSequence, Random random) {
        // Tenta encontrar um gene da sequência de origem que ainda não esteja no filho
        for (Aula gene : sourceSequence) {
            if (!filho.contains(gene)) {
                return gene;
            }
        }
        // Se todos os genes da sequência de origem já estão no filho,
        // isso significa que precisamos de uma atribuição totalmente nova
        // para uma disciplina que está faltando.
        return gerarAtribuicaoAulaAleatoriaValida(); // Gerar uma atribuição válida completamente nova
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
            case 1: // Mutar Dominio.Sala
                String novoIdSala = todasSalas.get(random.nextInt(todasSalas.size())).getId();
                novaAtribuicao = new Aula(atribuicaoParaMutar.getDisciplinaId(), atribuicaoParaMutar.getHorarioId(),
                        novoIdSala, atribuicaoParaMutar.getProfessorId());
                break;
            case 2: // Mutar Dominio.Professor
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

    // ====================================================================================
    // Métodos Auxiliares para Carregar Dados da Universidade (Apenas para Exemplo!)
    // Você precisará substituir isso com sua própria lógica de carregamento de dados.
    // ====================================================================================
    private static void carregarDadosUniversidadeExemplo() {
        todasDisciplinas = new ArrayList<>();
        todosProfessores = new ArrayList<>();
        todasSalas = new ArrayList<>();
        todosHorarios = new ArrayList<>();
        matriculasAlunos = new HashMap<>();

        // Exemplo de Disciplinas
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


        // Exemplo de Professores
        todosProfessores.add(new Professor("PROF_A", "Prof. Ana", Set.of("SEG_8-10AM", "QUA_10-12PM", "SEX_8-10AM"), "Manha"));
        todosProfessores.add(new Professor("PROF_B", "Prof. Bruno", Set.of("SEG_10-12PM", "QUA_14-16PM", "SEX_10-12PM"), "Tarde"));
        todosProfessores.add(new Professor("PROF_C", "Prof. Carlos", Set.of("TER_8-10AM", "QUI_10-12PM", "TER_14-16PM"), "Manha"));
        todosProfessores.add(new Professor("PROF_D", "Prof. Daniela", Set.of("SEG_14-16PM", "QUA_8-10AM", "QUI_14-16PM"), "Tarde"));
        todosProfessores.add(new Professor("PROF_E", "Prof. Eduardo", Set.of("TER_10-12PM", "QUI_8-10AM", "SEX_14-16PM"), "Manha"));
        todosProfessores.add(new Professor("PROF_F", "Prof. Fatima", Set.of("SEG_8-10AM", "TER_10-12PM", "QUA_14-16PM"), "Noite"));


        // Exemplo de Salas
        todasSalas.add(new Sala("SALA_A101", "Lab. Info 1", 30, Set.of("SEG_8-10AM", "SEG_10-12PM", "TER_8-10AM", "TER_10-12PM", "QUA_8-10AM", "QUA_10-12PM")));
        todasSalas.add(new Sala("SALA_A102", "Auditorio", 50, Set.of("SEG_8-10AM", "SEG_14-16PM", "TER_8-10AM", "TER_14-16PM", "QUI_8-10AM", "QUI_14-16PM")));
        todasSalas.add(new Sala("SALA_B201", "Dominio.Sala de Dominio.Aula 1", 25, Set.of("SEG_10-12PM", "SEG_14-16PM", "QUA_10-12PM", "QUA_14-16PM", "SEX_10-12PM", "SEX_14-16PM")));
        todasSalas.add(new Sala("SALA_B202", "Dominio.Sala de Dominio.Aula 2", 35, Set.of("TER_8-10AM", "TER_10-12PM", "QUI_8-10AM", "QUI_10-12PM", "SEX_8-10AM", "SEX_10-12PM")));


        // Exemplo de Horários
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


        // Exemplo de Matrículas de Alunos
        // Dominio.Aluno 1 matriculado em COMP101, MAT201
        matriculasAlunos.put("ALUNO_001", List.of("COMP101", "MAT201"));
        // Dominio.Aluno 2 matriculado em COMP101, FIS101
        matriculasAlunos.put("ALUNO_002", List.of("COMP101", "FIS101"));
        // Dominio.Aluno 3 matriculado em MAT201, LIT301
        matriculasAlunos.put("ALUNO_003", List.of("MAT201", "LIT301"));
        // Dominio.Aluno 4 matriculado em COMP101, COMP202
        matriculasAlunos.put("ALUNO_004", List.of("COMP101", "COMP202"));
        // Dominio.Aluno 5 matriculado em FIS101, BIO101
        matriculasAlunos.put("ALUNO_005", List.of("FIS101", "BIO101"));
        // Dominio.Aluno 6 matriculado em MAT201, MAT302
        matriculasAlunos.put("ALUNO_006", List.of("MAT201", "MAT302"));


        // Preencher mapas para acesso rápido
        mapaDisciplinas = todasDisciplinas.stream().collect(Collectors.toMap(Disciplina::getId, d -> d));
        mapaProfessores = todosProfessores.stream().collect(Collectors.toMap(Professor::getId, p -> p));
        mapaSalas = todasSalas.stream().collect(Collectors.toMap(Sala::getId, s -> s));
        mapaHorarios = todosHorarios.stream().collect(Collectors.toMap(Horario::getId, h -> h));

        // Ajusta numeroGenes para o número total de disciplinas
        numeroGenes = todasDisciplinas.size();
    }

    // Método auxiliar para gerar uma atribuição aula aleatória e válida (dentro das possibilidades)
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

        // Verificar se professor está disponível no horário escolhido
        int tentativas = 0;
        while (!professor.getHorarioDisponiveis().contains(horario.getId()) && tentativas < 100) {
            professor = todosProfessores.get(random.nextInt(todosProfessores.size()));
            horario = todosHorarios.get(random.nextInt(todosHorarios.size())); // Tenta um novo horário também
            tentativas++;
        }
        tentativas = 0;
        // Verificar se sala está disponível no horário escolhido
        while (!sala.getHorarioDisponiveisId().contains(horario.getId()) && tentativas < 100) {
            sala = todasSalas.get(random.nextInt(todasSalas.size()));
            horario = todosHorarios.get(random.nextInt(todosHorarios.size())); // Tenta um novo horário também
            tentativas++;
        }
        return new Aula(disciplina.getId(), horario.getId(), sala.getId(), professor.getId());
    }
}
