/*
 * Droid-Lite — minimal navigation host.
 * Replaces androidx.navigation3 with a ~120-line observable back stack.
 * Derived from Neo Store's navigation structure (GPLv3).
 */
package com.machiav3lli.fdroid.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.machiav3lli.fdroid.ui.pages.AppPage
import com.machiav3lli.fdroid.ui.pages.MainPage
import com.machiav3lli.fdroid.ui.pages.OnboardingPage
import com.machiav3lli.fdroid.ui.pages.PrefsPage
import com.machiav3lli.fdroid.ui.pages.SearchPage
import com.machiav3lli.fdroid.ui.pages.SortFilterSheet

sealed class NavRoute {
    data object Onboarding : NavRoute()
    data class Main(val page: Int = 0) : NavRoute()
    data class App(val packageName: String) : NavRoute()
    data class SortFilter(val page: String) : NavRoute()
    data class Search(val query: String) : NavRoute()
    data class Prefs(val page: Int = 0) : NavRoute()
}

class NavBackStack(vararg initial: NavRoute) {
    val routes: SnapshotStateList<NavRoute> = mutableStateListOf(*initial)

    val size: Int get() = routes.size
    val top: NavRoute? get() = routes.lastOrNull()

    fun add(route: NavRoute) {
        routes.add(route)
    }

    fun remove(route: NavRoute) {
        routes.remove(route)
    }

    fun removeLastOrNull() {
        if (routes.size > 1) routes.removeAt(routes.lastIndex)
    }

    fun navigate(route: NavRoute) {
        routes.removeAll { it == route }
        routes.add(route)
    }

    fun navigateUnique(route: NavRoute) {
        val topRoute = routes.lastOrNull()
        if (topRoute == route) return
        routes.removeAll { it::class == route::class }
        routes.add(route)
    }

    operator fun contains(route: NavRoute): Boolean = routes.contains(route)

    fun any(predicate: (NavRoute) -> Boolean): Boolean = routes.any(predicate)

    fun none(predicate: (NavRoute) -> Boolean): Boolean = routes.none(predicate)
}

@Composable
fun AppNavDisplay(
    backStack: NavBackStack,
    modifier: Modifier = Modifier,
) {
    val top by androidx.compose.runtime.rememberUpdatedState(backStack.top)

    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
    }

    AnimatedContent(
        modifier = modifier,
        targetState = top,
        transitionSpec = {
            if (targetState is NavRoute.Onboarding || targetState is NavRoute.Main) {
                (fadeIn(tween(200)) togetherWith fadeOut(tween(200)))
            } else {
                (slideInHorizontally(tween(250)) { it } togetherWith
                        slideOutHorizontally(tween(250)) { -it })
            }
        },
        contentKey = { it?.let { r -> r::class.simpleName } ?: "none" },
        label = "droidlite-nav",
    ) { route ->
        when (route) {
            is NavRoute.Onboarding -> OnboardingPage {
                if (backStack.none { it is NavRoute.Main }) backStack.add(NavRoute.Main())
                backStack.remove(NavRoute.Onboarding)
            }

            is NavRoute.Main -> MainPage(
                pageIndex = route.page,
                navigator = { backStack.navigateUnique(it) },
            )

            is NavRoute.Prefs -> PrefsPage(
                pageIndex = route.page,
                navigateUp = { backStack.removeLastOrNull() },
            )

            is NavRoute.App -> AppPage(route.packageName) {
                backStack.removeLastOrNull()
            }

            is NavRoute.SortFilter -> SortFilterSheet(route.page) {
                backStack.removeLastOrNull()
            }

            is NavRoute.Search -> SearchPage(route.query) {
                backStack.removeLastOrNull()
            }

            null -> {}
        }
    }
}
