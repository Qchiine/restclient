package ma.projet.restclient.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class CompteFormatter {

    private static final String TYPE_COURANT = "COURANT";
    private static final String TYPE_EPARGNE = "EPARGNE";

    private CompteFormatter() {
    }

    public static String formatSolde(double solde) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        return formatter.format(solde);
    }

    public static String formatTypeLabel(String type) {
        if (TYPE_EPARGNE.equalsIgnoreCase(type)) {
            return "Épargne";
        }
        if (TYPE_COURANT.equalsIgnoreCase(type)) {
            return "Courant";
        }
        return type == null ? "Inconnu" : type;
    }

    public static String formatDate(String isoDate) {
        if (isoDate == null || isoDate.length() < 10) {
            return isoDate == null ? "Date inconnue" : isoDate;
        }
        return isoDate.substring(8, 10) + "/" + isoDate.substring(5, 7) + "/" + isoDate.substring(0, 4);
    }

    public static boolean isEpargne(String type) {
        return TYPE_EPARGNE.equalsIgnoreCase(type);
    }
}
