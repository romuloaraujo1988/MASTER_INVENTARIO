# Inventário de Código-Fonte e Métricas — SIHCP Mobile

**Diretório analisado:** `InventarioMobile/app/src/main`  
**Versão:** 2.29.8 (Build / versionCode 84)  
**Data da extração:** 13/11/2026 21:48:26

---

## 1. Resumo Quantitativo Real

- **Total de arquivos em `app/src/main`:** 561
- **Arquivos Kotlin (`.kt`) em `src/main`:** 359 (62,021 linhas)
- **Arquivos de teste Kotlin:** 51 testes unitários (`src/test`) + 4 instrumentados (`src/androidTest`) = 55 arquivos (**414 arquivos `.kt` no projeto**)
- **Arquivos XML (`.xml`) em `src/main`:** 201 (15,392 linhas, sendo 48 layouts de tela em `res/layout`)
- **Componentes Arquiteturais:** 26 Activities · 8 Fragments · 22 ViewModels · 38 UseCases · 12 Domain Repositories · 15 Data Repositories · 2 Workers · 12 Remote APIs · 6 Security · 7 Network

### 1.1 Distribuição por Extensão (`app/src/main`)

| Extensão | Quantidade de Arquivos | Linhas Totais |
|---|---:|---:|
| `.kt` | 359 | 62,021 |
| `.png` | 1 | 0 |
| `.xml` | 201 | 15,392 |

### 1.2 Distribuição dos Arquivos Kotlin por Pacote (`com.inventario.mobile`)

| Pacote | Arquivos `.kt` | Linhas de Código |
|---|---:|---:|
| `com.inventario.mobile.data` | 111 | 16,204 |
| `com.inventario.mobile.presentation` | 93 | 22,344 |
| `com.inventario.mobile.domain` | 78 | 7,177 |
| `com.inventario.mobile.utils` | 31 | 8,997 |
| `com.inventario.mobile.di` | 13 | 1,393 |
| `com.inventario.mobile.network` | 7 | 1,388 |
| `com.inventario.mobile.security` | 6 | 1,079 |
| `com.inventario.mobile.ui` | 6 | 1,115 |
| `com.inventario.mobile.sync` | 5 | 654 |
| `com.inventario.mobile.api` | 2 | 216 |
| `com.inventario.mobile.util` | 2 | 194 |
| `com.inventario.mobile.worker` | 2 | 512 |
| `com.inventario.mobile.(raiz)` | 1 | 343 |
| `com.inventario.mobile.auth` | 1 | 219 |
| `com.inventario.mobile.config` | 1 | 186 |

---

## 2. Lista Integral dos Arquivos Kotlin (`src/main/java/com/inventario/mobile`)

001. `InventarioMobileApplication.kt`
002. `api/PatrimonioApi.kt`
003. `api/SalaApi.kt`
004. `auth/TokenManager.kt`
005. `config/Android14NetworkConfig.kt`
006. `data/audit/AuditService.kt`
007. `data/cache/SalaCache.kt`
008. `data/cache/SearchCache.kt`
009. `data/export/CsvGenerator.kt`
010. `data/export/ExcelGenerator.kt`
011. `data/local/LocalDataManager.kt`
012. `data/local/dao/ChartDataModels.kt`
013. `data/local/dao/ColetaDao.kt`
014. `data/local/dao/DashboardDao.kt`
015. `data/local/dao/FotoReferenciaDao.kt`
016. `data/local/dao/HistoricoScanDao.kt`
017. `data/local/dao/LogColetaDao.kt`
018. `data/local/dao/PatrimonioDao.kt`
019. `data/local/dao/ResponsavelDao.kt`
020. `data/local/dao/SalaDao.kt`
021. `data/local/dao/SincronizacaoDao.kt`
022. `data/local/dao/SugestaoDescricaoDao.kt`
023. `data/local/dao/SyncLogDao.kt`
024. `data/local/database/AppDatabase.kt`
025. `data/local/dto/DashboardStatsDto.kt`
026. `data/local/entity/ColetaEntity.kt`
027. `data/local/entity/FotoReferenciaEntity.kt`
028. `data/local/entity/HistoricoScanEntity.kt`
029. `data/local/entity/LogColetaEntity.kt`
030. `data/local/entity/PatrimonioEntity.kt`
031. `data/local/entity/ResponsavelEntity.kt`
032. `data/local/entity/SalaComEstatisticasEntity.kt`
033. `data/local/entity/SalaEntity.kt`
034. `data/local/entity/SincronizacaoEntity.kt`
035. `data/local/entity/SugestaoDescricaoEntity.kt`
036. `data/local/entity/SyncLogEntity.kt`
037. `data/mapper/ColetaMapper.kt`
038. `data/mapper/DashboardMapper.kt`
039. `data/mapper/HistoricoScanMapper.kt`
040. `data/mapper/PatrimonioConsultaMapper.kt`
041. `data/mapper/PatrimonioDetalheMapper.kt`
042. `data/mapper/PatrimonioMapper.kt`
043. `data/mapper/SalaComProgressoMapper.kt`
044. `data/mapper/SalaMapper.kt`
045. `data/mapper/SugestaoDescricaoMapper.kt`
046. `data/migration/ColetaMigration.kt`
047. `data/model/ApiModels.kt`
048. `data/model/CacheServerStats.kt`
049. `data/model/Coleta.kt`
050. `data/model/EstadoPatrimonio.kt`
051. `data/model/Patrimonio.kt`
052. `data/model/Responsavel.kt`
053. `data/model/Sala.kt`
054. `data/model/Usuario.kt`
055. `data/observer/ConnectivityObserver.kt`
056. `data/paging/SalaPagingSource.kt`
057. `data/pdf/PdfGenerator.kt`
058. `data/realtime/ColetaEvento.kt`
059. `data/realtime/RealtimeColetasManager.kt`
060. `data/remote/api/ApiClient.kt`
061. `data/remote/api/ApiService.kt`
062. `data/remote/api/AuthApi.kt`
063. `data/remote/api/ColetaApi.kt`
064. `data/remote/api/DescricaoSugestaoApi.kt`
065. `data/remote/api/FotoColetaApi.kt`
066. `data/remote/api/FotoReferenciaApi.kt`
067. `data/remote/api/OfflineSyncApi.kt`
068. `data/remote/api/PatrimonioApi.kt`
069. `data/remote/api/PatrimonioConsultaApi.kt`
070. `data/remote/api/RelatorioFotoApi.kt`
071. `data/remote/api/SyncApi.kt`
072. `data/remote/dto/ApiResponse.kt`
073. `data/remote/dto/AuthDto.kt`
074. `data/remote/dto/ColetaDto.kt`
075. `data/remote/dto/ColetasAllResponse.kt`
076. `data/remote/dto/ColetasPorDiaDto.kt`
077. `data/remote/dto/DashboardStatsDto.kt`
078. `data/remote/dto/FotoReferenciaDTO.kt`
079. `data/remote/dto/MobileColetaBatchRequest.kt`
080. `data/remote/dto/MobileColetaRequest.kt`
081. `data/remote/dto/MobileColetaResponseDto.kt`
082. `data/remote/dto/MobileLoginResponseDto.kt`
083. `data/remote/dto/MobileOfflineDataDTO.kt`
084. `data/remote/dto/MobilePatrimonioDto.kt`
085. `data/remote/dto/PagedResponse.kt`
086. `data/remote/dto/PagedResponseDto.kt`
087. `data/remote/dto/PatrimonioConsultaDTO.kt`
088. `data/remote/dto/PatrimonioDetalheDTO.kt`
089. `data/remote/dto/PatrimonioDto.kt`
090. `data/remote/dto/ResponsavelDto.kt`
091. `data/remote/dto/SalaComProgressoDTO.kt`
092. `data/remote/dto/SalaDto.kt`
093. `data/remote/dto/SetorDto.kt`
094. `data/remote/dto/SugestaoDescricaoDto.kt`
095. `data/remote/dto/SyncDto.kt`
096. `data/remote/dto/UsuarioDto.kt`
097. `data/repository/AuthRepositoryImpl.kt`
098. `data/repository/ColetaRepositoryImpl.kt`
099. `data/repository/DashboardRepositoryImpl.kt`
100. `data/repository/ExportRepositoryImpl.kt`
101. `data/repository/FotoReferenciaRepositoryImpl.kt`
102. `data/repository/HistoricoScanRepositoryImpl.kt`
103. `data/repository/InventarioRepository.kt`
104. `data/repository/PatrimonioConsultaRepositoryImpl.kt`
105. `data/repository/PatrimonioRepositoryAdapter.kt`
106. `data/repository/PatrimonioRepositoryImpl.kt`
107. `data/repository/PdfRepositoryImpl.kt`
108. `data/repository/RankingsRepositoryImpl.kt`
109. `data/repository/SalaRepositoryImpl.kt`
110. `data/repository/SugestaoDescricaoRepositoryImpl.kt`
111. `data/repository/SyncRepository.kt`
112. `data/strategy/DataSourceStrategy.kt`
113. `data/strategy/DataSourceStrategyFactory.kt`
114. `data/strategy/LocalDataSourceStrategy.kt`
115. `data/strategy/RemoteDataSourceStrategy.kt`
116. `data/util/TextNormalizer.kt`
117. `di/ApiModule.kt`
118. `di/AppModule.kt`
119. `di/CacheModule.kt`
120. `di/DashboardModule.kt`
121. `di/DatabaseModule.kt`
122. `di/MapperModule.kt`
123. `di/NetworkModule.kt`
124. `di/NotificationModule.kt`
125. `di/PdfModule.kt`
126. `di/RepositoryModule.kt`
127. `di/SyncModule.kt`
128. `di/UseCaseModule.kt`
129. `di/UtilModule.kt`
130. `domain/model/Coleta.kt`
131. `domain/model/DashboardStats.kt`
132. `domain/model/DescricaoItem.kt`
133. `domain/model/EstatisticasSala.kt`
134. `domain/model/ExportConfig.kt`
135. `domain/model/ExportFilter.kt`
136. `domain/model/ExportFormat.kt`
137. `domain/model/ExportResult.kt`
138. `domain/model/FiltroColetaSala.kt`
139. `domain/model/HistoricoScan.kt`
140. `domain/model/OrigemSugestoes.kt`
141. `domain/model/Patrimonio.kt`
142. `domain/model/PatrimonioComColeta.kt`
143. `domain/model/PatrimonioConsulta.kt`
144. `domain/model/PatrimonioDetalhe.kt`
145. `domain/model/PdfSummary.kt`
146. `domain/model/RankingsAnalytics.kt`
147. `domain/model/RelatorioFotoExceptions.kt`
148. `domain/model/RelatorioFotoInfo.kt`
149. `domain/model/ResultadoSugestoes.kt`
150. `domain/model/Sala.kt`
151. `domain/model/SalaComProgresso.kt`
152. `domain/model/SearchFilter.kt`
153. `domain/model/Setor.kt`
154. `domain/model/StatusFiltro.kt`
155. `domain/model/SugestaoDescricao.kt`
156. `domain/model/Usuario.kt`
157. `domain/repository/AuthRepository.kt`
158. `domain/repository/ColetaRepository.kt`
159. `domain/repository/DashboardRepository.kt`
160. `domain/repository/ExportRepository.kt`
161. `domain/repository/FotoReferenciaRepository.kt`
162. `domain/repository/HistoricoScanRepository.kt`
163. `domain/repository/PatrimonioConsultaRepository.kt`
164. `domain/repository/PatrimonioRepository.kt`
165. `domain/repository/PdfRepository.kt`
166. `domain/repository/RankingsRepository.kt`
167. `domain/repository/SalaRepository.kt`
168. `domain/repository/SugestaoDescricaoRepository.kt`
169. `domain/usecase/AgruparColetasPorSalaUseCase.kt`
170. `domain/usecase/BaixarRelatorioFotoExcelUseCase.kt`
171. `domain/usecase/BaixarRelatorioFotoPdfUseCase.kt`
172. `domain/usecase/BuscarColetasComFallbackUseCase.kt`
173. `domain/usecase/BuscarDescricoesNaoColetadasUseCase.kt`
174. `domain/usecase/BuscarEstatisticasDashboardUseCase.kt`
175. `domain/usecase/BuscarEstatisticasSalaUseCase.kt`
176. `domain/usecase/BuscarEvolucaoColetasUseCase.kt`
177. `domain/usecase/BuscarHistoricoScansUseCase.kt`
178. `domain/usecase/BuscarInfoRelatorioFotoUseCase.kt`
179. `domain/usecase/BuscarPatrimonioUseCase.kt`
180. `domain/usecase/BuscarPatrimoniosPorDescricaoUseCase.kt`
181. `domain/usecase/BuscarPatrimoniosPorSalaParaExportacaoUseCase.kt`
182. `domain/usecase/BuscarPatrimoniosPorSalaUseCase.kt`
183. `domain/usecase/BuscarPatrimoniosUseCase.kt`
184. `domain/usecase/BuscarSalasComColetasUseCase.kt`
185. `domain/usecase/BuscarSalasComProgressoUseCase.kt`
186. `domain/usecase/BuscarSalasParaExportacaoUseCase.kt`
187. `domain/usecase/BuscarSugestoesDescricaoUseCase.kt`
188. `domain/usecase/ExcluirColetaPendenteUseCase.kt`
189. `domain/usecase/FiltrarColetasUseCase.kt`
190. `domain/usecase/GerarRelatorioPdfUseCase.kt`
191. `domain/usecase/GerarRelatorioUseCase.kt`
192. `domain/usecase/LimparCacheDeOutrosInventariosUseCase.kt`
193. `domain/usecase/MarcarPatrimonioColetadoLocalmenteUseCase.kt`
194. `domain/usecase/ObterDetalhePatrimonioUseCase.kt`
195. `domain/usecase/ObterUsuarioAtualUseCase.kt`
196. `domain/usecase/ReenviarColetaUseCase.kt`
197. `domain/usecase/RegistrarAcessoPatrimonioUseCase.kt`
198. `domain/usecase/RegistrarColetaPorDescricaoUseCase.kt`
199. `domain/usecase/RegistrarColetaUseCase.kt`
200. `domain/usecase/RenovarTokenComBiometriaUseCase.kt`
201. `domain/usecase/SincronizarColetasDoServidorUseCase.kt`
202. `domain/usecase/SincronizarColetasPendentesUseCase.kt`
203. `domain/usecase/SincronizarDadosUseCase.kt`
204. `domain/usecase/SincronizarFotosReferenciaUseCase.kt`
205. `domain/usecase/SincronizarSalasUseCase.kt`
206. `domain/usecase/SincronizarSugestoesDescricaoUseCase.kt`
207. `domain/validator/ColetaValidator.kt`
208. `network/ConnectionStateManager.kt`
209. `network/ConnectivityMonitor.kt`
210. `network/DeviceInfoInterceptor.kt`
211. `network/NetworkQualityMonitor.kt`
212. `network/OfflineFallbackInterceptor.kt`
213. `network/RefreshTokenInterceptor.kt`
214. `network/TimeoutFallbackInterceptor.kt`
215. `presentation/activity/NetworkDiagnosticActivity.kt`
216. `presentation/adapter/PatrimonioSearchAdapter.kt`
217. `presentation/adapter/SalaAdapter.kt`
218. `presentation/charts/ChartDataProvider.kt`
219. `presentation/charts/ChartHelper.kt`
220. `presentation/charts/ChartsFragment.kt`
221. `presentation/charts/ChartsViewModel.kt`
222. `presentation/coleta/ColetaViewModelClean.kt`
223. `presentation/coleta/CollectionAdapter.kt`
224. `presentation/coleta/CollectionViewActivity.kt`
225. `presentation/coleta/ItemSemEtiquetaActivity.kt`
226. `presentation/coleta/ItemSemEtiquetaViewModel.kt`
227. `presentation/coleta/LiveColetaMonitorViewModel.kt`
228. `presentation/coleta/ManualCollectionActivity.kt`
229. `presentation/coleta/ManualCollectionViewModel.kt`
230. `presentation/coleta/adapter/SugestaoDescricaoAdapter.kt`
231. `presentation/coleta/state/SugestaoDescricaoState.kt`
232. `presentation/coletas/ColetasAdapter.kt`
233. `presentation/coletas/ColetasState.kt`
234. `presentation/coletas/ColetasUnificadaActivity.kt`
235. `presentation/coletas/ColetasUnificadaState.kt`
236. `presentation/coletas/ColetasUnificadaViewModel.kt`
237. `presentation/components/PhotoCaptureHelper.kt`
238. `presentation/dashboard/DashboardFragment.kt`
239. `presentation/dashboard/DashboardPresentationModels.kt`
240. `presentation/dashboard/DashboardUiMapper.kt`
241. `presentation/dashboard/DashboardViewModelClean.kt`
242. `presentation/descricao/DescricaoAdapter.kt`
243. `presentation/descricao/DescricaoSelectionActivity.kt`
244. `presentation/descricao/DescricaoSelectionViewModelClean.kt`
245. `presentation/detail/PatrimonioDetailActivity.kt`
246. `presentation/dialog/EstadoPatrimonioDialog.kt`
247. `presentation/export/SalaFilterAdapter.kt`
248. `presentation/filtros/FiltrosActivity.kt`
249. `presentation/filtros/FiltrosViewModel.kt`
250. `presentation/filtros/FiltrosViewModelFactory.kt`
251. `presentation/historico/HistoricoScansActivity.kt`
252. `presentation/historico/HistoricoScansAdapter.kt`
253. `presentation/historico/HistoricoScansState.kt`
254. `presentation/historico/HistoricoScansViewModel.kt`
255. `presentation/inventario/InventarioActivity.kt`
256. `presentation/inventario/InventarioPagerAdapter.kt`
257. `presentation/inventario/InventarioPorResponsavelFragment.kt`
258. `presentation/inventario/InventarioPorSalaFragment.kt`
259. `presentation/inventario/InventarioPorSalaViewModel.kt`
260. `presentation/inventario/InventarioTabsActivity.kt`
261. `presentation/inventario/InventarioViewModel.kt`
262. `presentation/inventario/PatrimonioAdapter.kt`
263. `presentation/login/LoginActivity.kt`
264. `presentation/login/LoginViewModel.kt`
265. `presentation/login/PinLoginDialog.kt`
266. `presentation/login/PinSetupDialog.kt`
267. `presentation/main/MainActivity.kt`
268. `presentation/sala/SalaSelectionActivity.kt`
269. `presentation/sala/SalaSelectionViewModel.kt`
270. `presentation/sala/SalaSelectionViewModelFactory.kt`
271. `presentation/scanner/CustomCaptureActivity.kt`
272. `presentation/scanner/ScannerActivity.kt`
273. `presentation/scanner/ScannerViewModel.kt`
274. `presentation/scanner/ScannerViewModelFactory.kt`
275. `presentation/scanner/navigation/ColetaShortcut.kt`
276. `presentation/scanner/navigation/ColetaShortcutNavigator.kt`
277. `presentation/scanner/navigation/SalaContext.kt`
278. `presentation/scanner/navigation/ScannedItemRef.kt`
279. `presentation/scanner/navigation/ShortcutNavigationPlan.kt`
280. `presentation/search/QuickSearchActivity.kt`
281. `presentation/settings/SettingsActivity.kt`
282. `presentation/settings/SettingsViewModel.kt`
283. `presentation/settings/SettingsViewModelFactory.kt`
284. `presentation/setup/InitialSetupActivity.kt`
285. `presentation/state/ColetaState.kt`
286. `presentation/state/DescricaoState.kt`
287. `presentation/state/InventarioPorSalaState.kt`
288. `presentation/state/PatrimonioDetailState.kt`
289. `presentation/state/QuickSearchState.kt`
290. `presentation/state/RelatorioFotoState.kt`
291. `presentation/statistics/ExportFragment.kt`
292. `presentation/statistics/ExportState.kt`
293. `presentation/statistics/ExportViewModel.kt`
294. `presentation/statistics/OverviewFragment.kt`
295. `presentation/statistics/RankingsFragment.kt`
296. `presentation/statistics/RankingsViewModel.kt`
297. `presentation/statistics/SetorPendenteAdapter.kt`
298. `presentation/statistics/StatisticsActivity.kt`
299. `presentation/statistics/StatisticsPagerAdapter.kt`
300. `presentation/statistics/TopColetorAdapter.kt`
301. `presentation/sync/PendingCollectionsActivity.kt`
302. `presentation/sync/PendingCollectionsAdapter.kt`
303. `presentation/sync/PendingCollectionsViewModel.kt`
304. `presentation/sync/SyncActivity.kt`
305. `presentation/sync/SyncViewModel.kt`
306. `presentation/viewmodel/PatrimonioDetailViewModel.kt`
307. `presentation/viewmodel/QuickSearchViewModel.kt`
308. `security/BiometricManager.kt`
309. `security/InputValidator.kt`
310. `security/KeystoreRecoveryActivity.kt`
311. `security/LogSanitizer.kt`
312. `security/PinAuthManager.kt`
313. `security/SqlCipherKeyManager.kt`
314. `sync/NetworkConnectivityObserver.kt`
315. `sync/SyncError.kt`
316. `sync/SyncLogger.kt`
317. `sync/SyncManager.kt`
318. `sync/SyncScheduler.kt`
319. `ui/ConnectionStatusBar.kt`
320. `ui/base/BaseOfflineActivity.kt`
321. `ui/base/BaseOfflineFragment.kt`
322. `ui/coleta/ColetaActivity.kt`
323. `ui/components/OfflineIndicator.kt`
324. `ui/splash/SplashActivity.kt`
325. `util/NetworkChecker.kt`
326. `util/NetworkUtils.kt`
327. `utils/Android14CameraHelper.kt`
328. `utils/CameraUtils.kt`
329. `utils/DateUtils.kt`
330. `utils/ErrorMapper.kt`
331. `utils/FeatureFlags.kt`
332. `utils/FileUtils.kt`
333. `utils/FotoReferenciaHelper.kt`
334. `utils/FotoTipo.kt`
335. `utils/LegacyPhotoMigrator.kt`
336. `utils/MetricsHelper.kt`
337. `utils/NavigationHelper.kt`
338. `utils/NetworkLocationManager.kt`
339. `utils/NetworkMonitor.kt`
340. `utils/NetworkUtils.kt`
341. `utils/NotificationUtils.kt`
342. `utils/OfflineNotificationManager.kt`
343. `utils/PermissionHelper.kt`
344. `utils/PhotoHelper.kt`
345. `utils/PreferencesManager.kt`
346. `utils/QRCodeUtils.kt`
347. `utils/ScanMetricsTracker.kt`
348. `utils/ServerConfigManager.kt`
349. `utils/ServerValidator.kt`
350. `utils/SessionManager.kt`
351. `utils/SoundUtils.kt`
352. `utils/ThemeHelper.kt`
353. `utils/TipoPatrimonioIconMapper.kt`
354. `utils/ValidationUtils.kt`
355. `utils/VibrationHelper.kt`
356. `utils/VoiceCommandParser.kt`
357. `utils/VoiceSearchManager.kt`
358. `worker/ColetaSyncWorker.kt`
359. `worker/PhotoSyncWorker.kt`
