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

package org.omnirom.music.providers;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Represents a connection to an audio provider (music source) service
 */
public class ProviderConnection extends AbstractProviderConnection {
    private static final String TAG = "ProviderConnection";

    private IMusicProvider mBinder;

    /**
     * Constructor
     *
     * @param ctx            The context to which this connection should be bound
     * @param providerName   The name of the provider (example: 'Spotify')
     * @param pkg            The package in which the service can be found (example: org.example.music)
     * @param serviceName    The name of the service (example: .MusicService)
     * @param configActivity The name of the configuration activity in the aforementioned package
     */
    public ProviderConnection(Context ctx, String providerName, String authorName, String pkg,
                              String serviceName, String configActivity) {
        super(ctx, providerName, authorName, pkg, serviceName, configActivity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindService() {
        if (mIsBound) {
            ProviderAggregator.getDefault().unregisterProvider(this);
            if (mAudioSocket != null) {
                mAudioSocket.disconnectSocket();
            }

            mBinder = null;
        }

        super.unbindService();
    }

    public IMusicProvider getBinder() {
        return mBinder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBinder = IMusicProvider.Stub.asInterface(service);

        try {
            // Tell the provider its identifier
            mBinder.setIdentifier(mIdentifier);

            // Register the provider
            ProviderAggregator.getDefault().registerProvider(this);
            mIsBound = true;
            if (DEBUG) Log.d(TAG, "Connected to providers " + name);

            // Automatically try to login the providers once bound
            if (mBinder.isSetup()) {
                Log.d(TAG, "Provider " + getProviderName() + " is setup! Trying to see if auth");
                if (!mBinder.isAuthenticated()) {
                    if (!mBinder.login()) {
                        Log.e(TAG, "Error while requesting login!");
                    }
                } else {
                    // Update playlists
                    ProviderAggregator.getDefault().getAllPlaylists();
                }
            }

            if (mAudioSocket != null) {
                mBinder.setAudioSocketName(mAudioSocket.getSocketName());
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Remote exception occurred on the set providers", e);
        }

        super.onServiceConnected(name, service);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        // Release the binder
        mBinder = null;
        super.onServiceDisconnected(name);
        Log.e(TAG, "Service disconnected: " + name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AudioHostSocket createAudioSocket(final String socketName) {
        AudioHostSocket host = super.createAudioSocket(socketName);

        try {
            if (mBinder != null) {
                mBinder.setAudioSocketName(socketName);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot assign audio socket to " + getProviderName(), e);
        }

        return host;
    }
}
