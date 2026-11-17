package ma.projet.restclient.entities;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import javax.xml.bind.annotation.XmlElement;

@Root(name = "item", strict = false)
public class Compte {

    @Element(name = "id", required = false)
    private Long id;

    @Element(name = "solde")
    private double solde;

    @Element(name = "type")
    private String type;

    @Element(name = "dateCreation")
    private String dateCreation;

    public Compte() {
    }

    public Compte(Long id, double solde, String type, String dateCreation) {
        this.id = id;
        this.solde = solde;
        this.type = type;
        this.dateCreation = dateCreation;
    }

    @XmlElement(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @XmlElement(name = "solde")
    public double getSolde() {
        return solde;
    }

    public void setSolde(double solde) {
        this.solde = solde;
    }

    @XmlElement(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElement(name = "dateCreation")
    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return "Compte{id=" + id + ", solde=" + solde + ", type='" + type + "', dateCreation='" + dateCreation + "'}";
    }
}
