package com.learning.macys.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.learning.macys.BR;
import com.learning.macys.R;
import com.learning.macys.data.model.FileModel;
import com.learning.macys.databinding.ExtensionBinding;
import com.learning.macys.databinding.FileBinding;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by sonal on 3/2/18.
 */

public class FileListAdapter extends RecyclerView.Adapter {
    private List<FileModel> dataSet = new ArrayList<>();

    public FileListAdapter() {
    }

    public void setData(List<FileModel> names) {
        dataSet.clear();
        dataSet.addAll(names);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {

        switch (dataSet.get(position).getViewType()) {
            case 0:
                return FileModel.HEADER;
            case 1:
                return FileModel.DATA;
            case 2:
                return FileModel.AVG_SIZE;
            case 3:
                return FileModel.FREQ_EXT;

            default:
                return -1;
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView txtHeaderName;

        HeaderViewHolder(View itemView) {
            super(itemView);

            this.txtHeaderName = itemView.findViewById(R.id.header);
        }


    }

    public static class FileDataViewHolder extends RecyclerView.ViewHolder {

        private FileBinding binding;

        FileDataViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
        }

        FileBinding getBinding() {
            return binding;
        }
    }

    public static class ExtDataViewHolder extends RecyclerView.ViewHolder {

        private ExtensionBinding binding;

        ExtDataViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
        }

        ExtensionBinding getBinding() {
            return binding;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case FileModel.HEADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_layout, parent, false);
                return new HeaderViewHolder(view);
            case FileModel.DATA:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_layout, parent, false);
                return new FileDataViewHolder(view);
            case FileModel.AVG_SIZE:
            case FileModel.FREQ_EXT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_ext_layout, parent, false);
                return new ExtDataViewHolder(view);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FileModel object = dataSet.get(position);
        if (object != null) {
            switch (object.getViewType()) {
                case FileModel.HEADER:
                    ((HeaderViewHolder) holder).txtHeaderName.setText(object.getFilename());

                    break;
                case FileModel.DATA:
                    ((FileDataViewHolder) holder).getBinding().setVariable(BR.filemodel, object);
                    ((FileDataViewHolder) holder).getBinding().executePendingBindings();
                    break;
                case FileModel.FREQ_EXT:
                case FileModel.AVG_SIZE:
                    ((ExtDataViewHolder) holder).getBinding().setVariable(BR.filemodel, object);
                    ((ExtDataViewHolder) holder).getBinding().executePendingBindings();
                    break;

            }

        }

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
