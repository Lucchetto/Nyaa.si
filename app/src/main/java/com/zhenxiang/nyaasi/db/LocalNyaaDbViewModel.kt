package com.zhenxiang.nyaasi.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.MutableLiveData




class LocalNyaaDbViewModel(application: Application): AndroidViewModel(application) {

    private val nyaaLocalRepo = NyaaDbRepo(application)

    val viewedReleasesSearchFilter = MutableLiveData<String>()
    // Source of data
    private val preFilterViewedReleases = Transformations.map(nyaaLocalRepo.viewedDao.getAllWithDetails()) {
        it.map { item -> item.details }
    }
    // Filtered list exposed for usage
    val viewedReleases = Transformations.switchMap(viewedReleasesSearchFilter) { query ->
        preFilterViewedReleases.searchByName(query)
    }

    val savedReleasesSearchFilter = MutableLiveData<String>()
    // Source of data
    private val preFilterSavedReleases = Transformations.map(nyaaLocalRepo.savedDao.getAllWithDetails()) {
        it.map { item -> item.details }
    }
    // Filtered list exposed for usage
    val savedReleases = Transformations.switchMap(savedReleasesSearchFilter) { query ->
        preFilterSavedReleases.searchByName(query)
    }

    init {
        // Required to emit value for viewedReleases on start
        viewedReleasesSearchFilter.value = null
        // Required to emit value for savedReleases on start
        savedReleasesSearchFilter.value = null
    }

    suspend fun getDetailsById(id: Int): NyaaReleaseDetails? {
        return withContext(Dispatchers.IO) {
            nyaaLocalRepo.detailsDao.getById(id)
        }
    }

    fun addToViewed(release: NyaaReleasePreview) {
        nyaaLocalRepo.previewsDao.insert(release)
        nyaaLocalRepo.viewedDao.insert(ViewedNyaaRelease(release.id, System.currentTimeMillis()))
    }

    fun isSaved(release: NyaaReleasePreview): Boolean {
        return nyaaLocalRepo.savedDao.getById(release.id) != null
    }

    fun toggleSaved(release: NyaaReleasePreview): Boolean {
        nyaaLocalRepo.savedDao.getById(release.id)?.let {
            nyaaLocalRepo.savedDao.delete(it)
            nyaaLocalRepo.previewsDao.deleteById(it.releaseId)
            return false
        } ?: run {
            nyaaLocalRepo.previewsDao.insert(release)
            nyaaLocalRepo.savedDao.insert(SavedNyaaRelease(release.id, System.currentTimeMillis()))
            return true
        }
    }

    fun addDetails(details: NyaaReleaseDetails) {
        nyaaLocalRepo.detailsDao.insert(details)
    }
}

private fun LiveData<List<NyaaReleasePreview>>.searchByName(query: String?): LiveData<List<NyaaReleasePreview>> {
    return if (query.isNullOrEmpty()) {
        this
    } else {
        Transformations.map(this) { list ->
            list.filter { item -> item.name.contains(query, true) }
        }
    }
}
