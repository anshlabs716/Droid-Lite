package com.machiav3lli.fdroid.data.repository

import com.machiav3lli.fdroid.data.database.dao.RBLogDao
import com.machiav3lli.fdroid.data.database.entity.RBLog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class PrivacyRepository(
    private val rbDao: RBLogDao,
) {
    fun getRBLogs(packageName: String): Flow<List<RBLog>> = rbDao.getFlow(packageName)

    fun getRBLogsMap(packageName: String): Flow<Map<String, RBLog>> = rbDao.getFlow(packageName)
        .mapLatest { list ->
            list.groupBy(RBLog::hash)
                .mapValues { (_, rbDataList) -> rbDataList.maxByOrNull(RBLog::timestamp)!! }
        }

    suspend fun loadRBLogs(packageName: String): List<RBLog> = rbDao.get(packageName)

    suspend fun upsertRBLogs(logs: Collection<RBLog>) {
        rbDao.multipleUpserts(logs.toList())
    }
}
