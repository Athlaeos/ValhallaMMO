package me.athlaeos.valhallammo.dom;

public interface Fetcher <T, E> {
    T get(E obj);
}
