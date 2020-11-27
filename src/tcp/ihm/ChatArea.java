package tcp.ihm;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

/**
 * Classe purement IHM, elle implemente une zone de texte dans laquelle un chat utilisant une representation HTML peut
 * etre correctement represente et visualise.
 *
 * @author Guillaume Berthomet
 * @author Lola Cremer
 */
public class ChatArea extends JEditorPane {

    private static final String TEXT_COLOR_HEX = "#E0E0E0";
    private static final String ERROR_COLOR_HEX = "#F84545";

    private String content;

    /**
     * Construit une zone de chat textuelle HTML.
     */
    public ChatArea() {
        this.setEditable(false);
        this.setContentType("text/html");
        this.content = "";

        DefaultCaret caret = (DefaultCaret) this.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    /**
     * Ajoute une ligne a la zone de chat.
     * @param line La ligne a ajouter.
     */
    public void addLine(String line) {
        content += "<p style='color: " + TEXT_COLOR_HEX + "; padding: 0; margin: 0 3px; word-wrap: break-word;'>" +
                line +
                "</p>\n";
        this.setText(content);
    }

    /**
     * Ajoute une ligne d'erreur a la zone de chat.
     * @param error Le contenu de l'erreur a ajouter.
     */
    public void addErrorLine(String error) {
        addLine("<span style='color: " + ERROR_COLOR_HEX + "'>" + error + "</span>");
    }

}
