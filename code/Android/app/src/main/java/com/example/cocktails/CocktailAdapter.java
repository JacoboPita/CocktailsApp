package com.example.cocktails;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import java.util.List;

/**
 *
 */
public class CocktailAdapter extends RecyclerView.Adapter<CocktailAdapter.ViewHolder> {
    private final List<Cocktail> cocktailList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Cocktail cocktail);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CocktailAdapter(List<Cocktail> cocktailList) {
        this.cocktailList = cocktailList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cocktail_user, parent, false); //item_cocktail_user
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cocktail cocktail = cocktailList.get(position);
        holder.cocktailName.setText(cocktail.getName());

        // Cargar imagen desde URL o Base64, dependiendo de cual sea la api
        String imageUrl = cocktail.getImageUrl();
        if (imageUrl != null && (imageUrl.startsWith("http") || imageUrl.startsWith("https"))) {
            // Si es una URL, cargar con Picasso
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.icon_cocktail_color_fino)
                    .error(R.drawable.error)
                    .into(holder.cocktailImage);
        } else {
            // Si es una imagen en Base64, hay que decodificarla
            try {
                if (imageUrl != null && imageUrl.contains(",")) {
                    imageUrl = imageUrl.split(",")[1];
                }
                byte[] decodedString = Base64.decode(imageUrl, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.cocktailImage.setImageBitmap(decodedBitmap);
            } catch (Exception e) {
                Log.e("Base64_Decode", "Error al decodificar imagen: " + e.getMessage());
            }
        }

        // Configurar clic en el item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(cocktail);
            } else {
                // Si no hay listener, abrir detalles
                Intent intent = new Intent(holder.itemView.getContext(), SeeCocktailActivity.class);
                intent.putExtra("nombre", cocktail.getName());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cocktailList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cocktailName;
        ImageView cocktailImage;

        public ViewHolder(View itemView) {
            super(itemView);
            cocktailName = itemView.findViewById(R.id.cocktailName);
            cocktailImage = itemView.findViewById(R.id.cocktailImage);
        }
    }
}
