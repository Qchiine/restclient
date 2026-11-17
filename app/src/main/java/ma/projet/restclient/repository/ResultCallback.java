package ma.projet.restclient.repository;

import java.util.List;

/**
 * Contrat unique de retour asynchrone : succes avec donnees ou echec explicite.
 * Evite les callbacks silencieux qui masquent une reponse XML invalide.
 */
public interface ResultCallback<T> {

    void onSuccess(T data);

    void onError(String message, Throwable cause);
}
