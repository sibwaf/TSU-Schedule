package ru.dyatel.tsuschedule.layout;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ru.dyatel.tsuschedule.NavigationHandler;
import ru.dyatel.tsuschedule.R;

import java.util.LinkedList;
import java.util.List;

public class MenuButtonAdapter extends RecyclerView.Adapter<MenuButtonAdapter.Holder> {

	static class Holder extends RecyclerView.ViewHolder {

		Holder(View v) {
			super(v);
		}

	}

	private List<MenuEntry> menu = new LinkedList<>();

	private NavigationHandler navigationHandler;

	public MenuButtonAdapter(NavigationHandler navigationHandler) {
		this.navigationHandler = navigationHandler;
	}

	public void addMenuEntry(MenuEntry entry) {
		menu.add(entry);
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
		final MenuEntry entry = menu.get(position);
		TextView button = (TextView) holder.itemView;
		button.setCompoundDrawablesWithIntrinsicBounds(
				ContextCompat.getDrawable(button.getContext(), entry.getIconResId()), null, null, null
		);
		button.setText(entry.getTextResId());
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				navigationHandler.navigate(entry.getFragmentProvider().invoke(), entry.getName());
			}
		});
	}

	@Override
	public int getItemCount() {
		return menu.size();
	}

}
