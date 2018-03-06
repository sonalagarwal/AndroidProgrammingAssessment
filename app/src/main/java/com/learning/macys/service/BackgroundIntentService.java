package com.learning.macys.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;

import com.learning.macys.data.model.FileModel;
import com.learning.macys.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by sonal on 3/5/18.
 */

public class BackgroundIntentService extends IntentService {
    public static final String ACTION_MyIntentService = "com.learning.macys.service.RESPONSE";
    public static final String ACTION_MyUpdate = "com.learning.macys.service.UPDATE";
    public static final String ACTION_Size = "com.learning.macys.service.FILE_SIZE";
    private static final int FILE_COUNT = 10;
    private static final int EXT_COUNT = 5;
    public static final String ACTION_DATA = "com.learning.macys.service.DATA";
    public static final String ACTION_FILE_SIZE = "com.learning.macys.service.SIZE";
    public static final String EXTRA_KEY_UPDATE = "EXTRA_UPDATE";
    private ArrayList<FileModel> filesList = new ArrayList<>();
    private HashMap<String, FileModel> gfilemap = new HashMap<>();
    private StopReceiver receiver;
    private boolean stop = false;

    public BackgroundIntentService() {
        super("com.learning.macys.service.BackgroundIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        IntentFilter filter = new IntentFilter(StopReceiver.ACTION_STOP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new StopReceiver();
        registerReceiver(receiver, filter);

        HashMap<String, FileModel> filemap = new HashMap<>();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File f = new File(path);
        String state = Environment.getExternalStorageState();
        if (f.isDirectory() && Environment.MEDIA_MOUNTED.equalsIgnoreCase(state)) {
            filemap = displayDirectoryContents(f);

        }
        List<FileModel> finalDislayList = new ArrayList<>();
        finalDislayList.add(new FileModel("Largest Files", null, null, 0));

        Map<String, FileModel> sortFilesBySize = sortFilesBySize(filemap);
        ArrayList<FileModel> sortedfileList = getFileList(sortFilesBySize);
        ArrayList<FileModel> largeFileList = getLargeFiles(FILE_COUNT, sortedfileList);
        String avgFileSize = avgFileSize(filesList);
        ArrayList<String> extsList = frequentFileExtensions(EXT_COUNT);
        for (FileModel file : largeFileList) {
            finalDislayList.add(file);
        }
        finalDislayList.add(new FileModel("Average File Size", null, null, 0));
        finalDislayList.add(new FileModel(null, null, avgFileSize, 2));
        finalDislayList.add(new FileModel("Frequently used extentions", null, null, 0));
        if (extsList.size() > 0) {
            for (String ext : extsList) {
                finalDislayList.add(new FileModel(null, null, ext, 3));
            }
        } else {
            finalDislayList.add(new FileModel("No files with extensions found", null, null, 0));

        }
        //return result
        Intent intentResponse = new Intent();
        intentResponse.setAction(ACTION_MyIntentService);
        intentResponse.addCategory(Intent.CATEGORY_DEFAULT);
        intentResponse.putParcelableArrayListExtra(ACTION_DATA, (ArrayList<? extends Parcelable>) finalDislayList);
        sendBroadcast(intentResponse);
    }

    public HashMap<String, FileModel> displayDirectoryContents(File dir) {

        try {
            File[] files = dir.listFiles();
            if (files != null) {
                //send update
                Intent intentUpdate = new Intent();
                intentUpdate.setAction(ACTION_Size);
                intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
                intentUpdate.putExtra(ACTION_FILE_SIZE, files.length);
                sendBroadcast(intentUpdate);

                for (int i = 0; i < files.length; i++) {
                    intentUpdate = new Intent();
                    intentUpdate.setAction(ACTION_MyUpdate);
                    intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
                    intentUpdate.putExtra(EXTRA_KEY_UPDATE, i);
                    sendBroadcast(intentUpdate);
                    if (stop) {
                        break;
                    }
                    if (files[i].isDirectory()) {
                        System.out.println("directory:" + files[i].getCanonicalPath());
                        displayDirectoryContents(files[i]);
                    } else {
                        gfilemap.put(files[i].getName(), new FileModel(files[i].getName(), files[i].length() + ""
                                , StringUtils.getExt(files[i].getPath()), FileModel.DATA));
                        filesList.add(new FileModel(files[i].getName(), files[i].length() + "",
                                StringUtils.getExt(files[i].getPath()), FileModel.DATA));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return gfilemap;
    }

    @Override
    public void onDestroy() {

        Log.i("", "onCreate() , service stopped...");
    }

    public String avgFileSize(ArrayList<FileModel> fileList) {
        long avgFileSize = 0;
        if (fileList.size() > 0) {
            for (int i = 0; i < fileList.size(); i++) {
                if (fileList.get(i).getFileSize() != null && !fileList.get(i).getFileSize().isEmpty())
                    avgFileSize += Integer.parseInt(fileList.get(i).getFileSize());
            }
            if (avgFileSize > 0) {
                avgFileSize = avgFileSize / fileList.size();
            }
        }
        return Long.toString(avgFileSize);
    }

    private ArrayList<FileModel> getFileList(Map<String, FileModel> sortFilesBySize) {
        ArrayList<FileModel> fileList = new ArrayList<>();
        Iterator it = sortFilesBySize.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            fileList.add((FileModel) pair.getValue());
            it.remove();
        }

        return fileList;
    }

    public ArrayList<FileModel> getLargeFiles(int fileCount, ArrayList<FileModel> fileList) {

        ArrayList<FileModel> largeFileList = new ArrayList<>();
        int count = fileList.size();
        if (count < fileCount) {
            fileCount = count;
        }
        if (fileList.size() > 0) {
            for (int i = 0; i < fileCount; i++) {
                fileList.get(i).setFileSize(fileList.get(i).getFileSize() + " Bytes");
                largeFileList.add(fileList.get(i));
            }
        }
        return largeFileList;
    }

    public ArrayList<String> frequentFileExtensions(int extentionCount) {
        ArrayList<String> extsList = new ArrayList<>();

        Map<String, Integer> sortFilesByExtension = getSortedFileExtensions();
        if (sortFilesByExtension != null) {
            Iterator it = sortFilesByExtension.entrySet().iterator();
            int count = 0;

            while (it.hasNext()) {
                if (count != extentionCount) {
                    Map.Entry pair = (Map.Entry) it.next();
                    extsList.add(pair.getKey().toString());
                    it.remove();
                    count++;
                } else {
                    break;
                }
            }
        }

        return extsList;
    }

    private Map<String, Integer> getSortedFileExtensions() {

        HashMap<String, Integer> extensionMap = new HashMap<>();
        for (FileModel file : filesList) {
            if (file.getFileExtension() != null) {
                if (extensionMap.containsKey(file.getFileExtension())) {
                    extensionMap.put(file.getFileExtension(), Integer.parseInt(extensionMap.get(file.getFileExtension()).toString()) + 1);
                } else {
                    extensionMap.put(file.getFileExtension(), 1);
                }
            }
        }
        if (!extensionMap.isEmpty()) {
            return sortExtByCount(extensionMap);
        } else {
            return null;
        }
    }


    private Map<String, Integer> sortExtByCount(HashMap<String, Integer> extmap) {

        List<Map.Entry<String, Integer>> list = new LinkedList<>(extmap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {

                return o2.getValue().compareTo(o1.getValue());


            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMapOnSize = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMapOnSize.put(entry.getKey(), entry.getValue());
        }

        return sortedMapOnSize;
    }

    private Map<String, FileModel> sortFilesBySize(HashMap<String, FileModel> filemap) {

        List<Map.Entry<String, FileModel>> list = new LinkedList<>(filemap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, FileModel>>() {
            public int compare(Map.Entry<String, FileModel> o1,
                               Map.Entry<String, FileModel> o2) {

                return Long.valueOf(o2.getValue().getFileSize()).compareTo(Long.valueOf(o1.getValue().getFileSize()));


            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, FileModel> sortedMapOnSize = new LinkedHashMap<>();
        for (Map.Entry<String, FileModel> entry : list) {
            sortedMapOnSize.put(entry.getKey(), entry.getValue());
        }

        return sortedMapOnSize;
    }


    public class StopReceiver extends BroadcastReceiver {

        public static final String ACTION_STOP = "stop";

        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(receiver);
            stopSelf();
            stop = true;
        }

    }
}
