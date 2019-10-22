package zy.com.imagepicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fxYan
 */
public final class MediaPickerAdapter extends RecyclerView.Adapter {

    public static final int ADD = 0;
    public static final int MEDIA = ADD + 1;

    private LayoutInflater inflater;
    private List<LocalMediaItem> dataSource = new ArrayList<>();
    private ArrayList<LocalMediaItem> selectedList = new ArrayList<>();
    private View.OnClickListener onClickListener;
    private int imageMaxSize;
    private int selectedImageSize;
    private int videoMaxSize;
    private int selectedVideoSize;
    private Context context;

    public MediaPickerAdapter(Context context, int imageMaxSize, int videoMaxSize) {
        inflater = LayoutInflater.from(context);
        this.context=context;
        this.imageMaxSize = imageMaxSize;
        this.videoMaxSize = videoMaxSize;
    }

    public List<LocalMediaItem> getDataSource() {
        return dataSource;
    }

    public void clearDataSource() {
        dataSource.clear();
    }

    public ArrayList<LocalMediaItem> getSelectedList() {
        return selectedList;
    }

    public void addAll(List<LocalMediaItem> data) {
        dataSource.addAll(data);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.onClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = MEDIA;
        if (position == 0) {
            viewType = ADD;
        }
        return viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ADD) {
            return new AddViewHolder(inflater.inflate(R.layout.listitem_media_add, parent, false));
        }
        return new MediaViewHolder(inflater.inflate(R.layout.listitem_media_pick, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ADD:
                ((AddViewHolder) holder).addIv.setOnClickListener(onClickListener);
                break;
            case MEDIA: {
                MediaViewHolder h = (MediaViewHolder) holder;
                LocalMediaItem item = dataSource.get(position - 1);
                Glide.with(context).load(item.getUri()).into(h.picIv);
                if (LocalMediaItem.MediaType.VIDEO == item.getType()) {
                    h.videoFlagIv.setVisibility(View.VISIBLE);
                } else {
                    h.videoFlagIv.setVisibility(View.GONE);
                }

                h.chooseRl.setTag(position - 1);
//                h.numTv.setOnClickListener(onClickListener);
                h.chooseRl.setOnClickListener(onClickListener);
                int indexOf = selectedList.indexOf(item);
                if (indexOf >= 0) {
                    h.numTv.setText(String.valueOf(indexOf + 1));
                    h.numTv.setBackgroundResource(R.drawable.shape_media_picker_selected);
                } else {
                    h.numTv.setText("");
                    h.numTv.setBackgroundResource(R.drawable.shape_media_picker_unselect);
                }
            }
            break;
            default:
        }
    }

    @Override
    public int getItemCount() {
        return dataSource.size() + 1;
    }

    public void select(int position) {
        LocalMediaItem item = dataSource.get(position);
        if (selectedList.contains(item)) {
            if (item.getType() == LocalMediaItem.MediaType.VIDEO) {
                selectedVideoSize--;
            } else {
                selectedImageSize--;
            }
            selectedList.remove(item);
        } else {
            if (item.getType() == LocalMediaItem.MediaType.IMAGE) {
                if (selectedImageSize >= imageMaxSize) {
                    Toast.makeText(context, String.format("最多只能选择%s张图片", imageMaxSize), Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedImageSize++;
                selectedList.add(item);
            } else {
                if (selectedVideoSize >= videoMaxSize) {
                    Toast.makeText(context, String.format("最多只能选择%s个视频", videoMaxSize), Toast.LENGTH_SHORT).show();
                    return;
//                } else if (item.getSize() > 10 * 1024 * 1024) {
//                    ToastUtils.normal("视频最大不超过10M，请重新选择");
//                    return;
                }
                selectedVideoSize++;
                selectedList.add(item);
            }
        }
    }

    static class AddViewHolder extends RecyclerView.ViewHolder {

        ImageView addIv;

        public AddViewHolder(View itemView) {
            super(itemView);
            addIv = itemView.findViewById(R.id.addIv);
        }
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {

        ImageView picIv;
        ImageView videoFlagIv;
        TextView numTv;
        RelativeLayout chooseRl;

        public MediaViewHolder(View itemView) {
            super(itemView);
            picIv = itemView.findViewById(R.id.picIv);
            videoFlagIv = itemView.findViewById(R.id.videoFlagIv);
            numTv = itemView.findViewById(R.id.numTv);
            chooseRl = itemView.findViewById(R.id.chooseRl);

        }
    }
}
