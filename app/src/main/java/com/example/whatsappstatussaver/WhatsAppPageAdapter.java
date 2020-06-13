package com.example.whatsappstatussaver;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class WhatsAppPageAdapter extends FragmentStatePagerAdapter {
    int countTab;
    public WhatsAppPageAdapter(@NonNull FragmentManager fm, int countTab) {
        super(fm);
        this.countTab=countTab;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        WhatsappVideoListFragment whatsappVideoListFragment=null;
        Bundle bundle = new Bundle();
        switch (position){
            case 0:
                 whatsappVideoListFragment=new WhatsappVideoListFragment();
                bundle.putString("StatusType", "Saved");
                whatsappVideoListFragment.setArguments(bundle);
                return whatsappVideoListFragment;
            case 1:
                whatsappVideoListFragment=new WhatsappVideoListFragment();
                bundle.putString("StatusType", "Seen");
                whatsappVideoListFragment.setArguments(bundle);
                return whatsappVideoListFragment;
            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return countTab;
    }
}
