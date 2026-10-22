package org.softwareanvil.musa.di

import org.koin.core.module.Module

/** Platform bindings, mainly the SQLite driver. */
expect val platformModule: Module
