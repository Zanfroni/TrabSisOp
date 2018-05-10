package escalonalixo;

import java.util.LinkedList;
import java.util.Collections;

/**
 *
 * @author Gabriel Franzoni 15105090
 */
public class Processo {

    private String id;
    private int arrivalTime, executionTime, priority, sliceTime, executedTime;
    private boolean roundRobin = true;
    private LinkedList<Integer> IOTimeList;

    public Processo(int sliceTime, int arrivalTime, int executionTime, int priority){
    	this.sliceTime = sliceTime;
    	this.arrivalTime = arrivalTime;
    	this.executionTime = executionTime;
    	this.priority = priority;
        executedTime = 0;
    	IOTimeList = new LinkedList<>();
    }
    
    public String getId(){
        return id;
    }
    
    public void insertIOTime(int n){
    	IOTimeList.add(n);
        Collections.sort(IOTimeList);
    }
    
    public boolean IORepetition(){
        for(int i = 0; i < IOTimeList.size()-1; i++){
            if(IOTimeList.get(i) == IOTimeList.get(i+1)) return true;
        }
        return false;
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
    
    public void fillSlice(int n){
        sliceTime = n;
        setRR();
    }
    
    public void reduceSlice(){
        sliceTime--;
    }
    
    public int getSlice(){
        return sliceTime;
    }
    
    private void setRR(){
        roundRobin = false;
    }
    
    public void desetRR(){
        roundRobin = true;
    }
    
    public boolean getRR(){
        return roundRobin;
    }
}
