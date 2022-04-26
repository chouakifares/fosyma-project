package eu.su.mas.dedaleEtu.mas.knowledge;

import eu.su.mas.dedale.env.Observation;

import java.io.Serializable;
import java.sql.Timestamp;

public class Treasure implements Serializable , Comparable{

    private final String position;
    private final Observation type;
    private Integer value;
    private long obsTime = 0;



    public Treasure(String position, Observation type, Integer value, long t){
        this.position = position;
        this.type = type;
        this.value = value;
        this.obsTime = t;
    }

    public long getObsTime() {
        return obsTime;
    }

    public String getPosition() {
        return position;
    }

    public Integer getQuantity() {
        return value;
    }

    public Observation getType(){
        return type;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == this) return true;
        if (! (obj instanceof Treasure)) return false;
        Treasure other = (Treasure) obj;
        return ((Treasure) obj).getPosition().equals(this.position);

    }

    @Override
    public int compareTo(Object o) {
        return ((Treasure)o).getQuantity().compareTo(this.getQuantity());
    }
}
