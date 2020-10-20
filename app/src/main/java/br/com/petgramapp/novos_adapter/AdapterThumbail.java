package br.com.petgramapp.novos_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.zomato.photofilters.utils.ThumbnailItem;

import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.interfaces.FiltersListFragmentListener;


public class AdapterThumbail extends RecyclerView.Adapter<AdapterThumbail.MyViewHolder> {

    Context context;
    List<ThumbnailItem> thumbnailItems;
    FiltersListFragmentListener listener;
    private int selectedIndex = 0;

    public AdapterThumbail(Context context, List<ThumbnailItem> thumbnailItems, FiltersListFragmentListener listener) {
        this.context = context;
        this.thumbnailItems = thumbnailItems;
        this.listener = listener;
    }

    public AdapterThumbail(Context context, List<ThumbnailItem> thumbnailItems) {
        this.context = context;
        this.thumbnailItems = thumbnailItems;
    }

    public AdapterThumbail(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.thumbnail_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        final ThumbnailItem thumbnailItem = thumbnailItems.get(position);

        holder.imageThumbnail.setImageBitmap(thumbnailItem.image);
        //selecionando o thumbnail
        holder.imageThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // listener.onFilterSelected(thumbnailItem.filter);
                selectedIndex = position;
                notifyDataSetChanged();
            }
        });

        holder.filterNameThumbnail.setText(thumbnailItem.filterName);

        if (selectedIndex == position) {
            holder.filterNameThumbnail.setTextColor(ContextCompat.getColor(context, R.color.selected_filter));
        } else {
            holder.filterNameThumbnail.setTextColor(ContextCompat.getColor(context, R.color.normal_filter));
        }
    }

    @Override
    public int getItemCount() {
        return thumbnailItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageThumbnail;
        public TextView filterNameThumbnail;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageThumbnail = itemView.findViewById(R.id.thumbnail_id);
            filterNameThumbnail = itemView.findViewById(R.id.filter_name_id);
        }
    }
}
