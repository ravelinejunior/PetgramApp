package br.com.petgramapp.utils;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

import br.com.petgramapp.model.FotoPostada;

public class MyDiff extends DiffUtil.Callback {
    private List<FotoPostada> oldList;
    private List<FotoPostada> newList;

    public MyDiff(List<FotoPostada> oldList, List<FotoPostada> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldItemPosition == newItemPosition;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition) == newList.get(newItemPosition);
    }
}
