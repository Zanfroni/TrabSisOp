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
    private int slice;
    private LinkedList<Integer> sortedArrivalTime, sortedIOTime;

    public Escalonador(){
        sortedArrivalTime = new LinkedList<>();
        sortedIOTime = new LinkedList<>();
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
        
        //Aqui, lê-se a partição
        line = in.readLine();
        slice = Integer.parseInt(line);
        
        //Agora, lê-se cada informação de cada processo e insere-os no vetor de processos
        while((line = in.readLine()) != null){
            String info[] = line.split(" ");
            if(Integer.parseInt(info[2]) > 9 || Integer.parseInt(info[2]) < 1) shutdown(); // Prioridade não está entre 1 e 9
            procs[procCount] = new Processo(slice, Integer.parseInt(info[0]), Integer.parseInt(info[1]), Integer.parseInt(info[2])); //Cria o processo
            if(info.length > 3){ //Verifica se existe processos IO e insere os tempos de IO no processo respectivo
            	for(int i = 3; i < info.length; i++) procs[procCount].insertIOTime(Integer.parseInt(info[i]));
            }
            //else procs[procCount].insertIOTime(0); SERÁ QUE REALMENTE PRECISA DISTO???
            procs[procCount].setPrintValue(procCount+1); //Número de identificação do processo
            procCount++;
        }
        in.close();
        printInput();
        
    }
    
    public void schExec(){
    	//Ok, deve-se retirar o primeiro valor de chegada e primeiro valor de IO (se tudo for zero, cancela).
    	//Procura o processo
    	//Insere em AvaliableProcess
    	//Performa a recursão, troca de contexto toda vez. Ela é um while de "slice" vezes.
    	//Quando o IO inicia (tem que estar no processo, pega o mais cedo da lista que disponibiliza e segue em diante)
    	// 	*criar lógica de true e false que libera e sempre refazer quando entrar o próximo (ver se número repete)
    	//Executa IO e dps da troca de contexto, reinsere a merda do processo de volta nos AvaliableProcess e segue o baile.
    	//	*nota-se que se n ter avaliable mesmo tempo IO vai imprimir os "---", AND THAT'S EXACTLY WHAT I FUCKING NEED!!!
    	// Fazer relatório enquanto estiver fazendo merda na PUCRS!!!
        
        fillKeyLists();
    }
    
    private void fillKeyLists(){
        for(int i = 0; i < procs.length; i++){
            sortedArrivalTime.add(procs[i].getaT());
            for(int j = 0; j < procs[i].getio().size(); j++) sortedIOTime.add(procs[i].getio().get(j));
        }
        Collections.sort(sortedArrivalTime);
        Collections.sort(sortedIOTime);
        
        //TESTING CRAP
        
        System.out.println();
        System.out.println("coco???");
        for(int i = 0; i < sortedArrivalTime.size(); i++) System.out.print(sortedArrivalTime.get(i) + " ");
        System.out.println();
        for(int i = 0; i < sortedIOTime.size(); i++) System.out.print(sortedIOTime.get(i) + " ");
    }
    
    private void printInput(){
    //Impressão da entrada
        System.out.println("Entrada:");
        System.out.println("========");
        System.out.println(procs.length);
        System.out.println(slice);
        for(int i = 0; i < procs.length; i++){
            System.out.print(procs[i].getaT() + " ");
            System.out.print(procs[i].geteT() + " ");
            System.out.print(procs[i].getp() + " ");
            for(int j = 0; j < procs[i].getio().size(); j++) System.out.print(procs[i].getio().get(j) + " ");
            System.out.println();
        }
    }
        
    private void shutdown(){
        System.out.print("O programa foi fechado inesperadamente (condições de entrada não atingidas)");
        System.exit(0);
    }

}
