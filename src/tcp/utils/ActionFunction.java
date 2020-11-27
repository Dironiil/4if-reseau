package tcp.utils;

/**
 * Interface functionnelle representant une action ne prenant pas de parametre ni ne renvoyant de valeur.
 */
@FunctionalInterface
public interface ActionFunction {
    /**
     * L'action que la fonction doit effectuer.
     */
    void act();
}
