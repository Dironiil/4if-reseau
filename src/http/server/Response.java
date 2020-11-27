package http.server;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Objet simple représentant une réponse HTTP pouvant ensuite être envoyée sur un OutputStream.
 *
 * @author Guillaume Berthomet
 * @author Lola Cremer
 */
public class Response {

    /**
     * Contenu textuel représentant le header de la réponse.
     */
    private final String header;
    /**
     * Tableau de byte représentant le contenu de la réponse (et non un string car certaines réponses ne renvoient pas
     * du texte).
     */
    private byte[] content;

    /**
     * Constructeur d'une réponse prenant son header et son contenu en paramètre
     * @param header Header de la réponse (String, contenant les retours à la ligne bien placés)
     * @param content Contenu de la réponse (tableau d'octet, pouvant représenter n'importe quelle donnée)
     */
    public Response(String header, byte[] content) {
        this.header = header;
        this.content = content;
    }

    /**
     * Remplace le contenu de cette requête par le contenu passé en paramètre.
     * @param content Nouveau contenu pour la requête.
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * Ecrit - de façon arrangée pour être correctement interprété - la réponse sur le stream passé en paramètre. <br/>
     * (Correctement arrangé : header, puis ligne vide, puis contenu).
     * @param out Le stream sur lequel écrire cette réponse.
     * @throws IOException S'il y a eu un problème lors de l'écriture.
     */
    public void printOnStream(OutputStream out) throws IOException {
        out.write(header.getBytes());
        out.write('\n');
        if (content.length != 0) {
            out.write('\n');
            out.write(content);
        }
        out.flush();
    }
}
