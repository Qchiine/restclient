package ma.projet.restclient.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ma.projet.restclient.R;
import ma.projet.restclient.entities.Compte;
import ma.projet.restclient.util.CompteFormatter;

public class CompteAdapter extends RecyclerView.Adapter<CompteAdapter.CompteViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(Compte compte);
    }

    public interface OnUpdateClickListener {
        void onUpdateClick(Compte compte);
    }

    private final List<Compte> comptes = new ArrayList<>();
    private final OnDeleteClickListener onDeleteClickListener;
    private final OnUpdateClickListener onUpdateClickListener;

    public CompteAdapter(OnDeleteClickListener onDeleteClickListener,
                         OnUpdateClickListener onUpdateClickListener) {
        this.onDeleteClickListener = onDeleteClickListener;
        this.onUpdateClickListener = onUpdateClickListener;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public CompteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_compte, parent, false);
        return new CompteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompteViewHolder holder, int position) {
        holder.bind(comptes.get(position));
    }

    @Override
    public int getItemCount() {
        return comptes.size();
    }

    @Override
    public long getItemId(int position) {
        Long id = comptes.get(position).getId();
        return id == null ? RecyclerView.NO_ID : id;
    }

    public void updateData(List<Compte> nouveauxComptes) {
        List<Compte> cible = nouveauxComptes == null ? new ArrayList<>() : new ArrayList<>(nouveauxComptes);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new CompteDiff(comptes, cible));
        comptes.clear();
        comptes.addAll(cible);
        diff.dispatchUpdatesTo(this);
    }

    public void removeCompte(Compte compte) {
        int index = indexOf(compte);
        if (index < 0) {
            return;
        }
        comptes.remove(index);
        notifyItemRemoved(index);
    }

    private int indexOf(Compte compte) {
        for (int i = 0; i < comptes.size(); i++) {
            if (Objects.equals(comptes.get(i).getId(), compte.getId())) {
                return i;
            }
        }
        return -1;
    }

    private static final class CompteDiff extends DiffUtil.Callback {

        private final List<Compte> anciens;
        private final List<Compte> nouveaux;

        CompteDiff(List<Compte> anciens, List<Compte> nouveaux) {
            this.anciens = new ArrayList<>(anciens);
            this.nouveaux = nouveaux;
        }

        @Override
        public int getOldListSize() {
            return anciens.size();
        }

        @Override
        public int getNewListSize() {
            return nouveaux.size();
        }

        @Override
        public boolean areItemsTheSame(int oldPosition, int newPosition) {
            return Objects.equals(anciens.get(oldPosition).getId(), nouveaux.get(newPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldPosition, int newPosition) {
            return Objects.equals(anciens.get(oldPosition).toString(), nouveaux.get(newPosition).toString());
        }
    }

    class CompteViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvId;
        private final TextView tvSolde;
        private final TextView tvType;
        private final TextView tvDate;
        private final View btnDelete;
        private final View btnEdit;

        CompteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvSolde = itemView.findViewById(R.id.tvSolde);
            tvType = itemView.findViewById(R.id.tvType);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }

        void bind(Compte compte) {
            tvId.setText(compte.getId() == null ? "ID —" : "ID " + compte.getId());
            tvSolde.setText(CompteFormatter.formatSolde(compte.getSolde()));
            tvType.setText(CompteFormatter.formatTypeLabel(compte.getType()));
            tvDate.setText("Ouvert le " + CompteFormatter.formatDate(compte.getDateCreation()));

            boolean epargne = CompteFormatter.isEpargne(compte.getType());
            tvType.setBackgroundResource(epargne ? R.drawable.bg_tag_epargne : R.drawable.bg_tag_courant);
            tvType.setTextColor(itemView.getContext().getColor(
                    epargne ? R.color.tag_epargne_text : R.color.tag_courant_text));

            btnDelete.setOnClickListener(v -> {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(compte);
                }
            });
            btnEdit.setOnClickListener(v -> {
                if (onUpdateClickListener != null) {
                    onUpdateClickListener.onUpdateClick(compte);
                }
            });
        }
    }
}
