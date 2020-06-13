package com.example.whatsappstatussaver;

import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

import java.io.File;
import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public class WhatsappVideoListFragment extends Fragment {
    RecyclerView recyclerView;
    LinearLayout textLayout;
    ArrayList<String> audiosVideosUri = new ArrayList<>();
    ArrayList<String> audiosVideosTitle= new ArrayList<>();
    ArrayList<String> audiosVideosDuration= new ArrayList<>();
    AudioVideoListAdapter audioVideoListAdapter =new AudioVideoListAdapter();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         View view=inflater.inflate(R.layout.fragment_whatsapp_video_list, container, false);
        textLayout=(LinearLayout) view.findViewById(R.id.textLayout);
        recyclerView=(RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mainFunction();
        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                audiosVideosUri.clear();
                audiosVideosTitle.clear();
                audiosVideosDuration.clear();
                mainFunction();
                pullToRefresh.setRefreshing(false);
            }
        });

        return view;
    }
    private void mainFunction(){
        String statusType = getArguments().getString("StatusType");
        if(statusType.equals("Seen")){
            audioVideoListAdapter.setFolderName("Seen Status of WhatsApp Folder");
            fetchWhatsAppVideos("/storage/emulated/0/WhatsApp/Media/.Statuses");
        }else{
            audioVideoListAdapter.setFolderName("Saved Status of WhatsApp Folder");
            fetchWhatsAppVideos("/storage/emulated/0/Downloads/WhatsApp Status Download");
        }
    }
    public void fetchWhatsAppVideos(String whatsappMediaDirectoryName) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        File whatsappMediaDirectory = new File(whatsappMediaDirectoryName);
        File[] mediaDirectories = whatsappMediaDirectory.listFiles();
        if (mediaDirectories != null){
            for (File mediaDirectory : mediaDirectories) {
                if (mediaDirectory.getName().endsWith(".mp4")) {
                    audiosVideosUri.add(mediaDirectory.getAbsolutePath());
                    retriever.setDataSource(mediaDirectory.getAbsolutePath());
                    audiosVideosDuration.add(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    audiosVideosTitle.add(mediaDirectory.getName());
                }
            }
    }
        if(audiosVideosUri.size()==0){
            textLayout.setVisibility(View.VISIBLE);
        }else{
            textLayout.setVisibility(View.GONE);
        }
        retriever.release();
        audioVideoListAdapter.setValues(audiosVideosUri,audiosVideosTitle,audiosVideosDuration,getActivity());
        checkLayout();
        nativeAd();
    }
    private  void nativeAd(){
        MobileAds.initialize(getActivity(), "ca-app-pub-3940256099942544~3347511713");
        //native test ad id = ca-app-pub-3940256099942544/2247696110
        //native ad id=
        AdLoader adLoader = new AdLoader.Builder(getActivity(), "ca-app-pub-3940256099942544/2247696110")
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        // Show the ad.
                        NativeTemplateStyle styles = new
                                NativeTemplateStyle.Builder().build();
                        addingAdsItems();
                     audioVideoListAdapter.setValues(audiosVideosUri,audiosVideosTitle,audiosVideosDuration,getActivity(),styles,unifiedNativeAd);
                        recyclerView.setAdapter(audioVideoListAdapter);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // Handle the failure by logging, altering the UI, and so on.
                        //Toast.makeText(getApplicationContext(), "fail", Toast.LENGTH_SHORT).show();
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }
    private void addingAdsItems() {
        if(audiosVideosTitle.size()<=4){
            audiosVideosTitle.add("native add");
            audiosVideosUri.add("native add");
            audiosVideosDuration.add("native add");
        }else{
            audiosVideosTitle.add(4,"native add");
            audiosVideosUri.add(4,"native add");
            audiosVideosDuration.add(4,"native add");
            for(int i=0;i<audiosVideosTitle.size();i++){
                if(i==13 || i == 22 || i == 31 || i == 40 || i == 49) {
                    audiosVideosTitle.add(i,"banner add");
                    audiosVideosUri.add(i,"banner add");
                    audiosVideosDuration.add(i,"banner add");
                }
            }
        }
    }

    public void checkLayout() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(audioVideoListAdapter.getItemViewType(position)){
                    case 0:
                        return 1;
                    case 1:
                        return 2;
                    case 2:
                        return 2;

                    default:
                        return 1;


                }
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(audioVideoListAdapter);

    }



}
