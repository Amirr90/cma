package com.jccode.mycorrespondence.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.jccode.mycorrespondence.Adapter.LiveDataAdapter;
import com.jccode.mycorrespondence.R;

import java.util.List;


public class DocumentsSharedByMeFragment extends Fragment {
    RecyclerView recyclerView;
    LiveDataAdapter liveDataAdapter;
    Context context;
    List<DocumentSnapshot> snapshotList;

    public DocumentsSharedByMeFragment(Context context, List<DocumentSnapshot> snapshotList) {
        this.context = context;
        this.snapshotList = snapshotList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_documents_shared_by_me, container, false);

        //setRec(view);
        return view;
    }

    private void setRec(View view) {
/*
        recyclerView = view.findViewById(R.id.rec_1);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        liveDataAdapter = new LiveDataAdapter(snapshotList, context);
        recyclerView.setAdapter(liveDataAdapter);
        liveDataAdapter.notifyDataSetChanged();*/
    }
}