package com.example.smartcampus.client.ui.main.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcampus.client.R;
import com.example.smartcampus.client.ui.main.model.Room;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter для RecyclerView зі списком аудиторій
 *
 * ПРИСУТНЄ:
 *  Null-перевірки для всіх операцій
 *  Try-catch для безпеки
 * Fallback якщо View відсутні
 * Детальне логування
 */
public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private static final String TAG = "RoomAdapter";
    private List<Room> rooms = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Room room);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * ВИПРАВЛЕНО: Безпечне оновлення списку
     */
    public void setRooms(List<Room> newRooms) {
        if (newRooms == null) {
            Log.w(TAG, "⚠️ setRooms called with null, using empty list");
            newRooms = new ArrayList<>();
        }

        Log.d(TAG, "📝 Updating " + this.rooms.size() + " → " + newRooms.size() + " rooms");

        try {
            // DiffUtil для ефективного оновлення
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                    new RoomDiffCallback(this.rooms, newRooms)
            );

            this.rooms.clear();
            this.rooms.addAll(newRooms);
            diffResult.dispatchUpdatesTo(this);

            Log.d(TAG, "✅ Rooms updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "❌ DiffUtil failed, fallback to notifyDataSetChanged", e);

            // Fallback: простий notifyDataSetChanged
            this.rooms.clear();
            this.rooms.addAll(newRooms);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        try {
            if (position < 0 || position >= rooms.size()) {
                Log.e(TAG, "❌ Invalid position: " + position);
                return;
            }

            Room room = rooms.get(position);
            holder.bind(room);

            // Click listener
            holder.itemView.setOnClickListener(v -> {
                if (listener != null && room != null) {
                    listener.onItemClick(room);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Error binding view at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public Room getRoom(int position) {
        if (position >= 0 && position < rooms.size()) {
            return rooms.get(position);
        }
        Log.w(TAG, "⚠️ getRoom: invalid position " + position);
        return null;
    }

    /**
     * ViewHolder з null-перевірками та fallback
     */
    static class RoomViewHolder extends RecyclerView.ViewHolder {

        private final TextView roomName;
        private final TextView roomStatus;
        private final TextView roomDescription;
        private final View statusIndicator;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);

            roomName = itemView.findViewById(R.id.room_name);
            roomStatus = itemView.findViewById(R.id.room_status);
            roomDescription = itemView.findViewById(R.id.room_description);
            statusIndicator = itemView.findViewById(R.id.status_indicator);

            // Перевірка критичних View
            if (roomName == null) {
                Log.e(TAG, "❌ room_name (R.id.room_name) not found in item_room.xml!");
            }
        }

        /**
         * ✅ ВИПРАВЛЕНО: Повна null-безпека
         */
        void bind(Room room) {
            if (room == null) {
                Log.w(TAG, "⚠️ Attempting to bind null room");
                bindEmptyRoom();
                return;
            }

            try {
                bindRoomName(room);
                bindRoomStatus(room);
                bindRoomDescription(room);
                bindStatusIndicator(room);
            } catch (Exception e) {
                Log.e(TAG, "❌ Error in bind", e);
                bindEmptyRoom();
            }
        }

        private void bindRoomName(Room room) {
            if (roomName != null) {
                String name = room.getName();
                if (name == null || name.isEmpty()) {
                    name = "Аудиторія #" + room.getId();
                }
                roomName.setText(name);
            }
        }

        private void bindRoomStatus(Room room) {
            if (roomStatus != null) {
                String statusText = room.getStatusText();
                if (statusText == null || statusText.isEmpty()) {
                    statusText = room.isAvailable() ? "Вільна" : "Зайнята";
                }
                roomStatus.setText(statusText);

                // Колір тексту
                int textColor = room.isAvailable()
                        ? Color.parseColor("#4CAF50")  // Зелений
                        : Color.parseColor("#F44336"); // Червоний
                roomStatus.setTextColor(textColor);
            }
        }

        private void bindRoomDescription(Room room) {
            if (roomDescription != null) {
                String description = room.getDescription();

                if (description != null && !description.isEmpty()) {
                    roomDescription.setText(description);
                    roomDescription.setVisibility(View.VISIBLE);
                } else {
                    // Fallback опис
                    String fallback = "Поверх " + room.getFloor();
                    if (room.getCapacity() > 0) {
                        fallback += " • " + room.getCapacity() + " місць";
                    }
                    roomDescription.setText(fallback);
                    roomDescription.setVisibility(View.VISIBLE);
                }
            }
        }

        private void bindStatusIndicator(Room room) {
            if (statusIndicator != null) {
                int color = room.isAvailable()
                        ? Color.parseColor("#4CAF50")  // Зелений
                        : Color.parseColor("#F44336"); // Червоний
                statusIndicator.setBackgroundColor(color);
            }
        }

        private void bindEmptyRoom() {
            if (roomName != null) {
                roomName.setText("(невідома аудиторія)");
            }
            if (roomStatus != null) {
                roomStatus.setText("");
            }
            if (roomDescription != null) {
                roomDescription.setVisibility(View.GONE);
            }
            if (statusIndicator != null) {
                statusIndicator.setBackgroundColor(Color.GRAY);
            }
        }
    }

    /**
     * DiffUtil.Callback для ефективного оновлення
     */
    private static class RoomDiffCallback extends DiffUtil.Callback {

        private final List<Room> oldList;
        private final List<Room> newList;

        public RoomDiffCallback(List<Room> oldList, List<Room> newList) {
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
            try {
                Room oldRoom = oldList.get(oldPos);
                Room newRoom = newList.get(newPos);

                if (oldRoom == null || newRoom == null) return false;

                return oldRoom.getId() == newRoom.getId();
            } catch (Exception e) {
                Log.e(TAG, "❌ Error in areItemsTheSame", e);
                return false;
            }
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            try {
                Room oldRoom = oldList.get(oldPos);
                Room newRoom = newList.get(newPos);

                if (oldRoom == null || newRoom == null) return false;

                boolean namesSame = safeEquals(oldRoom.getName(), newRoom.getName());
                boolean availabilitySame = oldRoom.isAvailable() == newRoom.isAvailable();
                boolean floorsSame = oldRoom.getFloor() == newRoom.getFloor();
                boolean capacitySame = oldRoom.getCapacity() == newRoom.getCapacity();

                return namesSame && availabilitySame && floorsSame && capacitySame;
            } catch (Exception e) {
                Log.e(TAG, "❌ Error in areContentsTheSame", e);
                return false;
            }
        }

        private boolean safeEquals(String s1, String s2) {
            if (s1 == null && s2 == null) return true;
            if (s1 == null || s2 == null) return false;
            return s1.equals(s2);
        }
    }
}