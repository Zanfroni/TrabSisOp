package escalonalixo;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author Gabriel Franzoni 15105090
 */
public class Processo {

    private String id;
    private int arrivalTime, executionTime, priority, refresh; //refresh talvez caia fora
    private LinkedList<Integer> IOTimeList;

    public Processo(int refresh, int arrivalTime, int executionTime, int priority){
    	this.refresh = refresh;
    	this.arrivalTime = arrivalTime;
    	this.executionTime = executionTime;
    	this.priority = priority;
    	IOTimeList = new LinkedList<>();
    }
    
    public void insertIOTime(int n){
    	IOTimeList.add(n);
    }
    
    public void setPrintValue(int n){
        id = String.valueOf(n);
    }
    
    public int getaT(){
        return arrivalTime;
    }
    public int geteT(){
        return executionTime;
    }
    public int getp(){
        return priority;
    }
    public LinkedList<Integer> getio(){
        return IOTimeList;
    }
}
