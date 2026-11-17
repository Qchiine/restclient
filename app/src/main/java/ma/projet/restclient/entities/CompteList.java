package ma.projet.restclient.entities;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(name = "List", strict = false)
public class CompteList {

    @ElementList(inline = true, entry = "item", required = false)
    private List<Compte> comptes = new ArrayList<>();

    public List<Compte> getComptes() {
        return comptes;
    }

    public void setComptes(List<Compte> comptes) {
        this.comptes = comptes;
    }
}
