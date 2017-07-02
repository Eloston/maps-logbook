package com.github.eloston.mapslogbook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;

import java.util.ArrayList;
import java.util.Collections;

public class BasicListAdapter extends RecyclerView.Adapter<BasicListAdapter.ViewHolder> implements ItemTouchHelperClass.ItemTouchHelperAdapter {
    private ArrayList<Entry> entries;
    private CoordinatorLayout coordLayout;
    private final MainActivity mainActivityParent;

    BasicListAdapter(MainActivity mainActivity, ArrayList<Entry> entries, CoordinatorLayout coordLayout) {
        this.entries = entries;
        this.coordLayout = coordLayout;
        this.mainActivityParent = mainActivity;
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(this.entries, i, i + 1);
            }
        }
        else {
            for(int i = fromPosition; i > toPosition; i--) {
                Collections.swap(this.entries, i, i - 1);
            }
        }
        this.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemRemoved(final int position) {
        final Entry justDeletedEntry =  this.entries.remove(position);
        final int indexOfDeletedEntry = position;
        this.notifyItemRemoved(position);

        Snackbar.make(this.coordLayout, this.mainActivityParent.getResources().getString(R.string.deleted_entry_notice) + " " + justDeletedEntry.getName(), Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BasicListAdapter.this.entries.add(indexOfDeletedEntry, justDeletedEntry);
                        BasicListAdapter.this.notifyItemInserted(indexOfDeletedEntry);
                    }
                }).show();
    }

    @Override
    public BasicListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_circle_try, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final BasicListAdapter.ViewHolder holder, final int position) {
        Entry entry = this.entries.get(position);
        SharedPreferences sharedPreferences = this.mainActivityParent.getSharedPreferences(MainActivity.THEME_PREFERENCES, MainActivity.MODE_PRIVATE);
        //Background color for each to-do item. Necessary for night/day mode
        int bgColor;
        //color of title text in our to-do item. White for night mode, dark gray for day mode
        int entryColor;
        if (sharedPreferences.getString(MainActivity.THEME_SAVED, MainActivity.LIGHTTHEME).equals(MainActivity.LIGHTTHEME)) {
            bgColor = Color.WHITE;
            entryColor = this.mainActivityParent.getResources().getColor(R.color.secondary_text);
        }
        else{
            bgColor = Color.DKGRAY;
            entryColor = Color.WHITE;
        }
        holder.linearLayout.setBackgroundColor(bgColor);

        holder.mToDoTextview.setMaxLines(2);

        holder.mToDoTextview.setText(entry.getName());
        holder.mToDoTextview.setTextColor(entryColor);
        TextDrawable myDrawable = TextDrawable.builder().beginConfig()
                .textColor(Color.WHITE)
                .useFont(Typeface.DEFAULT)
                .toUpperCase()
                .endConfig()
                .buildRound(entry.getName().substring(0,1), entry.getColor());

        holder.mColorImageView.setImageDrawable(myDrawable);

    }

    @Override
    public int getItemCount() {
        return this.entries.size();
    }

    @SuppressWarnings("deprecation")
    public class ViewHolder extends RecyclerView.ViewHolder{
        View mView;
        LinearLayout linearLayout;
        TextView mToDoTextview;
        ImageView mColorImageView;

        public ViewHolder(View v){
            super(v);
            mView = v;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Entry entry = BasicListAdapter.this.entries.get(ViewHolder.this.getAdapterPosition());
                    Intent i = new Intent(BasicListAdapter.this.mainActivityParent, AddEntryActivity.class);
                    i.putExtra(MainActivity.ENTRY_EXTRA, entry);
                    i.putExtra(MainActivity.ENTRY_NEW_EXTRA, false);
                    BasicListAdapter.this.mainActivityParent.startActivityForResult(i, MainActivity.REQUEST_ID_ENTRY);
                }
            });
            mToDoTextview = (TextView)v.findViewById(R.id.toDoListItemTextview);
            mColorImageView = (ImageView)v.findViewById(R.id.toDoListItemColorImageView);
            linearLayout = (LinearLayout)v.findViewById(R.id.listItemLinearLayout);
        }


    }
}
