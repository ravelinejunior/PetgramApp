package br.com.petgramapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.model.FotoPostada;

public class AdapterMinhasFotos extends RecyclerView.Adapter<AdapterMinhasFotos.ViewHolder> {

    private Context contexto;
    private List<FotoPostada> fotoPostadaList;

    public AdapterMinhasFotos(Context c, List<FotoPostada> fotoPostadaList) {
        this.contexto = c;
        this.fotoPostadaList = fotoPostadaList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(contexto).inflate(R.layout.adapter_itens_foto,parent,false);
      return new AdapterMinhasFotos.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    FotoPostada fotoPostada = fotoPostadaList.get(position);

        Uri imagemUri = Uri.parse(fotoPostada.getImagemPostada());
        Picasso.get().load(imagemUri).placeholder(R.drawable.ic_coco_pet).into(holder.imagens_itensFoto);
    }

    @Override
    public int getItemCount() {
        return fotoPostadaList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imagens_itensFoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagens_itensFoto = itemView.findViewById(R.id.imagensPostadas_adapterFotos);
        }
    }
}
