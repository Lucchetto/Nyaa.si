package com.zhenxiang.nyaa.db

import android.app.Application

class NyaaDbRepo(application: Application) {

    val previewsDao = NyaaDb(application.applicationContext).nyaaReleasesPreviewDao()
    val viewedDao = NyaaDb(application.applicationContext).viewedNyaaReleasesDao()
    val savedDao = NyaaDb(application.applicationContext).savedNyaaReleasesDao()
}