package com.learning.macys.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Environment;

import com.learning.macys.BR;
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
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by sonal on 3/7/18.
 */

public class FileViewModel extends BaseObservable {
    private static final int FILE_COUNT = 10;
    private static final int EXT_COUNT = 5;
    private ArrayList<FileModel> filesList;
    private HashMap<String, FileModel> gfilemap;
    public int progressValue;
    public int maxValue;
    public boolean isLoading;
    public boolean stopScanning;
    public boolean scanVisible = true;
    private List<FileModel> finalDislayList;


    public List<FileModel> getFinalDislayList() {
        return finalDislayList;
    }

    public void setFinalDislayList(List<FileModel> finalDislayList) {
        this.finalDislayList = finalDislayList;
    }

    @Bindable
    public boolean isScanVisible() {
        return scanVisible;
    }

    public void setScanVisible(boolean scanVisible) {
        this.scanVisible = scanVisible;
        notifyPropertyChanged(BR.scanVisible);

    }

    public boolean isStopScanning() {
        return stopScanning;
    }

    public void setStopScanning(boolean stopScanning) {
        this.stopScanning = stopScanning;
    }

    @Bindable
    public int getProgressValue() {
        return progressValue;
    }

    public void setProgressValue(int progressValue) {
        this.progressValue = progressValue;
        notifyPropertyChanged(BR.progressValue);
    }

    @Bindable
    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        notifyPropertyChanged(BR.maxValue);
    }

    @Bindable
    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
        notifyPropertyChanged(BR.loading);
    }

    public List<FileModel> getDisplayableData() {
        setLoading(true);

        filesList = new ArrayList<>();
        gfilemap = new HashMap<>();

        HashMap<String, FileModel> filemap = new HashMap<>();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File f = new File(path);
        String state = Environment.getExternalStorageState();
        if (f.isDirectory() && Environment.MEDIA_MOUNTED.equalsIgnoreCase(state)) {
            filemap = displayDirectoryContents(f);

        }
        finalDislayList = new ArrayList<>();
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
        Observable.fromCallable(new Callable<List<FileModel>>() {

            @Override
            public List<FileModel> call() throws Exception {
                return finalDislayList;

            }
        });
        return finalDislayList;
    }

    private HashMap<String, FileModel> displayDirectoryContents(File dir) {

        try {
            final File[] files = dir.listFiles();
            if (files != null) {
               Observable.fromCallable(new Callable<Integer>() {

                   @Override
                   public Integer call() throws Exception {
                       return files.length;

                   }
               })
                       .observeOn(AndroidSchedulers.mainThread())
                       .subscribeOn(Schedulers.io())
                        .subscribe(getMaxObserver());


                for (int i = 0; i < files.length; i++) {

                    final int finalI = i;
                    Observable.fromCallable(new Callable<Integer>() {

                        @Override
                        public Integer call() throws Exception {
                            return finalI;

                        }
                    })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(getObserver());

                    if (isStopScanning()) {
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

    private Observer<Integer> getMaxObserver() {

        return new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer integer) {
                setMaxValue(integer);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
    }

    private Observer<Integer> getObserver() {

        return new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer integer) {
                setProgressValue(integer);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
    }

    private String avgFileSize(ArrayList<FileModel> fileList) {
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

    private ArrayList<FileModel> getLargeFiles(int fileCount, ArrayList<FileModel> fileList) {

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

    private ArrayList<String> frequentFileExtensions(int extentionCount) {
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
        Map<String, FileModel> sortedMapOnSize = new LinkedHashMap<String, FileModel>();
        for (Map.Entry<String, FileModel> entry : list) {
            sortedMapOnSize.put(entry.getKey(), entry.getValue());
        }

        return sortedMapOnSize;
    }
}
