package com.learning.macys;

import android.app.Fragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.support.v7.widget.Toolbar;

import com.learning.macys.adapter.FileListAdapter;
import com.learning.macys.data.model.FileModel;
import com.learning.macys.databinding.FileListBinding;
import com.learning.macys.viewmodel.FileViewModel;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.Single;


/**
 * Created by sonal on 3/2/18.
 */

public class MainFragment extends Fragment {

    private Button scanButton;
    private Button cancelButton;
    private FileListAdapter adapter;
    private RecyclerView mRecyclerView;
    private List<FileModel> data;
    private FileListBinding binding;
    private FileViewModel viewModel;
    private Single<List<FileModel>> observer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    public void createNotification() {

        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("123", "123", NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(mChannel);
        }


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity(), "123")
                .setContentTitle("Scan notification")
                .setContentText("File Scanning in Progress...")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_HIGH);


        mNotificationManager.notify(10, mBuilder.build());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        if (binding == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_layout, parent, false);

            scanButton = binding.scanBtn.findViewById(R.id.scan_btn);
            cancelButton = binding.cancelScanBtn.findViewById(R.id.cancel_scan_btn);
            mRecyclerView = binding.recyclerView.findViewById(R.id.recyclerView);
            Toolbar toolbar = binding.toolbar.findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.app_name);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (viewModel == null) {
            adapter = new FileListAdapter(getActivity());
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

            mRecyclerView.setLayoutManager(linearLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(adapter);
            viewModel = new FileViewModel();
            binding.setViewmodel(viewModel);

        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.setStopScanning(true);

            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNotification();
                viewModel.setScanVisible(false);

                        Observable.fromCallable(new Callable<List<FileModel>>() {

                            @Override
                            public List<FileModel> call() throws Exception {
                                return viewModel.getDisplayableData();

                            }
                        })
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(getSingleObserver());

            }
        });

    }
    private Observer<List<FileModel>> getSingleObserver() {

        return new Observer<List<FileModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
                viewModel.setLoading(true);

            }

            @Override
            public void onNext(List<FileModel> fileModels) {
                adapter.setData(fileModels);
                viewModel.setLoading(false);
                viewModel.setScanVisible(true);
                viewModel.setStopScanning(false);
                viewModel.setFinalDislayList(fileModels);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        MenuItem item = menu.findItem(R.id.menu_share);

        if (data != null && data.size() > 0) {
            item.setEnabled(true);
        } else {
            // disabled
            item.setEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_share:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}



