package com.jccode.mycorrespondence.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.jccode.mycorrespondence.AddDocumentActivity;
import com.jccode.mycorrespondence.LiveDocumentScreen;
import com.jccode.mycorrespondence.R;
import com.jccode.mycorrespondence.ViewAllDocumentActivity;
import com.jccode.mycorrespondence.ViewListSharedDocuments;
import com.jccode.mycorrespondence.utility.Util;

import java.util.ArrayList;
import java.util.List;

import static com.jccode.mycorrespondence.utility.Util.*;


public class DocumentFragment extends Fragment {
    RecyclerView rvDocument;
    private List<String> list = new ArrayList<>();
    DocumentAdapter documentAdapter;

    public DocumentFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_document, container, false);

        rvDocument = (RecyclerView) view.findViewById(R.id.rv_documents);
        rvDocument.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        rvDocument.setHasFixedSize(true);
        documentAdapter = new DocumentAdapter(getActivity(), list);

        rvDocument.setAdapter(documentAdapter);
        loadData();
        return view;
    }

    private void loadData() {

        list.add(ADD_DOCUMENT);
        list.add(VIEW_DOCUMENT);
        list.add(SEARCH_DOCUMENT);
        list.add(VIEW_SHARED_DOCUMENT);
        list.add(VIEW_RECEIVED_SHARED_DOCUMENT);
        list.add(VIEW_LIVE_SHARED_DOCUMENT_BY_ME);
        list.add(VIEW_LIVE_SHARED_DOCUMENT_TO_ME);



        documentAdapter.notifyDataSetChanged();


    }

    private class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.MyViewHolder> {
        Context context;
        List<String> homeList;
        int[] iconsList = {R.drawable.add_correspondence,
                R.drawable.correspondence_active, R.drawable.search_icon,
                R.drawable.share_documents, R.drawable.share_documents,
                R.drawable.share_documents, R.drawable.share_documents};

        public DocumentAdapter(Context context, List<String> homeList) {
            this.context = context;
            this.homeList = homeList;
        }

        @NonNull
        @Override
        public DocumentAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_parent_view2, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DocumentAdapter.MyViewHolder holder, final int position) {
            holder.textView.setText(homeList.get(position));
            holder.imageView.setImageResource(iconsList[position]);

            holder.cardHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selectedText = homeList.get(position);
                    switch (selectedText) {
                        case ADD_DOCUMENT: {
                            Intent intent = new Intent(getActivity(), AddDocumentActivity.class);
                            intent.putExtra(Util.FROM, "Add Document");
                            startActivity(intent);
                        }
                        break;

                        case VIEW_DOCUMENT: {
                            Intent intent = new Intent(getActivity(), ViewAllDocumentActivity.class);
                            intent.putExtra(Util.FROM, "View Document");
                            startActivity(intent);
                        }
                        break;
                        case VIEW_SHARED_DOCUMENT: {
                            startActivity(new Intent(context, ViewListSharedDocuments.class)
                                    .putExtra(TYPE, "SHARED")
                            );
                        }
                        break;
                        case VIEW_RECEIVED_SHARED_DOCUMENT: {
                            startActivity(new Intent(context, ViewListSharedDocuments.class)
                                    .putExtra(TYPE, "RECEIVED")
                            );

                        }

                        break;
                        case VIEW_LIVE_SHARED_DOCUMENT_BY_ME: {
                            startActivity(new Intent(context, LiveDocumentScreen.class)
                                    .putExtra(FROM, VIEW_LIVE_SHARED_DOCUMENT_BY_ME)
                            );

                        }
                        break;

                        case VIEW_LIVE_SHARED_DOCUMENT_TO_ME: {
                            startActivity(new Intent(context, LiveDocumentScreen.class)
                                    .putExtra(FROM, VIEW_LIVE_SHARED_DOCUMENT_TO_ME)
                            );

                        }
                        break;
                        default:
                            Snackbar.make(v, "coming soon", Snackbar.LENGTH_SHORT).show();

                    }


                }
            });
        }

        @Override
        public int getItemCount() {
            return homeList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout cardHome;
            TextView textView;
            ImageView imageView;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                textView = (TextView) itemView.findViewById(R.id.textView);
                cardHome = (RelativeLayout) itemView.findViewById(R.id.card_Home);
                imageView = (ImageView) itemView.findViewById(R.id.home_rec_imageView);
            }
        }
    }
}