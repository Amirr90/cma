package com.jccode.mycorrespondence.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jccode.mycorrespondence.ChatUsScreen;
import com.jccode.mycorrespondence.CreateCorrespondenceScreen;
import com.jccode.mycorrespondence.DonateActivity;
import com.jccode.mycorrespondence.DonateHistoryScreen;
import com.jccode.mycorrespondence.HomeScreen2;
import com.jccode.mycorrespondence.ListSharedDocumentsActivity;
import com.jccode.mycorrespondence.Profile;
import com.jccode.mycorrespondence.R;
import com.jccode.mycorrespondence.ShowCorrespondence;
import com.jccode.mycorrespondence.utility.Util;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jccode.mycorrespondence.utility.Util.EMAIL;
import static com.jccode.mycorrespondence.utility.Util.FILED;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.F_NAME;
import static com.jccode.mycorrespondence.utility.Util.IMAGE;
import static com.jccode.mycorrespondence.utility.Util.L_NAME;
import static com.jccode.mycorrespondence.utility.Util.POST;
import static com.jccode.mycorrespondence.utility.Util.RECEIVED;
import static com.jccode.mycorrespondence.utility.Util.REPLIED;
import static com.jccode.mycorrespondence.utility.Util.SEARCH_CORRESPONDENCE;
import static com.jccode.mycorrespondence.utility.Util.SHARED;
import static com.jccode.mycorrespondence.utility.Util.SHARED_CORRESPONDENCE;
import static com.jccode.mycorrespondence.utility.Util.SHARED_DOCUMENT;
import static com.jccode.mycorrespondence.utility.Util.SHARE_APP;
import static com.jccode.mycorrespondence.utility.Util.SORT_TYPE;
import static com.jccode.mycorrespondence.utility.Util.TITLE;
import static com.jccode.mycorrespondence.utility.Util.TYPE;
import static com.jccode.mycorrespondence.utility.Util.UN_REPLIED;
import static com.jccode.mycorrespondence.utility.Util.USER_NAME;
import static com.jccode.mycorrespondence.utility.Util.VIEW_RECEIVED_CORRESPONDENCE;
import static com.jccode.mycorrespondence.utility.Util.VIEW_SHARED_CORRESPONDENCE;


public class Correspondence extends Fragment {
    RecyclerView rvParent;
    List<String> list = new ArrayList<>();
    HomeAdapter parentAdapter;
    FirebaseUser user;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    String name, Class, subject, image;


    public Correspondence() {
        user = FirebaseAuth.getInstance().getCurrentUser();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_correspondence, container, false);

        rvParent = (RecyclerView) view.findViewById(R.id.rv_parent);
        rvParent.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        rvParent.setHasFixedSize(true);
        parentAdapter = new HomeAdapter(getActivity(), list);

        rvParent.setAdapter(parentAdapter);
        loadData();

        loadUserProfile(user.getUid(), view);

        return view;
    }

    private void loadData() {

        list.add("View All\nCorrespondence");
        list.add("Active\nCorrespondence");
        list.add("Closed\nCorrespondence");
        list.add(UN_REPLIED);
        list.add("Partial\nReplied");
        list.add(REPLIED);
        list.add(FILED);
        list.add("Donate Some Love");
        list.add("Chat\nwith us");
        list.add("Donation\nHistory");
        list.add("Add New\nCorrespondence");
        list.add("Profile");
    /*    list.add(VIEW_SHARED_CORRESPONDENCE);
        list.add(VIEW_RECEIVED_CORRESPONDENCE);*/
        list.add(SEARCH_CORRESPONDENCE);
        list.add(SHARE_APP);

        parentAdapter.notifyDataSetChanged();


    }

    private void loadUserProfile(String userId, final View view) {

        firestore.collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {

                            TextView tName = (TextView) view.findViewById(R.id.t_name);
                            TextView tClass = (TextView) view.findViewById(R.id.t_class);
                            final CircleImageView tProfile = (CircleImageView) view.findViewById(R.id.t_profile_image);

                            if (!documentSnapshot.contains(F_NAME))
                                name = user.getPhoneNumber();
                            else {
                                name = documentSnapshot.getString(F_NAME);
                                Class = documentSnapshot.getString(POST);
                                subject = documentSnapshot.getString(EMAIL);
                                image = documentSnapshot.getString(IMAGE);

                                if (name != null)
                                    tName.setText(name);
                                if (Class != null)
                                    tClass.setText(Class);
                                if (image != null)
                                    Picasso.with(getActivity()).load(image)
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .placeholder(R.drawable.profile)
                                            .into(tProfile, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError() {
                                                    Picasso.with(getActivity()).load(image).placeholder(R.drawable.profile).into(tProfile);
                                                }
                                            });
                            }
                        }
                    }
                });
    }

    private class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyHomeHolder> {
        Context context;
        List<String> homeList;
        int[] iconsList = {R.drawable.correspondence_all,
                R.drawable.correspondence_active, R.drawable.correspondence_close, R.drawable.correspondence_un_replied,
                R.drawable.correspondence_partial, R.drawable.correspondence_replied, R.drawable.file,
                R.drawable.donation, R.drawable.chat_us, R.drawable.donation_history,
                R.drawable.add_correspondence, R.drawable.profile2,
                /*R.drawable.share_documents,
                R.drawable.share_documents,*/ R.drawable.search_icon, R.drawable.share_app};

        public HomeAdapter(Context context, List<String> homeList) {
            this.context = context;
            this.homeList = homeList;
        }

        @NonNull
        @Override
        public HomeAdapter.MyHomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_parent_view2, parent, false);
            return new HomeAdapter.MyHomeHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeAdapter.MyHomeHolder holder, final int position) {
            holder.textView.setText(homeList.get(position));
            holder.imageView.setImageResource(iconsList[position]);

            holder.cardHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selectedText = homeList.get(position);
                    switch (selectedText) {
                        case "View All\nCorrespondence": {
                            startActivity(new Intent(context, ShowCorrespondence.class)
                                    .putExtra(SORT_TYPE, "All Correspondence"));

                        }
                        break;
                        case "Active\nCorrespondence": {
                            startActivity(new Intent(context, ShowCorrespondence.class)
                                    .putExtra(SORT_TYPE, "Active Correspondence"));

                        }
                        break;
                        case "Closed\nCorrespondence": {
                            startActivity(new Intent(context, ShowCorrespondence.class)
                                    .putExtra(SORT_TYPE, "Close Correspondence"));

                        }
                        break;
                        case UN_REPLIED: {
                            startActivity(new Intent(context, ShowCorrespondence.class)
                                    .putExtra(SORT_TYPE, "Un-Replied Correspondence"));

                        }
                        break;
                        case "Partial\nReplied": {
                            startActivity(new Intent(context, ShowCorrespondence.class)
                                    .putExtra(SORT_TYPE, "Partial Replied"));

                        }
                        break;
                        case REPLIED: {
                            startActivity(new Intent(context, ShowCorrespondence.class)
                                    .putExtra(SORT_TYPE, "Replied Correspondence"));

                        }

                        case VIEW_SHARED_CORRESPONDENCE: {
                            startActivity(new Intent(context, ListSharedDocumentsActivity.class)
                                    .putExtra(TYPE, SHARED)
                            );
                        }
                        break;
                        case VIEW_RECEIVED_CORRESPONDENCE: {
                            startActivity(new Intent(context, ListSharedDocumentsActivity.class)
                                    .putExtra(TYPE, RECEIVED)
                            );
                        }
                        break;
                        case FILED: {
                            startActivity(new Intent(context, ShowCorrespondence.class)
                                    .putExtra(SORT_TYPE, "File Correspondence"));

                        }
                        break;

                        case "Profile":
                            startActivity(new Intent(context, Profile.class));
                            break;

                        case "Donate Some Love":
                            startActivity(new Intent(context, DonateActivity.class));
                            break;
                        case "Add New\nCorrespondence": {
                            {
                                String From = "HomeScreen";
                                startActivity(new Intent(context, CreateCorrespondenceScreen.class)
                                        .putExtra(Util.FROM, From));
                            }

                        }
                        break;
                        case "Donation\nHistory": {
                            startActivity(new Intent(context, DonateHistoryScreen.class));


                        }
                        break;
                        case "Chat\nwith us": {
                            startActivity(new Intent(context, ChatUsScreen.class)
                                    .putExtra(USER_NAME, name));
                        }
                        break;
                        case SHARE_APP: {
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
                            sendIntent.setType("text/plain");

                            Intent shareIntent = Intent.createChooser(sendIntent, null);
                            startActivity(shareIntent);


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

        public class MyHomeHolder extends RecyclerView.ViewHolder {
            RelativeLayout cardHome;
            TextView textView;
            ImageView imageView;

            public MyHomeHolder(@NonNull View itemView) {
                super(itemView);

                textView = (TextView) itemView.findViewById(R.id.textView);
                cardHome = (RelativeLayout) itemView.findViewById(R.id.card_Home);
                imageView = (ImageView) itemView.findViewById(R.id.home_rec_imageView);
            }
        }
    }

}