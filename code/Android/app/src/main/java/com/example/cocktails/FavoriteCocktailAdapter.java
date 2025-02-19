package com.example.cocktails;

import android.content.Context;
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
import com.squareup.picasso.Picasso;
import java.util.List;

public class FavoriteCocktailAdapter extends RecyclerView.Adapter<FavoriteCocktailAdapter.ViewHolder> {

    private List<FavoriteCocktail> favoriteCocktailList;
    private Context context;
    private OnItemClickListener listener;

    // Interfaz para manejar el clic en los ítems
    public interface OnItemClickListener {
        void onItemClick(FavoriteCocktail cocktail);
    }

    // Método para establecer el listener desde el fragmento
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public FavoriteCocktailAdapter(Context context, List<FavoriteCocktail> favoriteCocktailList) {
        this.context = context;
        this.favoriteCocktailList = favoriteCocktailList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_cocktail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteCocktail cocktail = favoriteCocktailList.get(position);
        holder.textViewName.setText(cocktail.getName());

//        String imageBase64 = cocktail.getImageUrl();
//
//        Log.d("ADAPTER_DATA", "Procesando: " + cocktail.getName());
//        Log.d("ADAPTER_DATA", "Longitud Base64: " + imageBase64.length());
//
//        try {
//            byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
//            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//
//            if (decodedBitmap != null) {
//                Log.d("ADAPTER_DATA", "Imagen decodificada correctamente");
//                holder.imageViewCocktail.setImageBitmap(decodedBitmap);
//            } else {
//                Log.e("ADAPTER_ERROR", "Bitmap nulo después de decodificar");
//            }
//
//        } catch (IllegalArgumentException e) {
//            Log.e("ADAPTER_ERROR", "Error al decodificar Base64: " + e.getMessage());
//        }
        // Obtener la URL o Base64 de la imagen
        String imageUrl = cocktail.getImageUrl();

        if (imageUrl != null && (imageUrl.startsWith("http") || imageUrl.startsWith("https"))) {
            // Si es una URL, cargar con Picasso
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.icon_cocktail_color_fino)
                    .error(R.drawable.error)
                    .into(holder.imageViewCocktail);
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            // Si es una imagen en Base64, hay que decodificarla
            try {
                // Si la cadena Base64 contiene un encabezado de tipo MIME, se elimina (en caso de ser Base64 de una imagen en línea)
                if (imageUrl.contains(",")) {
                    imageUrl = imageUrl.split(",")[1];
                }

                // Decodificar la cadena Base64 a bytes
                byte[] decodedString = Base64.decode(imageUrl, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if (decodedBitmap != null) {
                    holder.imageViewCocktail.setImageBitmap(decodedBitmap);
                } else {
                    Log.e("ADAPTER_ERROR", "Imagen decodificada como nula");
                }

            } catch (IllegalArgumentException e) {
                // Error en la decodificación de Base64
                Log.e("Base64_Decode", "Error al decodificar imagen: " + e.getMessage());
            } catch (Exception e) {
                // Manejar cualquier otro error
                Log.e("Base64_Decode", "Error inesperado al decodificar imagen: " + e.getMessage());
            }
        } else {
            Log.e("ADAPTER_ERROR", "La URL o Base64 de la imagen es inválida");
        }

        holder.itemView.setOnClickListener(location -> {
            Intent intent = new Intent(context, SeeCocktailActivity.class);
            intent.putExtra("nombre", holder.textViewName.getText().toString());
            context.startActivity(intent);
        });
    }



    @Override
    public int getItemCount() {
        return favoriteCocktailList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        ImageView imageViewCocktail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            imageViewCocktail = itemView.findViewById(R.id.imageViewCocktail);
        }
    }
}


