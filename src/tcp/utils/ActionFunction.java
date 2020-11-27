package tcp.utils;

/**
 * Interface functionnelle représentant une action ne prenant pas de paramètre ni ne renvoyant de valeur.
 */
@FunctionalInterface
public interface ActionFunction {
    /**
     * L'action que la fonction doit effectuer.
     */
    void act();
}
