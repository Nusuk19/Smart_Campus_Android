package com.example.smartcampus.client.ui.tags;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartcampus.client.R;
import com.example.smartcampus.client.data.local.entities.TagEntity;
import com.example.smartcampus.client.utils.VirtualTagGenerator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 🆕 Adapter для RecyclerView зі списком NFC тегів
 */
public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private List<TagEntity> tags = new ArrayList<>();
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(TagEntity tag);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(TagEntity tag);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    /**
     * Оновити список тегів
     */
    public void setTags(List<TagEntity> newTags) {
        if (newTags == null) {
            newTags = new ArrayList<>();
        }

        // Використовуємо DiffUtil для ефективного оновлення
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new TagDiffCallback(this.tags, newTags)
        );

        this.tags.clear();
        this.tags.addAll(newTags);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        TagEntity tag = tags.get(position);
        holder.bind(tag);

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(tag);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                return longClickListener.onItemLongClick(tag);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    /**
     * ViewHolder для тегу
     */
    static class TagViewHolder extends RecyclerView.ViewHolder {

        private final TextView tagIcon;
        private final TextView tagName;
        private final TextView tagType;
        private final TextView tagUid;
        private final TextView tagLastUsed;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            tagIcon = itemView.findViewById(R.id.tag_icon);
            tagName = itemView.findViewById(R.id.tag_name);
            tagType = itemView.findViewById(R.id.tag_type);
            tagUid = itemView.findViewById(R.id.tag_uid);
            tagLastUsed = itemView.findViewById(R.id.tag_last_used);
        }

        void bind(TagEntity tag) {
            if (tag == null) return;

            // Іконка залежно від типу
            if ("VIRTUAL".equals(tag.tagType)) {
                tagIcon.setText("📱");
                tagType.setText("Віртуальний тег");
            } else {
                tagIcon.setText("💳");
                tagType.setText("Фізичний тег");
            }

            // Назва
            tagName.setText(tag.name != null && !tag.name.isEmpty()
                    ? tag.name
                    : "Тег #" + tag.id);

            // UID (скорочений)
            String displayUid = VirtualTagGenerator.shortUid(tag.tagUid);
            tagUid.setText("UID: " + displayUid);

            // Останнє використання
            if (tag.lastUsedAt > 0) {
                String timeAgo = getTimeAgo(tag.lastUsedAt);
                tagLastUsed.setText("Використано: " + timeAgo);
            } else {
                tagLastUsed.setText("Ще не використовувався");
            }
        }

        /**
         * Форматувати час "2 години тому"
         */
        private String getTimeAgo(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                return days + " " + (days == 1 ? "день" : "дні") + " тому";
            } else if (hours > 0) {
                return hours + " " + (hours == 1 ? "година" : "годин") + " тому";
            } else if (minutes > 0) {
                return minutes + " " + (minutes == 1 ? "хвилина" : "хвилин") + " тому";
            } else {
                return "щойно";
            }
        }
    }

    /**
     * DiffUtil.Callback для ефективного оновлення
     */
    private static class TagDiffCallback extends DiffUtil.Callback {

        private final List<TagEntity> oldList;
        private final List<TagEntity> newList;

        public TagDiffCallback(List<TagEntity> oldList, List<TagEntity> newList) {
            this.oldList = oldList != null ? oldList : new ArrayList<>();
            this.newList = newList != null ? newList : new ArrayList<>();
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
        public boolean areItemsTheSame(int oldPos, int newPos) {
            TagEntity oldTag = oldList.get(oldPos);
            TagEntity newTag = newList.get(newPos);
            return oldTag.id == newTag.id;
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            TagEntity oldTag = oldList.get(oldPos);
            TagEntity newTag = newList.get(newPos);

            return oldTag.id == newTag.id &&
                    safeEquals(oldTag.name, newTag.name) &&
                    safeEquals(oldTag.tagType, newTag.tagType) &&
                    oldTag.isActive == newTag.isActive &&
                    oldTag.lastUsedAt == newTag.lastUsedAt;
        }

        private boolean safeEquals(String s1, String s2) {
            if (s1 == null && s2 == null) return true;
            if (s1 == null || s2 == null) return false;
            return s1.equals(s2);
        }
    }
}