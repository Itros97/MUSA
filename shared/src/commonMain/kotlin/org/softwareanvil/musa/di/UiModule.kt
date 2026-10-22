package org.softwareanvil.musa.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.softwareanvil.musa.ui.products.edit.ProductEditActions
import org.softwareanvil.musa.ui.products.edit.ProductEditViewModel
import org.softwareanvil.musa.ui.products.list.ProductListViewModel

/** ViewModels and their helpers. */
val uiModule = module {
	factory { ProductEditActions(get(), get(), get(), get(), get(), get()) }
	viewModel { ProductListViewModel(get(), get(), get()) }
	viewModel { params -> ProductEditViewModel(params.get(), get(), get(), get()) }
}
