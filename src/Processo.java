import java.util.ArrayList;

/**
 *
 * @author Gabriel Franzoni 15105090
 */
public class Processo {

    private String id;
    private int arrivalTime, executionTime, priority, refresh;
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
}
