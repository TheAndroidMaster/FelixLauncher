package com.james.felixlauncher.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.james.felixlauncher.R;
import com.james.felixlauncher.adapters.AppDetailAdapter;
import com.james.felixlauncher.data.AppDetail;

import java.util.ArrayList;
import java.util.List;

public class FavFragment extends Fragment {

    ArrayList<AppDetail> list;
    AppDetailAdapter adapter;
    PackageManager manager;
    ProgressBar progress;
    Thread t;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recycler, container, false);

        progress = (ProgressBar) rootView.findViewById(R.id.progressBar);
        progress.setVisibility(View.VISIBLE);

        manager = getContext().getPackageManager();
        list = new ArrayList<>();

        RecyclerView recycler = (RecyclerView) rootView.findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 3));

        adapter = new AppDetailAdapter(getActivity(), manager, list);
        adapter.setListener(new AppDetailAdapter.Listener() {
            @Override
            public void onChange() {
                load();
            }
        });
        adapter.setGrid(true);

        recycler.setAdapter(adapter);

        load();

        return rootView;
    }

    public void load() {
        if (t != null && t.isAlive()) t.interrupt();
        list.clear();

        t = new Thread() {
            @Override
            public void run() {
                List<ResolveInfo> availableActivities = manager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);
                for (ResolveInfo ri : availableActivities) {
                    AppDetail app = new AppDetail(getContext(), ri.loadLabel(manager).toString(), ri.activityInfo.packageName);
                    if (app.fav && !app.hide) list.add(app);
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter.getList().size() != list.size()) adapter.setList(list);
                        progress.setVisibility(View.GONE);
                    }
                });
            }
        };
        t.start();
    }
}