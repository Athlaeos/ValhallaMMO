package me.athlaeos.valhallammo.dom;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Pair<T, E>{
    private T one;
    private E two;

    public Pair(T one, E two){
        this.one = one;
        this.two = two;
    }

    public E getTwo() { return two; }
    public T getOne() { return one; }
    public void setOne(T one) { this.one = one; }
    public void setTwo(E two) { this.two = two; }

    public Map<T, E> map(Collection<Pair<T, E>> pairs){
        Map<T, E> map = new HashMap<>();
        map.put(one, two);
        pairs.forEach(p -> map.put(p.getOne(), p.getTwo()));
        return map;
    }
}
