package eu.su.mas.dedaleEtu.mas.knowledge;

import eu.su.mas.dedale.env.Observation;

import java.io.Serializable;

public class Treasure implements Serializable {

    private final String position;
    private final Observation type;
    private Integer value;

    public Treasure(String position, Observation type, Integer value){
        this.position = position;
        this.type = type;
        this.value = value;
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
}
