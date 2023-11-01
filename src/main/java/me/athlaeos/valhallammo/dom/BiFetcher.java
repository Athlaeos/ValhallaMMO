package me.athlaeos.valhallammo.dom;

public interface BiFetcher<T, E> {
    T get(E obj);
}
