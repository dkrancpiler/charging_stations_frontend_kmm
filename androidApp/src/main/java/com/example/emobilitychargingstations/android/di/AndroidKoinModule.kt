package com.example.emobilitychargingstations.android.di

import android.app.Application
import app.cash.sqldelight.db.SqlDriver
import com.example.emobilitychargingstations.android.ui.viewmodels.StationsViewModel
import com.example.emobilitychargingstations.android.ui.viewmodels.UserViewModel
import com.example.emobilitychargingstations.data.local.DatabaseDriverFactory
import org.koin.dsl.module


fun androidKoinModule() = module {
    single { provideSqlDriver(get()) }
}

fun viewModelModule() = module {
    factory { StationsViewModel(get(), get()) }
    factory { UserViewModel(get()) }
}

fun provideSqlDriver(app: Application): SqlDriver {
    return DatabaseDriverFactory(app).createDriver()
}