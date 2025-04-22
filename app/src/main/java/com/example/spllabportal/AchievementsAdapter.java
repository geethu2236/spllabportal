package com.example.spllabportal;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.AchievementViewHolder> {

    private List<Achievement> achievementsList;

    public AchievementsAdapter(List<Achievement> achievementsList) {
        this.achievementsList = achievementsList;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievementsList.get(position);

        // Ensure achievement is not null
        if (achievement == null) {
            Log.e("AchievementsAdapter", "Achievement object is null at position: " + position);
            return;
        }

        // Log data for debugging
        Log.d("AchievementsAdapter", "Binding: " + achievement.getEventName() + " | Status: " + achievement.getStatus());

        // Set event name & status safely
        holder.tvName.setText(achievement.getName() != null ? achievement.getName() : "No Name");
        holder.tvRollNumber.setText(achievement.getRollNumber() != null ? achievement.getRollNumber() : "No Roll Number");
        holder.tvEventName.setText(achievement.getEventName() != null ? achievement.getEventName() : "No Event Name");
        holder.tvYear.setText(achievement.getYear() != null ? achievement.getYear() : "No Year");
        holder.tvDepartment.setText(achievement.getDepartment() != null ? achievement.getDepartment() : "No Department");
        holder.tvSpecialLab.setText(achievement.getSpecialLab() != null ? achievement.getSpecialLab() : "No Special Lab");
        holder.tvProjectTitle.setText(achievement.getProjectTitle() != null ? achievement.getProjectTitle() : "No Project Title");
        holder.tvEventType.setText(achievement.getEventType() != null ? achievement.getEventType() : "No Event Type");
        holder.tvEventMode.setText(achievement.getEventMode() != null ? achievement.getEventMode() : "No Event Mode");
        holder.tvAchievementType.setText(achievement.getAchievementType() != null ? achievement.getAchievementType() : "No Achievement Type");
        holder.tvProofLink.setText(achievement.getProofLink() != null ? achievement.getProofLink() : "No Proof Link");
        holder.tvStatus.setText(achievement.getStatus() != null ? achievement.getStatus() : "No Status");

        // Set status color
        if ("Pending".equals(achievement.getStatus())) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.pending));
        } else if ("Approved".equals(achievement.getStatus())) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.approved));
        } else if ("Rejected".equals(achievement.getStatus())) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.rejected));
        } else {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.teal_700)); // Default color
        }
    }

    @Override
    public int getItemCount() {
        return achievementsList != null ? achievementsList.size() : 0;
    }

    public void updateData(List<Achievement> newAchievementsList) {
        this.achievementsList = newAchievementsList;
        notifyDataSetChanged();
    }

    public class AchievementViewHolder extends RecyclerView.ViewHolder {

        public TextView tvName, tvRollNumber, tvEventName, tvStatus, tvYear, tvDepartment,
                tvSpecialLab, tvProjectTitle, tvEventType, tvEventMode,
                tvAchievementType, tvProofLink;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_name);
            tvRollNumber = itemView.findViewById(R.id.tv_rollnumber);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvYear = itemView.findViewById(R.id.tv_year);
            tvDepartment = itemView.findViewById(R.id.tv_department);
            tvSpecialLab = itemView.findViewById(R.id.tv_specialLab);
            tvProjectTitle = itemView.findViewById(R.id.tv_projecttitle);
            tvEventType = itemView.findViewById(R.id.tv_eventType);
            tvEventMode = itemView.findViewById(R.id.tv_eventMode);
            tvAchievementType = itemView.findViewById(R.id.tv_achievementType);
            tvProofLink = itemView.findViewById(R.id.tv_proofLink);
        }
    }

}
