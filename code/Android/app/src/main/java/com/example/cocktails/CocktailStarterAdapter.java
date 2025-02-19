package com.example.cocktails;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import com.squareup.picasso.Picasso;


/**
 * Allows you to display the list of random cocktails in the recycler view
 *
 * This class is set up so that cells can be clicked (with onItemClickListener Interfaz)
 *
 * Now, i use CocktailAdapter (before i used this class with InicioFragment, for upload random cocktails from public api
 */

/**
public class CocktailStarterAdapter extends RecyclerView.Adapter<CocktailStarterAdapter.ViewHolder> {
    // Stores the list of CocktailStarter objects to be displayed in the RecyclerView
    private ArrayList<CocktailStarter> cocktailList;
    // variable for the listener instance. Is necessary for the click on the recycler view cells:
    private OnItemClickListener mListener;

    // Constructor:
    public CocktailStarterAdapter(ArrayList<CocktailStarter> cocktailList) {
        this.cocktailList = cocktailList;
    }

    // Interfaz for the clics:
    public interface OnItemClickListener {
        void onItemClick(CocktailStarter cocktail);
    }

    // Method for assign the listener:
    public void setOnItemClickListener(OnItemClickListener mListener) {
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cocktail_starter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CocktailStarter cocktail = cocktailList.get(position);
        holder.tvName.setText(cocktail.getName());

        // Picasso to upload the cocktail image
        Picasso.get()
                .load(cocktail.getImageUrl())
                // Temporary image: while the images is not being loaded from the public API
                .placeholder(R.drawable.icon_cocktail_color_fino)
                // Error Image: Public API image not loading
                .error(R.drawable.error)
                .into(holder.ivImage);

        // Setting up cell click (Configurar el clic en la celda):
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(cocktail);  // Notifies the listener when an item is clicked
            }
        });
    }

    @Override
    public int getItemCount() {
        return cocktailList.size();
    }

    // Internal static class to hold the views that are going to be recycled
    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.CocktailNameTV);
            ivImage = itemView.findViewById(R.id.CocktailImageIV);
        }
    }
}*/

