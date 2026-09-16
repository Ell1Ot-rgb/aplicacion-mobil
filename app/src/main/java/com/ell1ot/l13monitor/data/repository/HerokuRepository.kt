package com.ell1ot.l13monitor.data.repository

import com.ell1ot.l13monitor.data.remote.DynoFormationDto
import com.ell1ot.l13monitor.data.remote.HerokuApiService
import com.ell1ot.l13monitor.data.remote.ReleaseDto
import com.ell1ot.l13monitor.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HerokuRepository @Inject constructor(
    private val api: HerokuApiService,
) {
    suspend fun formation(): Result<List<DynoFormationDto>> = runCatching {
        api.formation(BuildConfig.HEROKU_APP)
    }

    suspend fun releases(): Result<List<ReleaseDto>> = runCatching {
        api.releases(BuildConfig.HEROKU_APP)
    }
}
