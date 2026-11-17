package ma.projet.restclient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ma.projet.restclient.adapter.CompteAdapter;
import ma.projet.restclient.cache.CompteCache;
import ma.projet.restclient.config.NetworkMetrics;
import ma.projet.restclient.config.RetrofitClient;
import ma.projet.restclient.entities.Compte;
import ma.projet.restclient.repository.CompteRepository;
import ma.projet.restclient.repository.ResultCallback;
import ma.projet.restclient.util.CompteFormatter;

public class MainActivity extends AppCompatActivity
        implements CompteAdapter.OnDeleteClickListener, CompteAdapter.OnUpdateClickListener {

    private RecyclerView recyclerView;
    private CompteAdapter adapter;
    private RadioGroup formatGroup;
    private ExtendedFloatingActionButton addButton;
    private View emptyState;
    private View progressBar;
    private TextView tvSyncStatus;
    private TextView tvSummary;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;

    private CompteCache cache;
    private String currentFormat = RetrofitClient.FORMAT_JSON;
    private final List<Compte> comptesAffiches = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cache = new CompteCache(this);
        initViews();
        setupRecyclerView();
        setupFormatSelection();
        setupAddButton();
        setupRefresh();

        loadData(currentFormat, false);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        formatGroup = findViewById(R.id.formatGroup);
        addButton = findViewById(R.id.fabAdd);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);
        tvSyncStatus = findViewById(R.id.tvSyncStatus);
        tvSummary = findViewById(R.id.tvSummary);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        findViewById(R.id.btnRefresh).setOnClickListener(v -> loadData(currentFormat, true));
        findViewById(R.id.btnCompare).setOnClickListener(v -> showFormatComparison());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CompteAdapter(this, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupFormatSelection() {
        formatGroup.setOnCheckedChangeListener((group, checkedId) -> {
            currentFormat = checkedId == R.id.radioXml
                    ? RetrofitClient.FORMAT_XML
                    : RetrofitClient.FORMAT_JSON;
            loadData(currentFormat, true);
        });
    }

    private void setupAddButton() {
        addButton.setOnClickListener(v -> showCompteDialog(null));
    }

    private void setupRefresh() {
        swipeRefresh.setOnRefreshListener(() -> loadData(currentFormat, true));
    }

    private void loadData(String format, boolean showProgress) {
        if (showProgress) {
            setBusy(true);
        }
        new CompteRepository(format).getAllCompte(new ResultCallback<List<Compte>>() {
            @Override
            public void onSuccess(List<Compte> data) {
                cache.save(data);
                renderComptes(data);
                setBusy(false);
                showStatus("Chargé en " + format);
            }

            @Override
            public void onError(String message, Throwable cause) {
                setBusy(false);
                renderComptes(cache.load());
                showStatus(message);
                showToast(message);
            }
        });
    }

    private void renderComptes(List<Compte> comptes) {
        comptesAffiches.clear();
        if (comptes != null) {
            comptesAffiches.addAll(comptes);
        }
        adapter.updateData(comptes);
        boolean vide = comptesAffiches.isEmpty();
        emptyState.setVisibility(vide ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(vide ? View.GONE : View.VISIBLE);
        tvSummary.setText(comptesAffiches.size() + " compte(s) · " + currentFormat);
        updateMetricsLine();
    }

    private void updateMetricsLine() {
        NetworkMetrics.Sample sample = NetworkMetrics.lastFor(currentFormat);
        if (sample == null) {
            return;
        }
        cache.recordMetrics(currentFormat, sample.latencyMs, (int) sample.bytes);
    }

    private void setBusy(boolean busy) {
        progressBar.setVisibility(busy ? View.VISIBLE : View.GONE);
        swipeRefresh.setRefreshing(false);
        addButton.setEnabled(!busy);
    }

    private void showStatus(String message) {
        tvSyncStatus.setText(message);
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show());
    }

    private void showCompteDialog(Compte existant) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_compte, null);
        EditText etSolde = dialogView.findViewById(R.id.etSolde);
        RadioGroup typeGroup = dialogView.findViewById(R.id.typeGroup);
        TextView tvDateInfo = dialogView.findViewById(R.id.tvDateInfo);

        boolean edition = existant != null;
        if (edition) {
            etSolde.setText(String.valueOf(existant.getSolde()));
            if (CompteFormatter.isEpargne(existant.getType())) {
                typeGroup.check(R.id.radioEpargne);
            } else {
                typeGroup.check(R.id.radioCourant);
            }
            tvDateInfo.setText("Ouverture : " + CompteFormatter.formatDate(existant.getDateCreation()));
        } else {
            tvDateInfo.setText("Ouverture : " + CompteFormatter.formatDate(todayIso()));
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(edition ? R.string.edit_compte : R.string.add_compte)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(edition ? R.string.confirm : R.string.add_compte, null)
                .create();

        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String erreur = validate(etSolde.getText().toString());
            if (erreur != null) {
                etSolde.setError(erreur);
                return;
            }
            double solde = Double.parseDouble(etSolde.getText().toString().replace(',', '.'));
            String type = typeGroup.getCheckedRadioButtonId() == R.id.radioEpargne ? "EPARGNE" : "COURANT";

            if (edition) {
                existant.setSolde(solde);
                existant.setType(type);
                updateCompte(existant, dialog);
            } else {
                addCompte(new Compte(null, solde, type, todayIso()), dialog);
            }
        }));
        dialog.show();
    }

    private String validate(String soldeBrut) {
        String valeur = soldeBrut == null ? "" : soldeBrut.trim().replace(',', '.');
        if (valeur.isEmpty()) {
            return "Le solde est obligatoire.";
        }
        try {
            Double.parseDouble(valeur);
        } catch (NumberFormatException exception) {
            return "Le solde doit être un montant numérique.";
        }
        return null;
    }

    private void addCompte(Compte compte, AlertDialog dialog) {
        setBusy(true);
        String formatEcriture = RetrofitClient.FORMAT_JSON;
        new CompteRepository(formatEcriture).addCompte(compte, new ResultCallback<Compte>() {
            @Override
            public void onSuccess(Compte data) {
                setBusy(false);
                dialog.dismiss();
                showStatus("Compte créé");
                snack("Compte ajouté");
                loadData(currentFormat, false);
            }

            @Override
            public void onError(String message, Throwable cause) {
                setBusy(false);
                snack(message);
            }
        });
    }

    private void updateCompte(Compte compte, AlertDialog dialog) {
        setBusy(true);
        String formatEcriture = RetrofitClient.FORMAT_JSON;
        new CompteRepository(formatEcriture).updateCompte(compte.getId(), compte, new ResultCallback<Compte>() {
            @Override
            public void onSuccess(Compte data) {
                setBusy(false);
                dialog.dismiss();
                showStatus("Compte modifié");
                snack("Compte modifié");
                loadData(currentFormat, false);
            }

            @Override
            public void onError(String message, Throwable cause) {
                setBusy(false);
                snack(message);
            }
        });
    }

    @Override
    public void onUpdateClick(Compte compte) {
        showCompteDialog(compte);
    }

    @Override
    public void onDeleteClick(Compte compte) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(R.string.delete_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteCompte(compte))
                .show();
    }

    private void deleteCompte(Compte compte) {
        setBusy(true);
        String formatEcriture = RetrofitClient.FORMAT_JSON;
        new CompteRepository(formatEcriture).deleteCompte(compte.getId(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                setBusy(false);
                comptesAffiches.removeIf(item -> java.util.Objects.equals(item.getId(), compte.getId()));
                adapter.removeCompte(compte);
                boolean vide = comptesAffiches.isEmpty();
                emptyState.setVisibility(vide ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(vide ? View.GONE : View.VISIBLE);
                tvSummary.setText(comptesAffiches.size() + " compte(s) · " + currentFormat);
                cache.save(comptesAffiches);
                showStatus("Compte supprimé");
                snack("Compte supprimé");
            }

            @Override
            public void onError(String message, Throwable cause) {
                setBusy(false);
                snack(message);
            }
        });
    }

    private void snack(String message) {
        runOnUiThread(() -> Snackbar.make(recyclerView, message, Snackbar.LENGTH_SHORT).show());
    }

    private String todayIso() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE).format(new Date());
    }

    private void showFormatComparison() {
        long jsonLatency = cache.getLatency(RetrofitClient.FORMAT_JSON);
        int jsonWeight = cache.getWeight(RetrofitClient.FORMAT_JSON);
        long xmlLatency = cache.getLatency(RetrofitClient.FORMAT_XML);
        int xmlWeight = cache.getWeight(RetrofitClient.FORMAT_XML);

        String message = "JSON : " + describe(jsonLatency, jsonWeight)
                + "\n\nXML : " + describe(xmlLatency, xmlWeight)
                + "\n\nChargez chaque format au moins une fois pour comparer.";

        new AlertDialog.Builder(this)
                .setTitle("Comparateur JSON / XML")
                .setMessage(message)
                .setPositiveButton(R.string.confirm, null)
                .show();
    }

    private String describe(long latency, int weight) {
        if (latency < 0 || weight <= 0) {
            return "pas encore mesuré";
        }
        return latency + " ms · " + weight + " octets";
    }
}
