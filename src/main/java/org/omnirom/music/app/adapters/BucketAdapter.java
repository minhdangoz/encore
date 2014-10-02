/*
 * Copyright (C) 2014 Fastboot Mobile, LLC.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>.
 */

package org.omnirom.music.app.adapters;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.omnirom.music.api.echonest.AutoMixBucket;
import org.omnirom.music.app.R;

import java.util.List;
import java.util.zip.Inflater;

/**
 * Adapter to display a ListView of AutoMix buckets
 */
public class BucketAdapter extends BaseAdapter {

    /**
     * View Holder
     */
    public static class ViewHolder {
        public TextView tvBucketName;
        public ProgressBar pbBucketSpinner;
    }

    private List<AutoMixBucket> mBuckets;

    /**
     * Sets the list of {@link org.omnirom.music.api.echonest.AutoMixBucket} to show
     * @param buckets The list of buckets to show
     */
    public void setBuckets(List<AutoMixBucket> buckets) {
        mBuckets = buckets;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return mBuckets == null ? 0 : mBuckets.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AutoMixBucket getItem(int i) {
        return mBuckets != null ? mBuckets.get(i) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder tag;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_bucket, viewGroup, false);
            tag = new ViewHolder();
            tag.tvBucketName = (TextView) view.findViewById(R.id.tvBucketName);
            tag.pbBucketSpinner = (ProgressBar) view.findViewById(R.id.pbBucketSpinner);
            view.setTag(tag);
        } else {
            tag = (ViewHolder) view.getTag();
        }

        AutoMixBucket bucket = getItem(i);
        tag.tvBucketName.setText(bucket.getName());

        return view;
    }
}
