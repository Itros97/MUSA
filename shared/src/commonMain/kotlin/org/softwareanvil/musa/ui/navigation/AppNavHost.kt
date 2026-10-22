package org.softwareanvil.musa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import org.koin.compose.koinInject
import org.softwareanvil.musa.di.AppConfig
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.ui.events.detail.EventDetailScreen
import org.softwareanvil.musa.ui.events.list.EventListScreen
import org.softwareanvil.musa.ui.home.HomeScreen
import org.softwareanvil.musa.ui.home.HomeSection
import org.softwareanvil.musa.ui.products.edit.EditTarget
import org.softwareanvil.musa.ui.products.edit.ProductEditScreen
import org.softwareanvil.musa.ui.products.list.ProductListScreen

private object Routes {
	const val HOME = "home"
	const val PRODUCTS = "products"
	const val ARG_KIND = "kind"
	const val ARG_ID = "id"
	const val CREATE = "products/new/{$ARG_KIND}"
	const val EDIT = "products/{$ARG_ID}"
	const val EVENTS = "events"
	const val EVENT = "events/{$ARG_ID}"

	fun create(kind: ProductKind) = "products/new/${kind.name}"
	fun edit(id: ProductId) = "products/${id.value}"
	fun event(id: EventId) = "events/${id.value}"
}

/** Screens of the app and how to move between them. */
@Composable
fun AppNavHost() {
	val navController = rememberNavController()
	val config = koinInject<AppConfig>()
	val openProduct = { id: ProductId ->
		// Replaces the current editor so back returns to the catalogue, not to the original product.
		navController.navigate(Routes.edit(id)) { popUpTo(Routes.PRODUCTS) }
	}

	NavHost(navController = navController, startDestination = Routes.HOME) {
		composable(Routes.HOME) {
			HomeScreen(
				onOpenSection = { section -> routeOf(section)?.let(navController::navigate) },
				showDevTools = config.devTools,
			)
		}
		composable(Routes.PRODUCTS) {
			ProductListScreen(
				onOpenProduct = { navController.navigate(Routes.edit(it)) },
				onCreateProduct = { navController.navigate(Routes.create(it)) },
				onBack = { navController.popBackStack() },
			)
		}
		composable(Routes.CREATE, arguments = listOf(navArgument(Routes.ARG_KIND) { type = NavType.StringType })) { entry ->
			val kind = ProductKind.valueOf(checkNotNull(entry.arguments?.read { getStringOrNull(Routes.ARG_KIND) }))
			ProductEditScreen(EditTarget.New(kind), onClose = { navController.popBackStack() }, onOpenProduct = openProduct)
		}
		composable(Routes.EDIT, arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.StringType })) { entry ->
			val id = ProductId(checkNotNull(entry.arguments?.read { getStringOrNull(Routes.ARG_ID) }))
			ProductEditScreen(EditTarget.Existing(id), onClose = { navController.popBackStack() }, onOpenProduct = openProduct)
		}
		composable(Routes.EVENTS) {
			EventListScreen(onOpenEvent = { navController.navigate(Routes.event(it)) }, onBack = { navController.popBackStack() })
		}
		composable(Routes.EVENT, arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.StringType })) { entry ->
			val id = EventId(checkNotNull(entry.arguments?.read { getStringOrNull(Routes.ARG_ID) }))
			EventDetailScreen(id, onBack = { navController.popBackStack() })
		}
	}
}

/** Route that opens [section], or null while the section is not built yet. */
private fun routeOf(section: HomeSection): String? = when (section) {
	HomeSection.CATALOGUE -> Routes.PRODUCTS
	HomeSection.EVENTS -> Routes.EVENTS
	HomeSection.SALES, HomeSection.MARKET_ANALYSIS -> null
}
