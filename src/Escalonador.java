import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Scanner;

/**
 *
 * @author Gabriel Franzoni 15105090
 */

// Classe que realiza todo o processo de escalonamento de processos

public class Escalonador{

    private Processo[] procs; //Lista de todos os processos
    private int slice, nextArrival, execTime, recursionQuit, IOExecutionTime, restrict; //Dados. recursionQuit e restrict servem para evitar loop infinito no código
    private boolean preemp = false, noMultiContext = false; //Condicionais para execução (melhor demonstrados em ação)
    private LinkedList<Integer> sortedArrivalTime; //Lista de TODOS os tempos de chegada ordenados
    private LinkedList<String> printProcess, printIO; //Gráficos para serem impressos no final
    private LinkedList<Processo> availableProcess, roundRobinEffect; //Listas auxiliares que analisam processos que podem ser executados/rodiziados
    private Processo currentProcess, IOProcess; //Objetos de auxílio que servem como ponteiros que apontam para um processo

    //Construtor
    public Escalonador(){
        sortedArrivalTime = new LinkedList<>();
        printProcess = new LinkedList<>();
        printIO = new LinkedList<>();
        availableProcess = new LinkedList<>();
        roundRobinEffect = new LinkedList<>();
        execTime = 1;
        restrict = 0;
        currentProcess = null;
        IOProcess = null;
    }
    
    //Método que faz leitura do arquivo texto e constrói o Escalonador e os Processos
    public void schRead() throws FileNotFoundException, IOException{
    
    Scanner s = new Scanner(System.in);
    System.out.println("Digite o nome do arquivo de entrada (sem extensão .txt)");
    String enter = s.nextLine();
    System.out.println();
    System.out.println("====================================================================================");
    BufferedReader in = new BufferedReader(new FileReader(enter + ".txt"));
        String line;
        int procCount = 0;
        
        //Cria-se a matriz que contém todos os processos
        line = in.readLine();
        procs = new Processo[Integer.parseInt(line)];
        if(procs.length == 0) shutdown(); //Caso não exista nenhum
        recursionQuit = Integer.parseInt(line);
        
        //Aqui, lê-se a partição
        line = in.readLine();
        slice = Integer.parseInt(line);
        
        //Agora, lê-se cada informação de cada processo e insere-os no vetor de processos
        while((line = in.readLine()) != null){
            String info[] = line.split(" ");
            if(Integer.parseInt(info[0]) <= 0 || Integer.parseInt(info[1]) <= 0) shutdown();
            if(Integer.parseInt(info[2]) > 9 || Integer.parseInt(info[2]) < 1) shutdown(); // Prioridade não está entre 1 e 9
            procs[procCount] = new Processo(slice, Integer.parseInt(info[0]), Integer.parseInt(info[1]), Integer.parseInt(info[2])); //Cria o processo
            if(info.length > 3){ //Verifica se existe processos IO e insere os tempos de IO no processo respectivo
            	for(int i = 3; i < info.length; i++){
                    if(Integer.parseInt(info[i]) < 0 || Integer.parseInt(info[i]) > Integer.parseInt(info[1])
                            || Integer.parseInt(info[i]) == Integer.parseInt(info[1])) shutdown();
                    procs[procCount].insertIOTime(Integer.parseInt(info[i]));
                }
                if(procs[procCount].IORepetition()) shutdown(); //Verifica se não há repetição de entradas para IO
            }
            procs[procCount].setId(procCount+1); //Número de identificação do processo
            procCount++;
        }
        in.close();
        
    }
    
    // INÍCIO DO ESCALONAMENTO
    public void schExec(){
    
        fillKeyLists();
        nextArrival = sortedArrivalTime.removeFirst(); //Pega o primeiro tempo de chegada
        
        //Inicia escalonamento
        runScheduler();
    }
    
    // Métodos que cria e ordena lista de todos os tempos de chegada
    private void fillKeyLists(){
        for(int i = 0; i < procs.length; i++) sortedArrivalTime.add(procs[i].getArrivalTime());
        Collections.sort(sortedArrivalTime);
    }
    
    
    
    
    //Daqui em diante, o Escalonamento é iniciadom onde runScheduler e runRepeat são os dois
    //métodos principais para a execução
    
    //Toda vez que um novo nextArrival entrar, ele irá procurar o processo correspondente a ele
    //com o método searchProcess(), que é bem importante na jogada deste algoritmo
    
    private void runScheduler(){
        //Execução padrão quando ninguém tem arrivalTime = 1
        if(execTime < nextArrival){
            noProcessPrint();
            searchProcess();
            execTime++;
        }
        else{
            searchProcess();
            comparePriority();
            execTime++;
        }
        
        //Inicia a recursão
        runRepeat();
        printInput();
        print();
    }
    
    //RECURSÃO É FEITA AQUI (identação foi importante para localizar a ordem que é executado)
    private void runRepeat(){
        
        //Acaba a recursão
        if(recursionQuit == 0) return;
        
        //Se não existir preempção, ele pode continuar
        if(!preemp){
            if(!availableProcess.isEmpty()){ //Só continua se ter processo disponível na fila
            
                comparePriority(); //Metódo que serve para estabelecer prioridades e oferecer o rodízio entre os processos de mesma prioridade
                if(preemp) runRepeat(); //Se comparePriority executou operação com preempção, ele faz recursão
                else if(!currentProcess.getIOTimeList().isEmpty()){ //Verifica se é hora de aplicar IO
                    if(currentProcess.getExecutedTime() >= currentProcess.getIOTimeList().getFirst()){
                        currentProcess.getIOTimeList().removeFirst();
                        IOProcess = currentProcess;
                        availableProcess.remove(currentProcess); //Quando entra em IO, o processo temporariamente sai da fila para executar seu IO e retorna no final
                        currentProcess = null;
                        printProcess.add("T");
                        printIO.add("X");
                        noMultiContext = false;
                        if(nextArrival != -1 && execTime == nextArrival) searchProcess();
                        //execTime++;
                        IOExecutionTime = 4;
                        runRepeatIO(); //Aqui entra para outra recursão dentro desta, exclusiva para quando há IO (infelizmente, aconteceu redundâncias devido ao tratamento de IO)
                        availableProcess.add(IOProcess);
                        runRepeat();
                    }
                    else if(currentProcess.getSlice() == 0){ //Isto confere se a vez do processo acabou em seu rodízio de prioridades
                        currentProcess.fillSlice(slice);
                        runRepeat();
                    }
                    else{ //Executa normalmente um passo
                        runStep();
                        runRepeat();
                    }
                }
                else if(currentProcess.getSlice() == 0){ //Isto confere se a vez do processo acabou em seu rodízio de prioridades (infeliz redundância)
                    currentProcess.fillSlice(slice);
                    runRepeat();
                }
                else{ //Executa normalmente um passo (infeliz redundância)
                    runStep();
                    runRepeat();
                }
            }
            else{ //Se não existir processo disponível, ele se desativa e volta a ativar quando um outro que ainda não chegou entrar na fila.
                noProcessPrint();
                searchProcess();
                execTime++;
                runRepeat();
            }
        }
        
        //Caso exista preempção (interrompeu um processo), entra aqui e verifica se deve inserir uma troca.
        //A razão disto é que este algoritmo funciona duma forma que possa haver diversas trocas constantes
        //dependendo da prioridade, então ele faz isto para evitar que ele fiquei imprimindo várias trocas
        //uma depois de outra.
        else{
            if(noMultiContext){ //noMultiContext evita que imprima duas trocas de contexto seguidas
                printProcess.add("T");
                printIO.add("X");
                if(nextArrival != -1 && execTime == nextArrival) searchProcess();
                execTime++;
                noMultiContext = false;
            }
            preemp = false;
            runRepeat();
        }
    }
    
    // Recursão dentro da recursão para processos IO. Infelizmente, é bastante semelhante à outra mas tem mudanças exclusivas em certas partes para aconchegar o IO.
    // Nota-se que quando o IOExecutionTime (constante 4) zerar, ele volta pra recursão normal.
    private void runRepeatIO(){
        
        if(IOExecutionTime == 0) return;
        
        if(!preemp){
            if(!availableProcess.isEmpty()){
            
                comparePriority();
                if(preemp) runRepeatIO();
                else if(currentProcess.getSlice() == 0){
                    currentProcess.fillSlice(slice);
                    runRepeatIO();
                }
                else{
                    runStep();
                    runRepeatIO();
                }
            }
            else{
                noProcessPrintIO();
                if(nextArrival != -1 && execTime == nextArrival) searchProcess();
                runRepeatIO();
            }
        }
        else{
            if(noMultiContext){
                printProcess.add("T");
                printIO.add(IOProcess.getId());
                if(nextArrival != -1 && execTime == nextArrival) searchProcess();
                execTime++;	
                IOExecutionTime--;
                noMultiContext = false;
            }
            preemp = false;
            runRepeatIO();
        }
    }

    //Método que procura o processo correspondente ao tempo de execução (compara com seu tempo de chegada)
    private void searchProcess(){
        for(int i = 0; i < procs.length; i++){
            if(procs[i].getArrivalTime() == nextArrival){
                availableProcess.add(procs[i]);
                restrict++;
            }
        }
        //Remove duplicatas do tempo de chegada
        //Esta condição só é atingida quando não existir mais tempo de chegada, tornando nextArrival dispensável.
        if(sortedArrivalTime.isEmpty() || availableProcess.size() == procs.length || restrict == procs.length){
            nextArrival = -1;
            return;
        }
        int oldArrival = nextArrival;
        while(nextArrival == oldArrival){
            nextArrival = sortedArrivalTime.removeFirst();
            if(sortedArrivalTime.isEmpty()) return;
        }
    }
    
    //Método que pega os processos com melhor prioridade e trata o rodízio dos mesmos
    private void comparePriority(){
        Processo oldProcess = currentProcess;
        int bestPriority = 9;
        
        //Insere todos os processos com a melhor prioridade atual em uma lista separada
        for(int i = 0; i < availableProcess.size(); i++){
            if(availableProcess.get(i).getPriority() < bestPriority){
                bestPriority = availableProcess.get(i).getPriority();
            }
        }
        for(int i = 0; i < availableProcess.size(); i++){
            if(availableProcess.get(i).getPriority() == bestPriority){
                roundRobinEffect.add(availableProcess.get(i));
            }
        }
        
        //Ele faz o rodízio (Round Robin) pelas prioridades aqui
        //Se todas estiverem falsas, ele reinicia o rodízio
        int resetCondition = roundRobinEffect.size();
        for(int i = 0; i < roundRobinEffect.size(); i++){
            if(roundRobinEffect.get(i).getRR()){
                currentProcess = roundRobinEffect.get(i);
                if (oldProcess != currentProcess) preemp = true;
                break;
            }
            resetCondition--;
            if(resetCondition == 0){
                for(int j = 0; j < roundRobinEffect.size(); j++) roundRobinEffect.get(j).desetRR();
                //Se ele reiniciar o rodízio, ele parte de volta do primeiro valor
                currentProcess = roundRobinEffect.get(0);
                preemp = true;
            }
        }
        
        //Limpa a lista auxiliar
        roundRobinEffect.clear();
    }
    
    //Método que executa um passo. Processa um segundo do processo atual (currentProcess)
    private void runStep(){
        currentProcess.executeSecond();
        currentProcess.reduceSlice();
        printProcess.add(currentProcess.getId());
        if(IOExecutionTime > 0) {
            printIO.add(IOProcess.getId());
        }else printIO.add("X");
        if(nextArrival != -1 && execTime == nextArrival) searchProcess();
        execTime++;
        if(IOExecutionTime > 0) IOExecutionTime--;
        noMultiContext = true;
        if(currentProcess.getExecutedTime() == currentProcess.getExecutionTime()){
            availableProcess.remove(currentProcess);
            currentProcess = null;
            comparePriority();
            preemp = true;
            recursionQuit--;
        }
    }
    
    //Método que imprime um traço (significando que não há processos)
    private void noProcessPrint(){
        while(execTime < nextArrival){
            printProcess.add("-");
            printIO.add("X");
            execTime++;
        }
        printProcess.add("-");
        printIO.add("X");
        noMultiContext = true;
    }
    
    //Versão para quando há IO (com mudanças). Provavelmente o método mais volátil do código
    private void noProcessPrintIO(){
        while(execTime < nextArrival || IOExecutionTime > 0){
            if(IOExecutionTime <= 0) break;
            printProcess.add("-");
            printIO.add(IOProcess.getId());
            execTime++;            
            IOExecutionTime--;
        }
        //if(IOExecutionTime == 0) availableProcess.add(IOProcess);
        noMultiContext = true;
    }
    
    
    
    //Finalmente, segue os métodos de impressão e cálculo
    private void printInput(){
    //Impressão da entrada
        System.out.println("ENTRADA");
        System.out.println("=======");
        System.out.println(procs.length);
        System.out.println(slice);
        for(int i = 0; i < procs.length; i++){
            System.out.print(procs[i].getArrivalTime() + " ");
            System.out.print(procs[i].getExecutionTime() + " ");
            System.out.print(procs[i].getPriority() + " ");
            for(int j = 0; j < procs[i].getIOTimeList().size(); j++) System.out.print(procs[i].getIOTimeList().get(j) + " ");
            System.out.println();
        }
    }
    
    private void print(){
        System.out.println();
        System.out.println();
        System.out.println("GRÁFICO");
        System.out.println("=======");
        for(int i = 0; i < printProcess.size(); i++) System.out.print(printProcess.get(i));
        System.out.println();
        for(int i = 0; i < printIO.size(); i++){
            if(printIO.get(i).equals("X"))System.out.print(" ");
            else System.out.print(printIO.get(i));
        }
        System.out.println();
    }
    
    public void calculateMetrics(){
    	double sum = 0;
    	System.out.println();
    	System.out.println("RESULTADOS");
    	System.out.println("==========");
    	calculateWait(sum);
        calculateAnswer(sum);
        calculateTurnAround(sum);
	System.out.println("====================================================================================");
	System.out.println();
    }
    
    private void calculateWait(double sum){
    	int distance = 0;
    	for(int i = 0; i < procs.length; i++){
    	    int nullTime = procs[i].getArrivalTime();
    	    for(int j = 0; j < printProcess.size(); j++){
    	    	if(nullTime <= 0){
    	    	    if(!printProcess.get(j).equals(procs[i].getId())) distance++;
    	    	    else{
    	    	        sum += distance;
    	                distance = 0;
    	    	    }
                }
                nullTime--;
    	    }
    	}
    	double result = (double)(sum/procs.length);
    	System.out.println("Tempo de espera médio: " + String.valueOf(sum/procs.length));
    }
    
    private void calculateAnswer(double sum){
    	int distance = 0;
    	for(int i = 0; i < procs.length; i++){
    	    int nullTime = procs[i].getArrivalTime();
    	    for(int j = 0; j < printProcess.size(); j++){
    	        if(nullTime <= 0){
    	            if(!printProcess.get(j).equals(procs[i].getId())) distance++;
    	            else{
    	                sum += distance;
    	                distance = 0;
    	                break;
    	            }
    	        }
    	        nullTime--;
    	    }
    	}
    	double result = (double)(sum/procs.length);
    	System.out.println("Tempo de resposta médio: " + String.valueOf(sum/procs.length));
    }
    
    private void calculateTurnAround(double sum){
    	boolean first = true;
    	int init = 0, last = 0;
    	for(int i = 0; i < procs.length; i++){
    	    for(int j = 0; j < printProcess.size(); j++){
    	    	if(printProcess.get(j).equals(procs[i].getId())){
    	    	    if(first){
    	    	        init = j;
    	    	        first = false;
    	    	    }
    	    	    last = j;
    	    	}
    	    }
    	    sum += (last-init);
    	}
    	double result = (double)(sum/procs.length);
    	System.out.println("Tempo de turn-around médio: " + String.valueOf(sum/procs.length));
    }
        
    private void shutdown(){
        System.out.print("O programa foi fechado inesperadamente (condições de entrada não atingidas)");
        System.exit(0);
    }

}
