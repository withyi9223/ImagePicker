package zy.com.imagepicker;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

/**
 * ━━━━━━神兽出没━━━━━━
 * 　　　┏┓　　　┏┓
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　　　　　┃
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　┃
 * 　　┃　　　　　　　┃
 * 　　┃　　　┻　　　┃
 * 　　┃　　　　　　　┃
 * 　　┗━┓　　　┏━┛Code is far away from bug with the animal protecting
 * 　　　　┃　　　┃    神兽保佑,代码无bug
 * 　　　　┃　　　┃
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * 　　　　　┗┻┛　┗┻┛
 * ━━━━━━感觉萌萌哒━━━━━━
 * <p>
 * Created by zengyi on 2019/10/22.
 */
public class MainController {

    private Handler handler;
    private Context context;
    private List<LocalMediaItem> mediaItemList;


    public MainController(Handler handler, Context context) {
        this.handler = handler;
        this.context = context;
    }

    public void setMediaItemList(List<LocalMediaItem> mediaItemList) {
        this.mediaItemList = mediaItemList;
    }

    public List<LocalMediaItem> getMediaItemList() {
        return mediaItemList;
    }

    @SuppressLint("CheckResult")
    public void getDatas(final int imageMaxSize, final int videoMaxSize) {
        Observable.create((ObservableOnSubscribe<List<LocalMediaItem>>) emitter -> {
            ArrayList<LocalMediaItem> result = new ArrayList<>();
            if (imageMaxSize > 0) {
                ArrayList<LocalMediaItem> localImages = loadLocalImages();
                result.addAll(localImages);
            }
            if (videoMaxSize > 0) {
                ArrayList<LocalMediaItem> localVideos = loadLocalVideos();
                result.addAll(localVideos);
            }
            Collections.sort(result, (o1, o2) -> o2.getAddTime().compareTo(o1.getAddTime()));
            emitter.onNext(result);
            emitter.onComplete();
        }).compose(RxUtils.applySchedulers())
                .compose(RxUtils.bindToLifecycle(context))
                .subscribe(localMediaItems -> {
                    setMediaItemList(localMediaItems);
                    handler.sendEmptyMessage(101);
                });
    }

    private ArrayList<LocalMediaItem> loadLocalImages() {
        ArrayList<LocalMediaItem> list = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                    String.format("%s = ? or %s = ?", MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.MIME_TYPE),
                    new String[]{"image/jpeg", "image/png"},
                    String.format("%s DESC", MediaStore.Images.Media.DATE_ADDED));
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    String addedTime = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                    if (TextUtils.isEmpty(path)) continue;
                    File file = new File(path);
                    if (!file.exists()) continue;
                    Uri uri = Uri.fromFile(file);
                    LocalMediaItem item = new LocalMediaItem();
                    item.setPath(path);
                    item.setAddTime(addedTime);
                    item.setName(file.getName());
                    item.setUri(uri.toString());
                    item.setSize(file.length());
                    item.setType(LocalMediaItem.MediaType.IMAGE);
                    if (!list.contains(item)) {
                        list.add(item);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    private ArrayList<LocalMediaItem> loadLocalVideos() {
        ArrayList<LocalMediaItem> list = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                    null, null,
                    String.format("%s DESC", MediaStore.Video.Media.DATE_ADDED));
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                    String addedTime = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                    if (TextUtils.isEmpty(path)) continue;
                    File file = new File(path);
                    if (!file.exists()) continue;
                    Uri uri = Uri.fromFile(file);
                    LocalMediaItem item = new LocalMediaItem();
                    item.setPath(path);
                    item.setAddTime(addedTime);
                    item.setName(file.getName());
                    item.setUri(uri.toString());
                    item.setSize(file.length());
                    item.setType(LocalMediaItem.MediaType.VIDEO);
                    if (!list.contains(item)) {
                        list.add(item);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

}
