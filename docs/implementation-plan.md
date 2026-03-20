# Implementation Plan

## Phase 1: Theme
App colors and Material 3 theme

- [ ] AppColors.kt — Color palette with primary, secondary, tertiary, error colors derived from ui_design.seed_color
- [ ] AppTheme.kt — Material 3 theme composable wrapping MaterialTheme with light/dark color schemes and configureSystemAppearance expect/actual

## Phase 2: Feature: Market Directory
All layers for Market Directory feature

- [ ] MarketEntity.kt — Room @Entity for Market with fields: id ( String), name ( String), address ( String), daysOpen ( String), hours ( String), isOrganicCertified ( Boolean), phoneNumber ( String), websiteUrl ( String) + 4 more. Store Instant as Long (toEpochMilliseconds).
- [ ] MarketDao.kt — Room @Dao for MarketEntity with getAll(): Flow, getById(id), insert, update, deleteById
- [ ] [modify] AppDatabase.kt — Add MarketEntity to @Database entities array, add marketDao() abstract fun, increment version
- [ ] Market.kt — Domain data class for Market with fields: id ( String), name ( String), address ( String), daysOpen ( String), hours ( String), isOrganicCertified ( Boolean), phoneNumber ( String), websiteUrl ( String) + 4 more. Include toEntity() extension and Entity.toDomain() mapper. Use kotlin.time.Instant for date/time fields.
- [ ] MarketRepository.kt — Repository interface for Market with items: StateFlow, isLoading: StateFlow, error: StateFlow, loadAll, getById, insert, update, delete
- [ ] MarketRepositoryImpl.kt — Repository implementation for Market. Extends GoogleSheetsRepository<Market> (pre-built). Implements mapRow() to parse sheet rows. Uses MarketDao for offline caching.
- [ ] GetMarketListUseCase.kt — operator fun invoke(): StateFlow<List<Market>> from repository. suspend fun load() triggers loadAll. suspend fun refresh() triggers refresh.
- [ ] GetMarketDetailUseCase.kt — suspend fun load(id: String) selects specific Market from repository.
- [ ] DeleteMarketUseCase.kt — suspend fun execute(id: String) deletes Market from repository.
- [ ] MarketListViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] MarketListScreen.kt — Composable wrapping GenericListScreen from core/presentation/screens/. Uses koinViewModel() default parameter.
- [ ] MarketDetailViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] MarketDetailScreen.kt — Composable wrapping GenericDetailScreen from core/presentation/screens/. Uses koinViewModel() default parameter.
- [ ] MarketSearchViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] MarketSearchScreen.kt — Composable wrapping GenericSearchScreen from core/presentation/screens/. Uses koinViewModel() default parameter.

## Phase 3: Feature: Search & Filter
All layers for Search & Filter feature

- [ ] ProductEntity.kt — Room @Entity for Product with fields: id ( String), name ( String), category ( String), marketId ( String), isInSeason ( Boolean), photoUrl ( String), price ( Double), vendorName ( String). Store Instant as Long (toEpochMilliseconds).
- [ ] ProductDao.kt — Room @Dao for ProductEntity with getAll(): Flow, getById(id), insert, update, deleteById
- [ ] [modify] AppDatabase.kt — Add ProductEntity to @Database entities array, add productDao() abstract fun, increment version
- [ ] Product.kt — Domain data class for Product with fields: id ( String), name ( String), category ( String), marketId ( String), isInSeason ( Boolean), photoUrl ( String), price ( Double), vendorName ( String). Include toEntity() extension and Entity.toDomain() mapper. Use kotlin.time.Instant for date/time fields.
- [ ] ProductRepository.kt — Repository interface for Product with items: StateFlow, isLoading: StateFlow, error: StateFlow, loadAll, getById, insert, update, delete
- [ ] ProductRepositoryImpl.kt — Repository implementation for Product. Extends GoogleSheetsRepository<Product> (pre-built). Implements mapRow() to parse sheet rows. Uses ProductDao for offline caching.
- [ ] GetProductListUseCase.kt — operator fun invoke(): StateFlow<List<Product>> from repository. suspend fun load() triggers loadAll. suspend fun refresh() triggers refresh.
- [ ] GetProductDetailUseCase.kt — suspend fun load(id: String) selects specific Product from repository.
- [ ] DeleteProductUseCase.kt — suspend fun execute(id: String) deletes Product from repository.
- [ ] ProductListViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] ProductListScreen.kt — Composable wrapping GenericListScreen from core/presentation/screens/. Uses koinViewModel() default parameter.
- [ ] ProductDetailViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] ProductDetailScreen.kt — Composable wrapping GenericDetailScreen from core/presentation/screens/. Uses koinViewModel() default parameter.
- [ ] ProductSearchViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] ProductSearchScreen.kt — Composable wrapping GenericSearchScreen from core/presentation/screens/. Uses koinViewModel() default parameter.

## Phase 4: Feature: Favorites
All layers for Favorites feature

- [ ] FavoriteEntity.kt — Room @Entity for Favorite with fields: id ( String), marketId ( String), userId ( String), savedAt ( Instant). Store Instant as Long (toEpochMilliseconds).
- [ ] FavoriteDao.kt — Room @Dao for FavoriteEntity with getAll(): Flow, getById(id), insert, update, deleteById
- [ ] [modify] AppDatabase.kt — Add FavoriteEntity to @Database entities array, add favoriteDao() abstract fun, increment version
- [ ] Favorite.kt — Domain data class for Favorite with fields: id ( String), marketId ( String), userId ( String), savedAt ( Instant). Include toEntity() extension and Entity.toDomain() mapper. Use kotlin.time.Instant for date/time fields.
- [ ] FavoriteRepository.kt — Repository interface for Favorite with items: StateFlow, isLoading: StateFlow, error: StateFlow, loadAll, getById, insert, update, delete
- [ ] FavoriteRepositoryImpl.kt — Repository implementation for Favorite. Extends GoogleSheetsRepository<Favorite> (pre-built). Implements mapRow() to parse sheet rows. Uses FavoriteDao for offline caching.
- [ ] GetFavoriteListUseCase.kt — operator fun invoke(): StateFlow<List<Favorite>> from repository. suspend fun load() triggers loadAll. suspend fun refresh() triggers refresh.
- [ ] GetFavoriteDetailUseCase.kt — suspend fun load(id: String) selects specific Favorite from repository.
- [ ] DeleteFavoriteUseCase.kt — suspend fun execute(id: String) deletes Favorite from repository.
- [ ] FavoriteListViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] FavoriteListScreen.kt — Composable wrapping GenericListScreen from core/presentation/screens/. Uses koinViewModel() default parameter.
- [ ] FavoriteDetailViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] FavoriteDetailScreen.kt — Composable wrapping GenericDetailScreen from core/presentation/screens/. Uses koinViewModel() default parameter.
- [ ] FavoriteSearchViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] FavoriteSearchScreen.kt — Composable wrapping GenericSearchScreen from core/presentation/screens/. Uses koinViewModel() default parameter.

## Phase 5: Feature: Seasonal Guide
All layers for Seasonal Guide feature

- [ ] SeasonalGuideEntity.kt — Room @Entity for SeasonalGuide with fields: id (String). Store Instant as Long (toEpochMilliseconds).
- [ ] SeasonalGuideDao.kt — Room @Dao for SeasonalGuideEntity with getAll(): Flow, getById(id), insert, update, deleteById
- [ ] [modify] AppDatabase.kt — Add SeasonalGuideEntity to @Database entities array, add seasonalGuideDao() abstract fun, increment version
- [ ] SeasonalGuide.kt — Domain data class for SeasonalGuide with fields: id (String). Include toEntity() extension and Entity.toDomain() mapper. Use kotlin.time.Instant for date/time fields.
- [ ] SeasonalGuideRepository.kt — Repository interface for SeasonalGuide with items: StateFlow, isLoading: StateFlow, error: StateFlow, loadAll, getById, insert, update, delete
- [ ] SeasonalGuideRepositoryImpl.kt — Repository implementation for SeasonalGuide. Extends GoogleSheetsRepository<SeasonalGuide> (pre-built). Implements mapRow() to parse sheet rows. Uses SeasonalGuideDao for offline caching.
- [ ] GetSeasonalGuideListUseCase.kt — operator fun invoke(): StateFlow<List<SeasonalGuide>> from repository. suspend fun load() triggers loadAll. suspend fun refresh() triggers refresh.
- [ ] GetSeasonalGuideDetailUseCase.kt — suspend fun load(id: String) selects specific SeasonalGuide from repository.
- [ ] DeleteSeasonalGuideUseCase.kt — suspend fun execute(id: String) deletes SeasonalGuide from repository.
- [ ] SeasonalGuideListViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] SeasonalGuideListScreen.kt — Composable wrapping GenericListScreen from core/presentation/screens/. Uses koinViewModel() default parameter.
- [ ] SeasonalGuideDetailViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] SeasonalGuideDetailScreen.kt — Composable wrapping GenericDetailScreen from core/presentation/screens/. Uses koinViewModel() default parameter.
- [ ] SeasonalGuideSearchViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] SeasonalGuideSearchScreen.kt — Composable wrapping GenericSearchScreen from core/presentation/screens/. Uses koinViewModel() default parameter.

## Phase 6: Feature: Onboarding
All layers for Onboarding feature

- [ ] OnboardingEntity.kt — Room @Entity for Onboarding with fields: id (String). Store Instant as Long (toEpochMilliseconds).
- [ ] OnboardingDao.kt — Room @Dao for OnboardingEntity with getAll(): Flow, getById(id), insert, update, deleteById
- [ ] [modify] AppDatabase.kt — Add OnboardingEntity to @Database entities array, add onboardingDao() abstract fun, increment version
- [ ] Onboarding.kt — Domain data class for Onboarding with fields: id (String). Include toEntity() extension and Entity.toDomain() mapper. Use kotlin.time.Instant for date/time fields.
- [ ] OnboardingRepository.kt — Repository interface for Onboarding with items: StateFlow, isLoading: StateFlow, error: StateFlow, loadAll, getById, insert, update, delete
- [ ] OnboardingRepositoryImpl.kt — Repository implementation for Onboarding. Extends GoogleSheetsRepository<Onboarding> (pre-built). Implements mapRow() to parse sheet rows. Uses OnboardingDao for offline caching.
- [ ] GetOnboardingListUseCase.kt — operator fun invoke(): StateFlow<List<Onboarding>> from repository. suspend fun load() triggers loadAll. suspend fun refresh() triggers refresh.
- [ ] GetOnboardingDetailUseCase.kt — suspend fun load(id: String) selects specific Onboarding from repository.
- [ ] DeleteOnboardingUseCase.kt — suspend fun execute(id: String) deletes Onboarding from repository.
- [ ] OnboardingListViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] OnboardingListScreen.kt — Composable wrapping GenericListScreen from core/presentation/screens/. Uses koinViewModel() default parameter.
- [ ] OnboardingDetailViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] OnboardingDetailScreen.kt — Composable wrapping GenericDetailScreen from core/presentation/screens/. Uses koinViewModel() default parameter.
- [ ] OnboardingOnboardingViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] OnboardingOnboardingScreen.kt — Composable wrapping GenericOnboardingScreen from core/presentation/screens/. Uses koinViewModel() default parameter.
- [ ] OnboardingSearchViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] OnboardingSearchScreen.kt — Composable wrapping GenericSearchScreen from core/presentation/screens/. Uses koinViewModel() default parameter.

## Phase 7: Feature: Settings
All layers for Settings feature

- [ ] SettingsEntity.kt — Room @Entity for Settings with fields: id (String). Store Instant as Long (toEpochMilliseconds).
- [ ] SettingsDao.kt — Room @Dao for SettingsEntity with getAll(): Flow, getById(id), insert, update, deleteById
- [ ] [modify] AppDatabase.kt — Add SettingsEntity to @Database entities array, add settingsDao() abstract fun, increment version
- [ ] Settings.kt — Domain data class for Settings with fields: id (String). Include toEntity() extension and Entity.toDomain() mapper. Use kotlin.time.Instant for date/time fields.
- [ ] SettingsRepository.kt — Repository interface for Settings with items: StateFlow, isLoading: StateFlow, error: StateFlow, loadAll, getById, insert, update, delete
- [ ] SettingsRepositoryImpl.kt — Repository implementation for Settings. Extends GoogleSheetsRepository<Settings> (pre-built). Implements mapRow() to parse sheet rows. Uses SettingsDao for offline caching.
- [ ] GetSettingsListUseCase.kt — operator fun invoke(): StateFlow<List<Settings>> from repository. suspend fun load() triggers loadAll. suspend fun refresh() triggers refresh.
- [ ] GetSettingsDetailUseCase.kt — suspend fun load(id: String) selects specific Settings from repository.
- [ ] DeleteSettingsUseCase.kt — suspend fun execute(id: String) deletes Settings from repository.
- [ ] SettingsListViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] SettingsListScreen.kt — Composable wrapping GenericListScreen from core/presentation/screens/. Uses koinViewModel() default parameter.
- [ ] SettingsDetailViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] SettingsDetailScreen.kt — Composable wrapping GenericDetailScreen from core/presentation/screens/. Uses koinViewModel() default parameter.
- [ ] SettingsSettingsViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] SettingsSettingsScreen.kt — Composable wrapping GenericSettingsScreen from core/presentation/screens/. Uses koinViewModel() default parameter.
- [ ] SettingsSearchViewModel.kt — THIN ViewModel extending BaseViewModel. Combines use case StateFlows with isLoading + error via combine().stateIn(). UiState sealed interface in same file.
- [ ] SettingsSearchScreen.kt — Composable wrapping GenericSearchScreen from core/presentation/screens/. Uses koinViewModel() default parameter.

## Phase 8: Feature: Dashboard
Dashboard/Home screen — aggregates data from other features

- [ ] GetDashboardOverviewUseCase.kt — Dashboard use case — injects MarketRepository, ProductRepository, FavoriteRepository, exposes combined StateFlow with DashboardOverview(stats, quickActions, recentItems). operator fun invoke() returns StateFlow. IMPORTANT: Define the DashboardOverview data class IN THIS FILE (it does not exist elsewhere). Do NOT create a Dashboard domain model, repository, or entity — Dashboard is an aggregation view, not a stored entity.
- [ ] DashboardViewModel.kt — THIN ViewModel extending BaseViewModel. Combines GetDashboardOverviewUseCase() flow with isLoading + error via combine().stateIn(). UiState sealed interface in same file. IMPORT DashboardOverview from the use case file — do NOT redefine it here.
- [ ] DashboardScreen.kt — Composable wrapping GenericDashboardScreen from core/presentation/screens/. Maps DashboardOverview to stats (2-3 StatCards), quickActions (2 max), and recentItems list.

## Phase 9: App Wiring
Create routes, register DI, wire AppOrchestrator in App.kt

- [ ] AppRoutes.kt — @Serializable route objects/data classes for all screens. Use @Serializable object for tab destinations, @Serializable data class for detail screens with parameters.
- [ ] [modify] AppModule.kt — Register all project-specific DAOs (single { get<AppDatabase>().xxxDao() }), repositories (singleOf), use cases (factoryOf), and ViewModels (viewModelOf). Do NOT re-register pre-built services from coreModule().
- [ ] [modify] App.kt — Wire AppOrchestrator inside AppTheme: define NavigationTab list, set homeStartDestination, and register ALL screen composables inside homeBuilder lambda. Without this, app shows blank screen.

## Cleanup: Unused Template Files
Files/directories to delete before code generation:

- composeApp/src/commonMain/kotlin/com/marketfinder/preview/
- composeApp/src/commonMain/kotlin/com/marketfinder/core/data/auth/
- composeApp/src/commonMain/kotlin/com/marketfinder/core/presentation/auth/
- composeApp/src/commonMain/kotlin/com/marketfinder/core/data/chat/
- composeApp/src/commonMain/kotlin/com/marketfinder/core/data/deeplink/
- composeApp/src/commonMain/kotlin/com/marketfinder/core/data/notifications/
- composeApp/src/commonMain/kotlin/com/marketfinder/core/data/firestore/
- composeApp/src/commonMain/kotlin/com/marketfinder/core/data/media/PlayerSource.kt
- composeApp/src/commonMain/kotlin/com/marketfinder/core/data/media/MediaResult.kt
- composeApp/src/commonMain/kotlin/com/marketfinder/core/data/media/MediaPickerType.kt
- composeApp/src/commonMain/kotlin/com/marketfinder/core/presentation/media/
- composeApp/src/commonMain/kotlin/com/marketfinder/core/data/calendar/
- composeApp/src/commonMain/kotlin/com/marketfinder/core/data/charts/
- composeApp/src/commonMain/kotlin/com/marketfinder/core/data/messaging/
- composeApp/src/commonMain/kotlin/com/marketfinder/core/data/payment/
- composeApp/src/commonMain/kotlin/com/marketfinder/core/presentation/screens/GenericGalleryScreen.kt
- composeApp/src/commonMain/kotlin/com/marketfinder/core/presentation/screens/GenericSubscriptionScreen.kt
- composeApp/src/commonMain/kotlin/com/marketfinder/core/presentation/screens/GenericTabScreen.kt
- composeApp/src/commonMain/kotlin/com/marketfinder/core/presentation/screens/GenericMediaPlayerScreen.kt
- composeApp/src/commonMain/kotlin/com/marketfinder/core/presentation/screens/GenericPaywallScreen.kt
- composeApp/src/commonMain/kotlin/com/marketfinder/core/presentation/screens/GenericChartScreen.kt
- composeApp/src/commonMain/kotlin/com/marketfinder/core/presentation/screens/GenericCalendarScreen.kt
- composeApp/src/commonMain/kotlin/com/marketfinder/core/presentation/screens/GenericConversationListScreen.kt
- composeApp/src/commonMain/kotlin/com/marketfinder/core/presentation/screens/GenericMapScreen.kt
- composeApp/src/commonMain/kotlin/com/marketfinder/core/presentation/screens/GenericNotificationScreen.kt
- composeApp/src/commonMain/kotlin/com/marketfinder/core/presentation/screens/GenericAuthScreen.kt
- composeApp/src/commonMain/kotlin/com/marketfinder/core/presentation/screens/GenericChatScreen.kt
