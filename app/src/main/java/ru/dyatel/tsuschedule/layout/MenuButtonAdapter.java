package ru.dyatel.tsuschedule.layout;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ru.dyatel.tsuschedule.R;

public class MenuButtonAdapter extends RecyclerView.Adapter<MenuButtonAdapter.Holder> {

    private class MenuEntry {

        int iconResId;
        int textResId;
        Runnable action;

        public MenuEntry(int iconResId, int textResId, Runnable action) {
            this.iconResId = iconResId;
            this.textResId = textResId;
            this.action = action;
        }

    }

    private MenuEntry[] menu = new MenuEntry[]{

    };

    class Holder extends RecyclerView.ViewHolder {

        public Holder(View v) {
            super(v);
        }

    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_button, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        final MenuEntry entry = menu[position];
        TextView button = (TextView) holder.itemView;
        button.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(button.getContext(), entry.iconResId), null, null, null
        );
        button.setText(entry.textResId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.action.run();
            }
        });
    }

    @Override
    public int getItemCount() {
        return menu.length;
    }

}
