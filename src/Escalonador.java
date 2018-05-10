package escalonalixo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;

/**
 *
 * @author Gabriel Franzoni 15105090
 */

// Classe que realiza todo o processo de escalonamento de processos

public class Escalonador{

    private Processo[] procs;
    private int slice, nextArrival, execTime, recursionQuit;
    private boolean preemp = false, noMultiContext = false;
    private LinkedList<Integer> sortedArrivalTime, sortedIOTime;
    private LinkedList<String> printProcess, printIO;
    private LinkedList<Processo> availableProcess, roundRobinEffect;
    private Processo currentProcess;

    public Escalonador(){
        sortedArrivalTime = new LinkedList<>();
        printProcess = new LinkedList<>();
        printIO = new LinkedList<>();
        availableProcess = new LinkedList<>();
        roundRobinEffect = new LinkedList<>();
        execTime = 1;
        currentProcess = null;
    }
    
    // SE USAR RECURSÃO, APAGAR O SLICETIME DOS PROCESSOS!!!!**********************
    
    //Método que faz leitura do arquivo texto e constrói o Escalonador e os Processos
    public void schRead() throws FileNotFoundException, IOException{
    BufferedReader in = new BufferedReader(new FileReader("input.txt"));
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
            procs[procCount].setPrintValue(procCount+1); //Número de identificação do processo
            procCount++;
        }
        in.close();
        printInput();
        
    }
    
    public void schExec(){
    	//Ok, deve-se retirar o primeiro valor de chegada e primeiro valor de IO (se tudo for zero, cancela).
    	//Procura o processo
    	//Insere em AvailableProcess
    	//Performa a recursão, troca de contexto toda vez. Ela é um while de "slice" vezes.
    	//Quando o IO inicia (tem que estar no processo, pega o mais cedo da lista que disponibiliza e segue em diante)
    	// 	*criar lógica de true e false que libera e sempre refazer quando entrar o próximo (ver se número repete)
    	//Executa IO e dps da troca de contexto, reinsere a merda do processo de volta nos AvailableProcess e segue o baile.
    	//	*nota-se que se n ter available mesmo tempo IO vai imprimir os "---", AND THAT'S EXACTLY WHAT I FUCKING NEED!!!
    	// Fazer relatório enquanto estiver fazendo merda na PUCRS!!!
        
        fillKeyLists();
        
        nextArrival = sortedArrivalTime.removeFirst();
        
        //Inicia escalonamento
        runScheduler();
        
    }
    
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
            printProcess.add(currentProcess.getId());
            printIO.add("X");
            execTime++;
        }
        
        //Inicia a recursão
        runRepeat();
        print();
    }
    
    private void runRepeat(){
        
        if(recursionQuit == 0) return;
        
        if(!preemp){
            //ver se tem availableprocess
            
            comparePriority();
            if(preemp) runRepeat();
            /*else if(!currentProcess.getIOTimeList().isEmpty()){
           //ver tempo que foi executado <=, getfirst, removefirst, quando entrar, retira e mete um run com o priority
            }*/
            else if(currentProcess.getSlice() == 0){
                currentProcess.fillSlice(slice);
                runRepeat();
            }
            else{
                runStep();
                runRepeat();
            }
        }
        else{
            if(noMultiContext){
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
    
    private void runStep(){
        currentProcess.executeSecond();
        currentProcess.reduceSlice();
        printProcess.add(currentProcess.getId());
        printIO.add("X");
        if(nextArrival != -1 && execTime == nextArrival) searchProcess();
        execTime++;
        noMultiContext = true;
        if(currentProcess.getExecutedTime() == currentProcess.getExecutionTime()){
            availableProcess.remove(currentProcess);
            currentProcess = null;
            comparePriority();
            preemp = true;
            recursionQuit--;
        }
    }
    
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
    
    //se der uns exception, pode ser aqui o problema
    private void searchProcess(){
        for(int i = 0; i < procs.length; i++){
            if(procs[i].getArrivalTime() == nextArrival){
                availableProcess.add(procs[i]);
                System.out.println("asdasdsadasd " + execTime + " " + nextArrival + " " + procs[i].getArrivalTime());
            }
        }
        //Remove duplicatas do tempo de chegada
        //Esta condição só é atingida quando não existir mais tempo de chegada, tornando nextArrival dispensável.
        if(sortedArrivalTime.isEmpty() || availableProcess.size() == procs.length){
            nextArrival = -1;
            return;
        }
        int oldArrival = nextArrival;
        while(nextArrival == oldArrival){
            nextArrival = sortedArrivalTime.removeFirst();
            if(sortedArrivalTime.isEmpty()) return;
        }
    }
    
    //cuidar
    private void noProcessPrint(){
        while(execTime < nextArrival){
            printProcess.add("-");
            printIO.add("X");
            execTime++;
        }
        printProcess.add("-");
        printIO.add("X");
        noMultiContext = true;
        // testing shit
        System.out.println();
        System.out.println(execTime);
    }
    
    private void fillKeyLists(){
        for(int i = 0; i < procs.length; i++) sortedArrivalTime.add(procs[i].getArrivalTime());
        Collections.sort(sortedArrivalTime);
        
        //TESTING CRAP
        System.out.println();
        for(int i = 0; i < sortedArrivalTime.size(); i++) System.out.print(sortedArrivalTime.get(i) + " ");
    }
    
    private void printInput(){
    //Impressão da entrada
        System.out.println("Entrada:");
        System.out.println("========");
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
        for(int i = 0; i < printProcess.size(); i++) System.out.print(printProcess.get(i));
    }
        
    private void shutdown(){
        System.out.print("O programa foi fechado inesperadamente (condições de entrada não atingidas)");
        System.exit(0);
    }

}
