package http.server;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Objet simple representant une reponse HTTP pouvant ensuite etre envoyee sur un OutputStream.
 *
 * @author Guillaume Berthomet
 * @author Lola Cremer
 */
public class Response {

    /**
     * Contenu textuel representant le header de la reponse.
     */
    private final String header;
    /**
     * Tableau de byte representant le contenu de la reponse (et non un string car certaines reponses ne renvoient pas
     * du texte).
     */
    private byte[] content;

    /**
     * Constructeur d'une reponse prenant son header et son contenu en parametre
     * @param header Header de la reponse (String, contenant les retours a la ligne bien places)
     * @param content Contenu de la reponse (tableau d'octet, pouvant representer n'importe quelle donnee)
     */
    public Response(String header, byte[] content) {
        this.header = header;
        this.content = content;
    }

    /**
     * Remplace le contenu de cette requete par le contenu passe en parametre.
     * @param content Nouveau contenu pour la requete.
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * Ecrit - de façon arrangee pour etre correctement interprete - la reponse sur le stream passe en parametre. <br/>
     * (Correctement arrange : header, puis ligne vide, puis contenu).
     * @param out Le stream sur lequel ecrire cette reponse.
     * @throws IOException S'il y a eu un probleme lors de l'ecriture.
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
