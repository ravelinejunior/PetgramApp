package br.com.petgramapp.fragments.EditarImage;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.activities.EditarImagem.EditarImagem;
import br.com.petgramapp.interfaces.FiltersListFragmentListener;
import br.com.petgramapp.novos_adapter.AdapterThumbail;
import br.com.petgramapp.utils.BitmapUtils;
import br.com.petgramapp.utils.SpacesItemDecoration;

public class FiltersListFragment extends Fragment implements FiltersListFragmentListener {
    static Bitmap bitmap;
    static FiltersListFragment instance;
    RecyclerView recyclerView;
    List<ThumbnailItem> thumbnailItems;
    AdapterThumbail adapterThumbail;
    FiltersListFragmentListener listener;

    public FiltersListFragment() {
        // Required empty public constructor
    }

    public static FiltersListFragment getInstance(Bitmap bitmapSave) {
        bitmap = bitmapSave;
        if (instance == null) {
            instance = new FiltersListFragment();
        }
        return instance;
    }

    public void setListener(FiltersListFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_filters_list, container, false);

        thumbnailItems = new ArrayList<>();
        adapterThumbail = new AdapterThumbail(getContext(), thumbnailItems, listener);

        recyclerView = view.findViewById(R.id.recyclerView_idFiltersFrag);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));
        recyclerView.setAdapter(adapterThumbail);

        //exibir thumbnail
        displayThumbail(bitmap);

        return view;
    }

    public void displayThumbail(final Bitmap bitmap) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Bitmap thumbImg;
                if (bitmap == null) {
                    thumbImg = BitmapUtils.getBitmapFromAssets(getActivity(), EditarImagem.pictureName, 100, 100);
                } else {
                    thumbImg = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                }

                if (thumbImg == null) return;
                ThumbnailsManager.clearThumbs();
                thumbnailItems.clear();

                //adicionar um bitmap padrao da imagem primeiro
                ThumbnailItem thumbnailItem = new ThumbnailItem();
                thumbnailItem.image = thumbImg;
                thumbnailItem.filterName = "Normal";
                ThumbnailsManager.addThumb(thumbnailItem);

                List<Filter> filters = FilterPack.getFilterPack(getActivity());
                for (Filter filter : filters) {
                    ThumbnailItem thumb1 = new ThumbnailItem();
                    thumb1.image = thumbImg;
                    thumb1.filter = filter;
                    thumb1.filterName = filter.getName();
                    ThumbnailsManager.addThumb(thumb1);
                }

                thumbnailItems.addAll(ThumbnailsManager.processThumbs(getActivity()));

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterThumbail.notifyDataSetChanged();
                    }
                });
            }
        };
        new Thread(r).start();
    }

    @Override
    public void onFilterSelected(Filter filter) {
        if (listener != null) listener.onFilterSelected(filter);
    }
}
