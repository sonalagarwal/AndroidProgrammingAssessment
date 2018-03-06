package com.learning.macys;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
import com.learning.macys.service.BackgroundIntentService;

import java.util.List;

import io.reactivex.annotations.Nullable;


/**
 * Created by sonal on 3/2/18.
 */

public class MainFragment extends Fragment{

    private Button scanButton;
    private FileListAdapter adapter;
    private ProgressDialog myDialog;
    private RecyclerView mRecyclerView;
    private MyBroadcastReceiver myBroadcastReceiver;
    private MyBroadcastReceiver_Update myBroadcastReceiver_Update;
    private MyBroadcastReceiver_Size myBroadcastReceiver_Size;
    private Intent intentMyIntentService;
    private List<FileModel> data;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // If we are returning here from a screen orientation
        // and the service is still working, re-create and display the
        // progress dialog.
        if (isMyServiceRunning()) {
            displayProgress();
        }
    }
    public void createNotification(){

        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("123", "123", NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(mChannel);
        }


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity(),"123")
                .setContentTitle("Scan notification")
                .setContentText("File Scanning in Progress...")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_HIGH);


        mNotificationManager.notify(10, mBuilder.build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        View root = inflater.inflate(R.layout.fragment_layout, parent, false);

        scanButton = root.findViewById(R.id.scan_btn);
        mRecyclerView = root.findViewById(R.id.recyclerView);
        Toolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        return root;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new FileListAdapter(getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(adapter);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                displayProgress();

                //Start MyIntentService
                intentMyIntentService = new Intent(getActivity(), BackgroundIntentService.class);
                getActivity().startService(intentMyIntentService);
                createNotification();

                myBroadcastReceiver = new MyBroadcastReceiver();
                myBroadcastReceiver_Update = new MyBroadcastReceiver_Update();
                myBroadcastReceiver_Size = new MyBroadcastReceiver_Size();

                //register BroadcastReceiver
                IntentFilter intentFilter = new IntentFilter(BackgroundIntentService.ACTION_MyIntentService);
                intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
                getActivity().registerReceiver(myBroadcastReceiver, intentFilter);

                IntentFilter intentFilter_update = new IntentFilter(BackgroundIntentService.ACTION_MyUpdate);
                intentFilter_update.addCategory(Intent.CATEGORY_DEFAULT);
                getActivity().registerReceiver(myBroadcastReceiver_Update, intentFilter_update);

                IntentFilter intentFilter_size = new IntentFilter(BackgroundIntentService.ACTION_Size);
                intentFilter_update.addCategory(Intent.CATEGORY_DEFAULT);
                getActivity().registerReceiver(myBroadcastReceiver_Size, intentFilter_size);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(myBroadcastReceiver);
        getActivity().unregisterReceiver(myBroadcastReceiver_Update);
        getActivity().unregisterReceiver(myBroadcastReceiver_Size);
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.learning.macys.service.BackgroundIntentService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private void displayProgress() {
        myDialog = new ProgressDialog(getActivity(), 0);
        myDialog.setTitle("Scannning Files...");
        myDialog.setMessage("Please wait!");
        myDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        myDialog.setProgress(0);
        myDialog.setCancelable(true);
        myDialog.setCanceledOnTouchOutside(false);
        myDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Stop Sync", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //STOP SERVICE
                Intent sIntent = new Intent();
                sIntent.setAction(BackgroundIntentService.StopReceiver.ACTION_STOP);
                getActivity().sendBroadcast(sIntent);
                dialog.dismiss();
            }
        });
        myDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            public void onCancel(DialogInterface arg0) {
                //STOP SERVICE
                Intent sIntent = new Intent();
                sIntent.setAction(BackgroundIntentService.StopReceiver.ACTION_STOP);
                getActivity().sendBroadcast(sIntent);
                myDialog.dismiss();
            }
        });
        myDialog.show();


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


    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            data = intent.getParcelableArrayListExtra(BackgroundIntentService.ACTION_DATA);
            adapter.setData(data);
            myDialog.dismiss();
        }
    }

    public class MyBroadcastReceiver_Update extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            int update = intent.getIntExtra(BackgroundIntentService.EXTRA_KEY_UPDATE, 0);
            myDialog.setProgress(update);
        }
    }

    public class MyBroadcastReceiver_Size extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int max = intent.getIntExtra(BackgroundIntentService.ACTION_FILE_SIZE, 0);
            myDialog.setMax(max);

        }
    }

}



