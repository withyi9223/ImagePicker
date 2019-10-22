package zy.com.imagepicker;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fxYan
 */
public final class MediaPickerActivity extends RxAppCompatActivity implements View.OnClickListener {

    public static final String IMAGE_MAX_SIZE = "imageMaxSize";
    public static final String VIDEO_MAX_SIZE = "videoMaxSize";

    public static final String SELECTED_MEDIAS = "selectedMedias";

    public static final int REQUEST_WRITE_EXTERNAL_PERMISSION = 0;
    public static final int REQUEST_CAMERA_PERMISSION = 1;

    public static final int CAPTURE_IMAGE = 1;
    public static final int CAPTURE_VIDEO = 2;

    FrameLayout titleFl;
    RecyclerView recyclerView;
    View actionTv;
    View backIv;

    private MediaPickerAdapter adapter;
    private int imageMaxSize;
    private int videoMaxSize;
    private MainController controller;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_picker);
        initView(savedInstanceState);
        initEvent();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(IMAGE_MAX_SIZE, imageMaxSize);
        outState.putInt(VIDEO_MAX_SIZE, videoMaxSize);
    }

    public void initView(Bundle savedInstanceState) {
        titleFl = findViewById(R.id.titleFl);
        recyclerView = findViewById(R.id.recyclerView);
        backIv = findViewById(R.id.backIv);
        actionTv = findViewById(R.id.actionTv);

        controller = new MainController(new MyHandler(this), this);

        Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        if (bundle != null) {
            imageMaxSize = bundle.getInt(IMAGE_MAX_SIZE);
            videoMaxSize = bundle.getInt(VIDEO_MAX_SIZE);
        }
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        RecyclerView.ItemAnimator itemAnimator = recyclerView.getItemAnimator();
        if (itemAnimator != null) {
            itemAnimator.setChangeDuration(0);
        }
        adapter = new MediaPickerAdapter(this, imageMaxSize, videoMaxSize);
        adapter.setOnClickListener(this);
        recyclerView.setAdapter(adapter);

        boolean isWriteExternalGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        if (isWriteExternalGranted) {
            controller.getDatas(imageMaxSize, videoMaxSize);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_PERMISSION);
        }
    }

    public void initEvent() {
        backIv.setOnClickListener(this);
        actionTv.setOnClickListener(this);
    }

    public void onLocalMediaLoaded(List<LocalMediaItem> list) {
        adapter.clearDataSource();
        adapter.addAll(list);
        adapter.notifyDataSetChanged();
    }

    public void onImageCompressed(ArrayList<LocalMediaItem> list) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(SELECTED_MEDIAS, list);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.backIv) {
            finish();
        } else if (id == R.id.actionTv) {
            if (adapter.getSelectedList().isEmpty()) {
                Toast.makeText(this, "请选择", Toast.LENGTH_SHORT).show();
                return;
            }
            onImageCompressed(adapter.getSelectedList());
//                mPresenter.compressImages(adapter.getSelectedList());
        } else if (id == R.id.addIv) {
            boolean isWriteExternalGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
            boolean isCameraGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED;
            if (isWriteExternalGranted && isCameraGranted) {
                showPop();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            }
        } else if (id == R.id.chooseRl) {
            int position = (int) view.getTag();
            adapter.select(position);
            adapter.notifyDataSetChanged();
        }
    }

    private void showPop() {
        if (videoMaxSize == 0 && imageMaxSize == 0) return;

        if (videoMaxSize == 0) {
            toCaptureImage();
            return;
        }
        if (imageMaxSize == 0) {
            toCaptureVideo();
            return;
        }
        /*AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("title")
                //可以直接设置这三种button
                .setPositiveButton("拍摄视频", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toCaptureVideo();
                        dialog.dismiss();

                    }
                })
                .setNegativeButton("拍摄图片", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toCaptureImage();
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();*/
    }

    private void toCaptureVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        Uri uri = cr.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
//        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 10 * 1024 * 1024);// 最大10M
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);// 录制时长最大为60s
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, CAPTURE_VIDEO);
    }

    private void toCaptureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, CAPTURE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAPTURE_IMAGE:
                case CAPTURE_VIDEO:
                    controller.getDatas(imageMaxSize, videoMaxSize);
                    break;
                default:
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            controller.getDatas(imageMaxSize, videoMaxSize);
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                showPop();
            }
        }
    }

    public static void open(Activity context, int imageSize, int reqCode) {
        open(context, imageSize, 0, reqCode);
    }

    public static void open(Activity context, int imageSize, int videoSize, int reqCode) {
        Intent intent = new Intent(context, MediaPickerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(IMAGE_MAX_SIZE, imageSize);
        bundle.putInt(VIDEO_MAX_SIZE, videoSize);
        intent.putExtras(bundle);
        context.startActivityForResult(intent, reqCode);
    }

    static class MyHandler extends Handler {

        WeakReference<MediaPickerActivity> reference;

        public MyHandler(MediaPickerActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MediaPickerActivity activity = reference.get();
            switch (msg.what) {
                case 101:
                    activity.onLocalMediaLoaded(activity.controller.getMediaItemList());
                    break;
                default:

                    break;
            }
        }
    }
}
