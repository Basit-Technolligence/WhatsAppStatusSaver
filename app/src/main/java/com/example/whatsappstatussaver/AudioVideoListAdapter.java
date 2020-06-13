package com.example.whatsappstatussaver;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

public class AudioVideoListAdapter extends RecyclerView.Adapter<AudioVideoListAdapter.ViewHolder> {
    ArrayList<String> videosUri;
    ArrayList<String> videosTitle;
    ArrayList<String> videosDuration;
    String folderName;
    Activity activity;
    NativeTemplateStyle styles;
    UnifiedNativeAd unifiedNativeAd;

    public void setValues(ArrayList<String> videosUri, ArrayList<String> videosTitle, ArrayList<String> videosDuration, Activity activity) {
        this.videosUri = videosUri;
        this.videosTitle = videosTitle;
        this.videosDuration = videosDuration;
        this.activity = activity;

    }

    public void setValues(ArrayList<String> videosUri, ArrayList<String> videosTitle, ArrayList<String> videosDuration, Activity activity, NativeTemplateStyle styles, UnifiedNativeAd unifiedNativeAd) {
        this.videosUri = videosUri;
        this.videosTitle = videosTitle;
        this.videosDuration = videosDuration;
        this.activity = activity;
        this.styles = styles;
        this.unifiedNativeAd = unifiedNativeAd;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.native_ad_layout, parent, false);
            return new NativeAdViewHolder(view);

        } else if (viewType == 2) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.banner_ad_layout, parent, false);
            return new BannerAdViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_audio_video_list_items, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        if (getItemViewType(position) == 1) {
            NativeAdViewHolder new_holder = (NativeAdViewHolder) holder;
            new_holder.template.setStyles(styles);
            new_holder.template.setNativeAd(unifiedNativeAd);

        } else if (getItemViewType(position) == 2) {
            BannerAdViewHolder banner_new_holder = (BannerAdViewHolder) holder;
            banner_new_holder.mAdView.loadAd(new AdRequest.Builder().build());
        } else {
            try {
                holder.videoName.setText(videosTitle.get(position));
                holder.videoDescription.setText(getTimeString(Integer.parseInt(videosDuration.get(position))));
                    Glide.with(activity).asBitmap().load(new File(videosUri.get(position)))
                            .centerCrop().into(holder.imageView);

                holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity, ViewVideo.class);
                        intent.putStringArrayListExtra("VideoUri", videosUri);
                        intent.putStringArrayListExtra("VideoTitle", videosTitle);
                        intent.putExtra("VideoPosition", position);
                        if (styles != null) {
                            intent.putExtra("AdsLoaded", "Yes");
                        } else {
                            intent.putExtra("AdsLoaded", "No");
                        }
                        activity.startActivity(intent);
                    }
                });
                holder.save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveWhatsAppVideo(videosTitle.get(position),videosUri.get(position));
                    }
                });
                holder.moreOptions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popup = new PopupMenu(activity, holder.moreOptions);
                        //inflating menu from xml resource
                        popup.inflate(R.menu.video_list_item_menu);
                        //adding click listener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                int id = item.getItemId();
                                switch (id) {
                                    case R.id.delete: {
                                        try {

                                            deleteVideo(videosUri.get(position));
                                            recreateActivity();
                                            notifyDataSetChanged();

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    }
                                    case R.id.share: {
                                        File videoFile = new File(videosUri.get(position));
                                        Uri videoURI = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                                                ? FileProvider.getUriForFile(activity, activity.getPackageName(), videoFile)
                                                : Uri.fromFile(videoFile);
                                        ShareCompat.IntentBuilder.from(activity)
                                                .setStream(videoURI)
                                                .setType("video/mp4")
                                                .setChooserTitle("Share video...")
                                                .startChooser();
                                        break;
                                    }

                                    default: {
                                        Log.e("default", "its default ..");
                                    }
                                }
                                return true;
                            }
                        });
                        //displaying the popup
                        popup.show();
                    }
                });

            } catch (Exception e) {
//                Toast.makeText(activity,""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public int getItemCount() {
        return videosTitle.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (styles != null) {
            if (videosTitle.size() <= 5) {
                if (position == videosTitle.size() - 1) {
                    return 1;
                } else {
                    return 0;
                }

            } else {
                if (position == 4) {
                    return 1;
                } else if (position == 13 || position == 22 || position == 31 || position == 40 || position == 49) {
                    return 2;
                } else {
                    return 0;
                }
            }
        } else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, moreOptions;
        Button save;
        RelativeLayout relativeLayout;
        TextView videoName, videoDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
            imageView = (ImageView) itemView.findViewById(R.id.videoThumbnail);
            save = (Button) itemView.findViewById(R.id.save);
            videoName = (TextView) itemView.findViewById(R.id.videoName);
            moreOptions = (ImageView) itemView.findViewById(R.id.moreOptions);
            if (folderName.contains("Status of WhatsApp Folder") && moreOptions != null) {
                moreOptions.setVisibility(View.GONE);
                videoName.setVisibility(View.GONE);
                if(folderName.equals("Seen Status of WhatsApp Folder")) {
                    save.setVisibility(View.VISIBLE);
                }else{
                    moreOptions.setVisibility(View.VISIBLE);
                    videoName.setVisibility(View.VISIBLE);
                }
            }
            videoDescription = (TextView) itemView.findViewById(R.id.videoDescription);
        }
    }

    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        if (hours != 0) {
            buf
                    .append(String.format("%02d", hours))
                    .append(":")
                    .append(String.format("%02d", minutes))
                    .append(":")
                    .append(String.format("%02d", seconds));
        } else {
            buf
                    .append(String.format("%02d", minutes))
                    .append(":")
                    .append(String.format("%02d", seconds));
        }

        return buf.toString();
    }

    public class NativeAdViewHolder extends ViewHolder {
        TemplateView template;

        public NativeAdViewHolder(@NonNull View itemView) {
            super(itemView);
            template = itemView.findViewById(R.id.my_template);
        }
    }

    public class BannerAdViewHolder extends ViewHolder {
        AdView mAdView;

        public BannerAdViewHolder(@NonNull View itemView) {
            super(itemView);

            mAdView = itemView.findViewById(R.id.adView);
        }
    }


    public void deleteVideo(String mFilePath) {
        File file = new File(mFilePath);
        if (file.exists()) {
            if (file.delete()) {
                Log.e("-->", "file Deleted :" + mFilePath);
                callBroadCast();
            } else {
                Log.e("-->", "file not Deleted :" + mFilePath);
            }
        }


        //callBroadCast();
        callScanItent(activity, mFilePath);
    }

    public void callBroadCast() {
        if (Build.VERSION.SDK_INT >= 14) {
            Log.e("-->", " >= 14");
            MediaScannerConnection.scanFile(activity, new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                /*
                 *   (non-Javadoc)
                 * @see android.media.MediaScannerConnection.OnScanCompletedListener#onScanCompleted(java.lang.String, android.net.Uri)
                 */
                public void onScanCompleted(String path, Uri uri) {
                    Log.e("ExternalStorage", "Scanned " + path + ":");
                    Log.e("ExternalStorage", "-> uri=" + uri);
                }
            });
        }
    }

    public void callScanItent(Context context, String path) {
        MediaScannerConnection.scanFile(context,
                new String[]{path}, null, null);
    }

    public void recreateActivity() {
            activity.finish();
            activity.startActivity(new Intent(activity,WhatsappVideoList.class));
            Toast.makeText(activity, "Video Deleted Successfully", Toast.LENGTH_LONG).show();
    }

    public void setFolderName(String name) {
        folderName = name;
    }



    public void saveWhatsAppVideo(String videoName,String videoPath) {
        File myDirectory = new File(Environment.getExternalStorageDirectory(), "Downloads/WhatsApp Status Download");

        if(!myDirectory.exists()) {
            myDirectory.mkdirs();
        }
        File sourceLocation = new File(videoPath);
        File targetLocation = new File(Environment.getExternalStorageDirectory().toString() + "/Downloads/WhatsApp Status Download/"+videoName);
        if (sourceLocation.exists()) {
            try {
                InputStream in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);


                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();
                Toast.makeText(activity, "Status Saved Successfully, Swipe Left and Refresh List", Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                Toast.makeText(activity, ""+e.getMessage(), Toast.LENGTH_LONG).show();
            }


        } else {
            Toast.makeText(activity, "Failed to save, please try again", Toast.LENGTH_LONG).show();

        }
    }
}
