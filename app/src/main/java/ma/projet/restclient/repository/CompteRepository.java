package ma.projet.restclient.repository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import ma.projet.restclient.api.CompteService;
import ma.projet.restclient.config.RetrofitClient;
import ma.projet.restclient.entities.Compte;
import ma.projet.restclient.entities.CompteList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompteRepository {

    private final CompteService compteService;
    private final String format;

    public CompteRepository(String converterType) {
        this.format = converterType;
        this.compteService = RetrofitClient.getClient(converterType).create(CompteService.class);
    }

    public void getAllCompte(ResultCallback<List<Compte>> callback) {
        if (RetrofitClient.FORMAT_XML.equals(format)) {
            compteService.getAllCompteXml().enqueue(new Callback<CompteList>() {
                @Override
                public void onResponse(Call<CompteList> call, Response<CompteList> response) {
                    if (!response.isSuccessful()) {
                        callback.onError(describeHttpError(response.code()), null);
                        return;
                    }
                    CompteList body = response.body();
                    if (body == null) {
                        callback.onError("Réponse XML vide ou illisible.", null);
                        return;
                    }
                    List<Compte> comptes = body.getComptes();
                    callback.onSuccess(comptes == null ? Collections.emptyList() : comptes);
                }

                @Override
                public void onFailure(Call<CompteList> call, Throwable throwable) {
                    callback.onError(describeFailure(throwable), throwable);
                }
            });
            return;
        }

        compteService.getAllCompteJson().enqueue(new Callback<List<Compte>>() {
            @Override
            public void onResponse(Call<List<Compte>> call, Response<List<Compte>> response) {
                if (!response.isSuccessful()) {
                    callback.onError(describeHttpError(response.code()), null);
                    return;
                }
                List<Compte> body = response.body();
                callback.onSuccess(body == null ? Collections.emptyList() : body);
            }

            @Override
            public void onFailure(Call<List<Compte>> call, Throwable throwable) {
                callback.onError(describeFailure(throwable), throwable);
            }
        });
    }

    public void getCompteById(Long id, ResultCallback<Compte> callback) {
        compteService.getCompteById(id).enqueue(new Callback<Compte>() {
            @Override
            public void onResponse(Call<Compte> call, Response<Compte> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(describeHttpError(response.code()), null);
                    return;
                }
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<Compte> call, Throwable throwable) {
                callback.onError(describeFailure(throwable), throwable);
            }
        });
    }

    public void addCompte(Compte compte, ResultCallback<Compte> callback) {
        compteService.addCompte(compte).enqueue(new Callback<Compte>() {
            @Override
            public void onResponse(Call<Compte> call, Response<Compte> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(describeHttpError(response.code()), null);
                    return;
                }
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<Compte> call, Throwable throwable) {
                callback.onError(describeFailure(throwable), throwable);
            }
        });
    }

    public void updateCompte(Long id, Compte compte, ResultCallback<Compte> callback) {
        compteService.updateCompte(id, compte).enqueue(new Callback<Compte>() {
            @Override
            public void onResponse(Call<Compte> call, Response<Compte> response) {
                if (!response.isSuccessful()) {
                    callback.onError(describeHttpError(response.code()), null);
                    return;
                }
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<Compte> call, Throwable throwable) {
                callback.onError(describeFailure(throwable), throwable);
            }
        });
    }

    public void deleteCompte(Long id, ResultCallback<Void> callback) {
        compteService.deleteCompte(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    callback.onError(describeHttpError(response.code()), null);
                    return;
                }
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                callback.onError(describeFailure(throwable), throwable);
            }
        });
    }

    public String getFormat() {
        return format;
    }

    private String describeHttpError(int code) {
        return "Le service REST a répondu " + code + " pour le format " + format + ".";
    }

    private String describeFailure(Throwable throwable) {
        if (throwable instanceof IOException) {
            return "Service REST injoignable en " + format + ". Vérifiez que le serveur écoute sur 10.0.2.2:8082.";
        }
        return "Erreur " + format + " : " + throwable.getMessage();
    }
}
