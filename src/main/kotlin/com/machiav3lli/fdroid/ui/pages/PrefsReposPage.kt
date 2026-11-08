package com.machiav3lli.fdroid.ui.pages

import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.manager.work.SyncWorker
import com.machiav3lli.fdroid.ui.components.RepositoryItem
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Plus
import com.machiav3lli.fdroid.ui.components.prefs.PreferenceGroupHeading
import com.machiav3lli.fdroid.ui.components.WideSearchField
import com.machiav3lli.fdroid.utils.extension.text.toAddressFingerprint
import com.machiav3lli.fdroid.utils.extension.koinNeoViewModel
import com.machiav3lli.fdroid.utils.notifyDuplicateRepoAddress
import com.machiav3lli.fdroid.viewmodels.PrefsVM
import com.machiav3lli.fdroid.viewmodels.SheetNavigationData
import kotlinx.coroutines.launch

/*
 * Droid-Lite repository manager: single-pane list; selecting a repo pushes
 * RepoPage as a full-screen overlay state. Replaces the adaptive
 * list-detail pane scaffold.
 */
@Composable
fun PrefsReposPage(viewModel: PrefsVM = koinNeoViewModel()) {
    val mActivity = LocalActivity.current as? NeoActivity
    val scope = rememberCoroutineScope()
    val pageState by viewModel.reposState.collectAsStateWithLifecycle()
    val queryState = rememberTextFieldState(pageState.query)

    var detail by remember { mutableStateOf<SheetNavigationData?>(null) }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val scan = result.data?.getStringExtra("SCAN_RESULT")
        scan?.replaceFirst("fdroidrepo", "http")
        (scan?.toUri() ?: Uri.EMPTY).toAddressFingerprint()
            .takeUnless { it.first.isEmpty() }
            ?.let { (address, fingerprint) ->
                scope.launch {
                    if (!viewModel.isDuplicateAddress(address)) {
                        detail = SheetNavigationData(
                            viewModel.addNewRepository(address = address, fingerprint = fingerprint),
                            true
                        )
                    } else mActivity?.notifyDuplicateRepoAddress(address)
                }
            }
    }

    LaunchedEffect(pageState.auth) {
        pageState.auth.let { (address, fingerprint) ->
            if (address.isNotEmpty()) {
                scope.launch {
                    if (!viewModel.isDuplicateAddress(address)) {
                        detail = SheetNavigationData(
                            viewModel.addNewRepository(address = address, fingerprint = fingerprint),
                            true
                        )
                    } else mActivity?.notifyDuplicateRepoAddress(address)
                    viewModel.setIntent("", "")
                }
            }
        }
    }

    detail?.let { data ->
        RepoPage(
            repositoryId = data.repositoryId,
            initEditMode = data.editMode,
            onDismiss = { detail = null },
        )
        return
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                shape = MaterialTheme.shapes.extraLarge,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(0.dp),
                onClick = {
                    scope.launch {
                        detail = SheetNavigationData(viewModel.addNewRepository(), true)
                    }
                },
            ) {
                Icon(
                    imageVector = Phosphor.Plus,
                    contentDescription = stringResource(id = R.string.add_repository),
                )
            }
        },
    ) { _ ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                WideSearchField(
                    queryState = queryState,
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.search_for_repository),
                    onCleanQuery = { viewModel.setSearchQuery("") },
                    inFocusOnLaunch = false,
                    onQueryChanged = { newQuery -> viewModel.setSearchQuery(newQuery) },
                )
            }
            stickyHeader(key = "enabledTitle") {
                PreferenceGroupHeading(
                    heading = stringResource(id = R.string.enabled),
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .fillMaxWidth(),
                )
            }
            items(items = pageState.enabledRepos, key = { it.id }) {
                RepositoryItem(
                    modifier = Modifier.animateItem(),
                    repository = it,
                    onSwitch = { repo ->
                        scope.launch { SyncWorker.enableRepo(repo, !repo.enabled) }
                    },
                    onClick = { repo ->
                        detail = SheetNavigationData(repo.id, false)
                    },
                )
            }
            stickyHeader(key = "disabledTitle") {
                PreferenceGroupHeading(
                    heading = stringResource(id = R.string.disabled),
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .fillMaxWidth(),
                )
            }
            items(items = pageState.disabledRepo, key = { it.id }) {
                RepositoryItem(
                    modifier = Modifier.animateItem(),
                    repository = it,
                    onSwitch = { repo ->
                        scope.launch { SyncWorker.enableRepo(repo, !repo.enabled) }
                    },
                    onClick = { repo ->
                        detail = SheetNavigationData(repo.id, false)
                    },
                )
            }
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                )
            }
        }
    }
}
