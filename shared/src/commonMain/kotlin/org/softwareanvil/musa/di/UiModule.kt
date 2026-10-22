package org.softwareanvil.musa.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.softwareanvil.musa.ui.events.detail.EventActions
import org.softwareanvil.musa.ui.events.detail.EventDetailViewModel
import org.softwareanvil.musa.ui.events.edit.EventEditViewModel
import org.softwareanvil.musa.ui.events.list.EventListViewModel
import org.softwareanvil.musa.ui.home.DevToolsViewModel
import org.softwareanvil.musa.ui.products.edit.ProductEditActions
import org.softwareanvil.musa.ui.products.edit.ProductEditViewModel
import org.softwareanvil.musa.ui.products.list.ProductListViewModel

/** ViewModels and their helpers. */
val uiModule = module {
	factory { ProductEditActions(get(), get(), get(), get(), get(), get()) }
	viewModel { ProductListViewModel(get(), get(), get()) }
	viewModel { params -> ProductEditViewModel(params.get(), get(), get(), get()) }
	factory { EventActions(get(), get(), get(), get(), get(), get(), get(), get()) }
	viewModel { EventListViewModel(get()) }
	viewModel { params -> EventEditViewModel(params.get(), get(), get(), get(), get()) }
	viewModel { params -> EventDetailViewModel(params.get(), get(), get(), get()) }
	viewModel { DevToolsViewModel(get(), get()) }
}
