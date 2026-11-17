package ma.projet.restclient.cache;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ma.projet.restclient.entities.Compte;

/**
 * Snapshot local des comptes pour rester consultable sans reseau.
 * Conserve aussi la derniere mesure de performance par format.
 */
public class CompteCache {

    private static final String PREFS = "restclient.cache";
    private static final String KEY_COMPTES = "comptes";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_LATENCY_PREFIX = "latency.";
    private static final String KEY_WEIGHT_PREFIX = "weight.";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public CompteCache(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void save(List<Compte> comptes) {
        prefs.edit()
                .putString(KEY_COMPTES, gson.toJson(comptes))
                .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    public List<Compte> load() {
        String raw = prefs.getString(KEY_COMPTES, null);
        if (raw == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Compte>>() {
        }.getType();
        List<Compte> comptes = gson.fromJson(raw, type);
        return comptes == null ? new ArrayList<>() : comptes;
    }

    public long getTimestamp() {
        return prefs.getLong(KEY_TIMESTAMP, 0L);
    }

    public void recordMetrics(String format, long latencyMs, int weightBytes) {
        prefs.edit()
                .putLong(KEY_LATENCY_PREFIX + format, latencyMs)
                .putInt(KEY_WEIGHT_PREFIX + format, weightBytes)
                .apply();
    }

    public long getLatency(String format) {
        return prefs.getLong(KEY_LATENCY_PREFIX + format, -1L);
    }

    public int getWeight(String format) {
        return prefs.getInt(KEY_WEIGHT_PREFIX + format, -1);
    }
}
