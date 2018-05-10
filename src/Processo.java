package escalonalixo;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author Gabriel Franzoni 15105090
 */
public class Processo {

    private String id;
    private int arrivalTime, executionTime, priority, refresh, executedTime; //refresh talvez caia fora
    private LinkedList<Integer> IOTimeList;

    public Processo(int refresh, int arrivalTime, int executionTime, int priority){
    	this.refresh = refresh;
    	this.arrivalTime = arrivalTime;
    	this.executionTime = executionTime;
    	this.priority = priority;
        executedTime = 0;
    	IOTimeList = new LinkedList<>();
    }
    
    public void insertIOTime(int n){
    	IOTimeList.add(n);
    }
    
    public void setPrintValue(int n){
        id = String.valueOf(n);
    }
    
    public int getArrivalTime(){
        return arrivalTime;
    }
    
    public int getExecutionTime(){
        return executionTime;
    }
    
    public int getPriority(){
        return priority;
    }
    
    public void executeSecond(){
        executedTime++;
    }
    
    public int getExecutedTime(){
        return executedTime;
    }
    
    public LinkedList<Integer> getIOTimeList(){
        return IOTimeList;
    }
}
