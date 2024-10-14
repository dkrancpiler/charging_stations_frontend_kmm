import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import com.emobilitychargingstations.database.StationEntity
import com.emobilitychargingstations.database.StationsDatabase
import com.emobilitychargingstations.database.UserInfoEntity
import com.example.emobilitychargingstations.data.stations.StationsRepository
import com.example.emobilitychargingstations.data.stations.StationsRepositoryImpl
import com.example.emobilitychargingstations.data.stations.api.StationsApi
import com.example.emobilitychargingstations.data.users.UsersRepository
import com.example.emobilitychargingstations.data.users.UsersRepositoryImpl
import com.example.emobilitychargingstations.domain.stations.StationsUseCase
import com.example.emobilitychargingstations.domain.user.UserUseCase
import com.example.emobilitychargingstations.models.Station
import com.example.emobilitychargingstations.models.StationFilterProperties
import com.example.emobilitychargingstations.models.StationProperties
import com.example.emobilitychargingstations.models.getEmptyProperties
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.dsl.module

fun repositoryModule() = module {
    single { provideApi() }
    single { provideDataSource(get()) }
    single<StationsRepository> { StationsRepositoryImpl(get(), get()) }
    single<UsersRepository> { UsersRepositoryImpl(get()) }
}

fun useCaseModule() = module {
    factory { UserUseCase(get()) }
    factory { StationsUseCase(get(), get()) }
}

fun provideApi(): StationsApi {
    return StationsApi()
}

fun provideDataSource(driver: SqlDriver): StationsDatabase {
    return StationsDatabase(driver, StationEntity.Adapter(
        propertiesAdapter = object : ColumnAdapter<StationProperties, String> {
            override fun decode(databaseValue: String): StationProperties {
                return if (databaseValue.isEmpty()){
                    getEmptyProperties()
                } else {
                    return Json.decodeFromString<StationProperties>(databaseValue)
                }
            }

            override fun encode(value: StationProperties): String {
                return Json.encodeToString(
                    value
                )
            }
        }
    ), UserInfoEntity.Adapter(favoriteStationsAdapter = object : ColumnAdapter<List<Station>, String> {
        override fun decode(databaseValue: String): List<Station> {
            return if (databaseValue.isEmpty()){
                listOf()
            } else {
                return Json.decodeFromString<List<Station>>(databaseValue)
            }
        }
        override fun encode(value: List<Station>): String {
            return Json.encodeToString(
                value
            )
        }
    }, filterPropertiesAdapter = object : ColumnAdapter<StationFilterProperties, String> {
        override fun decode(databaseValue: String): StationFilterProperties {
            return if (databaseValue.isEmpty()) return StationFilterProperties()
            else Json.decodeFromString(databaseValue)
        }
        override fun encode(value: StationFilterProperties): String {
            return Json.encodeToString(
                value
            )
        }
    }))
}