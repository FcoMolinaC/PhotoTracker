package com.fmc.phototracker;

import java.io.File;

/**
 * Created by FMC on 26/11/2017.
 */

abstract class AlbumStorageDirFactory {
    public abstract File getAlbumStorageDir(String albumName);
}