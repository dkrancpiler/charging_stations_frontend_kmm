package com.example.emobilitychargingstations

import com.example.emobilitychargingstations.domain.stations.StationsUseCase
import com.example.emobilitychargingstations.domain.user.UserUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class UseCasesProvider(): KoinComponent {
    fun userUseCase() = get<UserUseCase>()
    fun stationsUseCase() = get<StationsUseCase>()
}