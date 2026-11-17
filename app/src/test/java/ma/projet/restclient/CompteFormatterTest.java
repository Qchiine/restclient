package ma.projet.restclient;

import org.junit.Test;

import ma.projet.restclient.util.CompteFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompteFormatterTest {

    private static String normaliserEspaces(String valeur) {
        return valeur.replace('\u202F', ' ').replace('\u00A0', ' ');
    }

    @Test
    public void formateLeSoldeEnDeviseFrancaise() {
        assertEquals("2 450,75 €", normaliserEspaces(CompteFormatter.formatSolde(2450.75)));
    }

    @Test
    public void formateUneDateIsoEnDateFrancaise() {
        assertEquals("14/02/2024", CompteFormatter.formatDate("2024-02-14"));
    }

    @Test
    public void renvoieUneDateInconnueSiValeurAbsente() {
        assertEquals("Date inconnue", CompteFormatter.formatDate(null));
    }

    @Test
    public void traduitLesTypesDeCompte() {
        assertEquals("Courant", CompteFormatter.formatTypeLabel("COURANT"));
        assertEquals("Épargne", CompteFormatter.formatTypeLabel("EPARGNE"));
    }

    @Test
    public void identifieLeTypeEpargneSansSensibiliteALaCasse() {
        assertTrue(CompteFormatter.isEpargne("epargne"));
        assertFalse(CompteFormatter.isEpargne("COURANT"));
    }
}
