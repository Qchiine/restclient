package ma.projet.restclient;

import org.junit.Test;
import org.simpleframework.xml.core.Persister;

import java.io.StringReader;
import java.util.List;

import ma.projet.restclient.entities.Compte;
import ma.projet.restclient.entities.CompteList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CompteXmlParsingTest {

    private static final String XML_PAYLOAD =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<List>"
                    + "<item><id>1</id><solde>2450.75</solde><type>COURANT</type><dateCreation>2024-02-14</dateCreation></item>"
                    + "<item><id>2</id><solde>18900.0</solde><type>EPARGNE</type><dateCreation>2023-11-03</dateCreation></item>"
                    + "</List>";

    @Test
    public void deserialiseUneListeDeComptesXml() throws Exception {
        CompteList liste = new Persister().read(CompteList.class, new StringReader(XML_PAYLOAD));

        List<Compte> comptes = liste.getComptes();
        assertNotNull(comptes);
        assertEquals(2, comptes.size());

        Compte premier = comptes.get(0);
        assertEquals(Long.valueOf(1L), premier.getId());
        assertEquals(2450.75, premier.getSolde(), 0.001);
        assertEquals("COURANT", premier.getType());
        assertEquals("2024-02-14", premier.getDateCreation());
    }

    @Test
    public void conserveLeTypeEpargneEtLeSoldeDecimal() throws Exception {
        CompteList liste = new Persister().read(CompteList.class, new StringReader(XML_PAYLOAD));

        Compte second = liste.getComptes().get(1);
        assertEquals("EPARGNE", second.getType());
        assertEquals(18900.0, second.getSolde(), 0.001);
    }
}
