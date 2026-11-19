# Inventário de Código-Fonte e Métricas — SIHCP Mobile

**Diretório analisado:** `InventarioMobile/app/src/main`  
**Versão:** 2.29.8 (Build / versionCode 84)  
**Data da extração:** 19/11/2026 11:30:42

---

## 1. Resumo Quantitativo Real

- **Total de arquivos em `app/src/main`:** 528
- **Arquivos Kotlin (`.kt`) em `src/main`:** 346 (60,621 linhas)
- **Arquivos de teste Kotlin:** 46 testes unitários (`src/test`) + 4 instrumentados (`src/androidTest`) = 50 arquivos (**396 arquivos `.kt` no projeto**)
- **Arquivos XML (`.xml`) em `src/main`:** 181 (15,083 linhas, sendo 48 layouts de tela em `res/layout`)
- **Componentes Arquiteturais:** 26 Activities · 8 Fragments · 22 ViewModels · 34 UseCases · 11 Domain Repositories · 14 Data Repositories · 2 Workers · 11 Remote APIs · 5 Security · 7 Network

### 1.1 Distribuição por Extensão (`app/src/main`)

| Extensão | Quantidade de Arquivos | Linhas Totais |
|---|---:|---:|
| `.kt` | 346 | 60,621 |
| `.png` | 1 | 0 |
| `.xml` | 181 | 15,083 |

### 1.2 Distribuição dos Arquivos Kotlin por Pacote (`com.inventario.mobile`)

| Pacote | Arquivos `.kt` | Linhas de Código |
|---|---:|---:|
| `com.inventario.mobile.data` | 107 | 15,807 |
| `com.inventario.mobile.presentation` | 92 | 22,289 |
| `com.inventario.mobile.domain` | 73 | 6,573 |
| `com.inventario.mobile.utils` | 32 | 9,093 |
| `com.inventario.mobile.di` | 13 | 1,341 |
| `com.inventario.mobile.network` | 7 | 1,388 |
| `com.inventario.mobile.ui` | 6 | 1,115 |
| `com.inventario.mobile.security` | 5 | 1,017 |
| `com.inventario.mobile.sync` | 4 | 522 |
| `com.inventario.mobile.api` | 2 | 216 |
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
042. `data/mapper/SalaComProgressoMapper.kt`
043. `data/mapper/SugestaoDescricaoMapper.kt`
044. `data/migration/ColetaMigration.kt`
045. `data/model/ApiModels.kt`
046. `data/model/CacheServerStats.kt`
047. `data/model/Coleta.kt`
048. `data/model/EstadoPatrimonio.kt`
049. `data/model/Patrimonio.kt`
050. `data/model/Responsavel.kt`
051. `data/model/Sala.kt`
052. `data/model/Usuario.kt`
053. `data/observer/ConnectivityObserver.kt`
054. `data/paging/SalaPagingSource.kt`
055. `data/pdf/PdfGenerator.kt`
056. `data/realtime/ColetaEvento.kt`
057. `data/realtime/RealtimeColetasManager.kt`
058. `data/remote/api/ApiClient.kt`
059. `data/remote/api/ApiService.kt`
060. `data/remote/api/AuthApi.kt`
061. `data/remote/api/ColetaApi.kt`
062. `data/remote/api/DescricaoSugestaoApi.kt`
063. `data/remote/api/FotoColetaApi.kt`
064. `data/remote/api/FotoReferenciaApi.kt`
065. `data/remote/api/OfflineSyncApi.kt`
066. `data/remote/api/PatrimonioApi.kt`
067. `data/remote/api/PatrimonioConsultaApi.kt`
068. `data/remote/api/RelatorioFotoApi.kt`
069. `data/remote/dto/ApiResponse.kt`
070. `data/remote/dto/AuthDto.kt`
071. `data/remote/dto/ColetaDto.kt`
072. `data/remote/dto/ColetasAllResponse.kt`
073. `data/remote/dto/ColetasPorDiaDto.kt`
074. `data/remote/dto/DashboardStatsDto.kt`
075. `data/remote/dto/FotoReferenciaDTO.kt`
076. `data/remote/dto/MobileColetaBatchRequest.kt`
077. `data/remote/dto/MobileColetaRequest.kt`
078. `data/remote/dto/MobileColetaResponseDto.kt`
079. `data/remote/dto/MobileLoginResponseDto.kt`
080. `data/remote/dto/MobileOfflineDataDTO.kt`
081. `data/remote/dto/MobilePatrimonioDto.kt`
082. `data/remote/dto/PagedResponse.kt`
083. `data/remote/dto/PagedResponseDto.kt`
084. `data/remote/dto/PatrimonioConsultaDTO.kt`
085. `data/remote/dto/PatrimonioDetalheDTO.kt`
086. `data/remote/dto/PatrimonioDto.kt`
087. `data/remote/dto/ResponsavelDto.kt`
088. `data/remote/dto/SalaComProgressoDTO.kt`
089. `data/remote/dto/SalaDto.kt`
090. `data/remote/dto/SetorDto.kt`
091. `data/remote/dto/SugestaoDescricaoDto.kt`
092. `data/remote/dto/SyncDto.kt`
093. `data/remote/dto/UsuarioDto.kt`
094. `data/repository/AuthRepositoryImpl.kt`
095. `data/repository/ColetaRepositoryImpl.kt`
096. `data/repository/DashboardRepositoryImpl.kt`
097. `data/repository/ExportRepositoryImpl.kt`
098. `data/repository/FotoReferenciaRepositoryImpl.kt`
099. `data/repository/HistoricoScanRepositoryImpl.kt`
100. `data/repository/InventarioRepository.kt`
101. `data/repository/PatrimonioConsultaRepositoryImpl.kt`
102. `data/repository/PatrimonioRepositoryAdapter.kt`
103. `data/repository/PatrimonioRepositoryImpl.kt`
104. `data/repository/RankingsRepositoryImpl.kt`
105. `data/repository/SalaRepositoryImpl.kt`
106. `data/repository/SugestaoDescricaoRepositoryImpl.kt`
107. `data/repository/SyncRepository.kt`
108. `data/strategy/DataSourceStrategy.kt`
109. `data/strategy/DataSourceStrategyFactory.kt`
110. `data/strategy/LocalDataSourceStrategy.kt`
111. `data/strategy/RemoteDataSourceStrategy.kt`
112. `data/util/TextNormalizer.kt`
113. `di/ApiModule.kt`
114. `di/AppModule.kt`
115. `di/CacheModule.kt`
116. `di/DashboardModule.kt`
117. `di/DatabaseModule.kt`
118. `di/MapperModule.kt`
119. `di/NetworkModule.kt`
120. `di/NotificationModule.kt`
121. `di/PdfModule.kt`
122. `di/RepositoryModule.kt`
123. `di/SyncModule.kt`
124. `di/UseCaseModule.kt`
125. `di/UtilModule.kt`
126. `domain/model/Coleta.kt`
127. `domain/model/DashboardStats.kt`
128. `domain/model/DescricaoItem.kt`
129. `domain/model/EstatisticasSala.kt`
130. `domain/model/ExportConfig.kt`
131. `domain/model/ExportFilter.kt`
132. `domain/model/ExportFormat.kt`
133. `domain/model/ExportResult.kt`
134. `domain/model/FiltroColetaSala.kt`
135. `domain/model/HistoricoScan.kt`
136. `domain/model/OrigemSugestoes.kt`
137. `domain/model/Patrimonio.kt`
138. `domain/model/PatrimonioComColeta.kt`
139. `domain/model/PatrimonioConsulta.kt`
140. `domain/model/PatrimonioDetalhe.kt`
141. `domain/model/PdfSummary.kt`
142. `domain/model/RankingsAnalytics.kt`
143. `domain/model/RelatorioFotoExceptions.kt`
144. `domain/model/RelatorioFotoInfo.kt`
145. `domain/model/ResultadoSugestoes.kt`
146. `domain/model/Sala.kt`
147. `domain/model/SalaComProgresso.kt`
148. `domain/model/SearchFilter.kt`
149. `domain/model/Setor.kt`
150. `domain/model/StatusFiltro.kt`
151. `domain/model/SugestaoDescricao.kt`
152. `domain/model/Usuario.kt`
153. `domain/repository/AuthRepository.kt`
154. `domain/repository/ColetaRepository.kt`
155. `domain/repository/DashboardRepository.kt`
156. `domain/repository/ExportRepository.kt`
157. `domain/repository/FotoReferenciaRepository.kt`
158. `domain/repository/HistoricoScanRepository.kt`
159. `domain/repository/PatrimonioConsultaRepository.kt`
160. `domain/repository/PatrimonioRepository.kt`
161. `domain/repository/RankingsRepository.kt`
162. `domain/repository/SalaRepository.kt`
163. `domain/repository/SugestaoDescricaoRepository.kt`
164. `domain/usecase/AgruparColetasPorSalaUseCase.kt`
165. `domain/usecase/BaixarRelatorioFotoExcelUseCase.kt`
166. `domain/usecase/BaixarRelatorioFotoPdfUseCase.kt`
167. `domain/usecase/BuscarColetasComFallbackUseCase.kt`
168. `domain/usecase/BuscarDescricoesNaoColetadasUseCase.kt`
169. `domain/usecase/BuscarEstatisticasDashboardUseCase.kt`
170. `domain/usecase/BuscarEstatisticasSalaUseCase.kt`
171. `domain/usecase/BuscarEvolucaoColetasUseCase.kt`
172. `domain/usecase/BuscarHistoricoScansUseCase.kt`
173. `domain/usecase/BuscarInfoRelatorioFotoUseCase.kt`
174. `domain/usecase/BuscarPatrimonioUseCase.kt`
175. `domain/usecase/BuscarPatrimoniosPorDescricaoUseCase.kt`
176. `domain/usecase/BuscarPatrimoniosPorSalaUseCase.kt`
177. `domain/usecase/BuscarPatrimoniosUseCase.kt`
178. `domain/usecase/BuscarSalasComColetasUseCase.kt`
179. `domain/usecase/BuscarSalasComProgressoUseCase.kt`
180. `domain/usecase/BuscarSalasParaExportacaoUseCase.kt`
181. `domain/usecase/BuscarSugestoesDescricaoUseCase.kt`
182. `domain/usecase/ExcluirColetaPendenteUseCase.kt`
183. `domain/usecase/FiltrarColetasUseCase.kt`
184. `domain/usecase/GerarRelatorioUseCase.kt`
185. `domain/usecase/LimparCacheDeOutrosInventariosUseCase.kt`
186. `domain/usecase/MarcarPatrimonioColetadoLocalmenteUseCase.kt`
187. `domain/usecase/ObterDetalhePatrimonioUseCase.kt`
188. `domain/usecase/ObterUsuarioAtualUseCase.kt`
189. `domain/usecase/ReenviarColetaUseCase.kt`
190. `domain/usecase/RegistrarAcessoPatrimonioUseCase.kt`
191. `domain/usecase/RegistrarColetaPorDescricaoUseCase.kt`
192. `domain/usecase/RegistrarColetaUseCase.kt`
193. `domain/usecase/RenovarTokenComBiometriaUseCase.kt`
194. `domain/usecase/SincronizarColetasPendentesUseCase.kt`
195. `domain/usecase/SincronizarDadosUseCase.kt`
196. `domain/usecase/SincronizarFotosReferenciaUseCase.kt`
197. `domain/usecase/SincronizarSugestoesDescricaoUseCase.kt`
198. `domain/validator/ColetaValidator.kt`
199. `network/ConnectionStateManager.kt`
200. `network/ConnectivityMonitor.kt`
201. `network/DeviceInfoInterceptor.kt`
202. `network/NetworkQualityMonitor.kt`
203. `network/OfflineFallbackInterceptor.kt`
204. `network/RefreshTokenInterceptor.kt`
205. `network/TimeoutFallbackInterceptor.kt`
206. `presentation/activity/NetworkDiagnosticActivity.kt`
207. `presentation/adapter/PatrimonioSearchAdapter.kt`
208. `presentation/adapter/SalaAdapter.kt`
209. `presentation/charts/ChartDataProvider.kt`
210. `presentation/charts/ChartHelper.kt`
211. `presentation/charts/ChartsFragment.kt`
212. `presentation/charts/ChartsViewModel.kt`
213. `presentation/coleta/ColetaViewModelClean.kt`
214. `presentation/coleta/CollectionAdapter.kt`
215. `presentation/coleta/CollectionViewActivity.kt`
216. `presentation/coleta/ItemSemEtiquetaActivity.kt`
217. `presentation/coleta/ItemSemEtiquetaViewModel.kt`
218. `presentation/coleta/LiveColetaMonitorViewModel.kt`
219. `presentation/coleta/ManualCollectionActivity.kt`
220. `presentation/coleta/ManualCollectionViewModel.kt`
221. `presentation/coleta/adapter/SugestaoDescricaoAdapter.kt`
222. `presentation/coleta/state/SugestaoDescricaoState.kt`
223. `presentation/coletas/ColetasAdapter.kt`
224. `presentation/coletas/ColetasUnificadaActivity.kt`
225. `presentation/coletas/ColetasUnificadaState.kt`
226. `presentation/coletas/ColetasUnificadaViewModel.kt`
227. `presentation/components/PhotoCaptureHelper.kt`
228. `presentation/dashboard/DashboardFragment.kt`
229. `presentation/dashboard/DashboardPresentationModels.kt`
230. `presentation/dashboard/DashboardUiMapper.kt`
231. `presentation/dashboard/DashboardViewModelClean.kt`
232. `presentation/descricao/DescricaoAdapter.kt`
233. `presentation/descricao/DescricaoSelectionActivity.kt`
234. `presentation/descricao/DescricaoSelectionViewModelClean.kt`
235. `presentation/detail/PatrimonioDetailActivity.kt`
236. `presentation/dialog/EstadoPatrimonioDialog.kt`
237. `presentation/export/SalaFilterAdapter.kt`
238. `presentation/filtros/FiltrosActivity.kt`
239. `presentation/filtros/FiltrosViewModel.kt`
240. `presentation/filtros/FiltrosViewModelFactory.kt`
241. `presentation/historico/HistoricoScansActivity.kt`
242. `presentation/historico/HistoricoScansAdapter.kt`
243. `presentation/historico/HistoricoScansState.kt`
244. `presentation/historico/HistoricoScansViewModel.kt`
245. `presentation/inventario/InventarioActivity.kt`
246. `presentation/inventario/InventarioPagerAdapter.kt`
247. `presentation/inventario/InventarioPorResponsavelFragment.kt`
248. `presentation/inventario/InventarioPorSalaFragment.kt`
249. `presentation/inventario/InventarioPorSalaViewModel.kt`
250. `presentation/inventario/InventarioTabsActivity.kt`
251. `presentation/inventario/InventarioViewModel.kt`
252. `presentation/inventario/PatrimonioAdapter.kt`
253. `presentation/login/LoginActivity.kt`
254. `presentation/login/LoginViewModel.kt`
255. `presentation/login/PinLoginDialog.kt`
256. `presentation/login/PinSetupDialog.kt`
257. `presentation/main/MainActivity.kt`
258. `presentation/sala/SalaSelectionActivity.kt`
259. `presentation/sala/SalaSelectionViewModel.kt`
260. `presentation/sala/SalaSelectionViewModelFactory.kt`
261. `presentation/scanner/CustomCaptureActivity.kt`
262. `presentation/scanner/ScannerActivity.kt`
263. `presentation/scanner/ScannerViewModel.kt`
264. `presentation/scanner/ScannerViewModelFactory.kt`
265. `presentation/scanner/navigation/ColetaShortcut.kt`
266. `presentation/scanner/navigation/ColetaShortcutNavigator.kt`
267. `presentation/scanner/navigation/SalaContext.kt`
268. `presentation/scanner/navigation/ScannedItemRef.kt`
269. `presentation/scanner/navigation/ShortcutNavigationPlan.kt`
270. `presentation/search/QuickSearchActivity.kt`
271. `presentation/settings/SettingsActivity.kt`
272. `presentation/settings/SettingsViewModel.kt`
273. `presentation/settings/SettingsViewModelFactory.kt`
274. `presentation/setup/InitialSetupActivity.kt`
275. `presentation/state/ColetaState.kt`
276. `presentation/state/DescricaoState.kt`
277. `presentation/state/InventarioPorSalaState.kt`
278. `presentation/state/PatrimonioDetailState.kt`
279. `presentation/state/QuickSearchState.kt`
280. `presentation/state/RelatorioFotoState.kt`
281. `presentation/statistics/ExportFragment.kt`
282. `presentation/statistics/ExportState.kt`
283. `presentation/statistics/ExportViewModel.kt`
284. `presentation/statistics/OverviewFragment.kt`
285. `presentation/statistics/RankingsFragment.kt`
286. `presentation/statistics/RankingsViewModel.kt`
287. `presentation/statistics/SetorPendenteAdapter.kt`
288. `presentation/statistics/StatisticsActivity.kt`
289. `presentation/statistics/StatisticsPagerAdapter.kt`
290. `presentation/statistics/TopColetorAdapter.kt`
291. `presentation/sync/PendingCollectionsActivity.kt`
292. `presentation/sync/PendingCollectionsAdapter.kt`
293. `presentation/sync/PendingCollectionsViewModel.kt`
294. `presentation/sync/SyncActivity.kt`
295. `presentation/sync/SyncViewModel.kt`
296. `presentation/viewmodel/PatrimonioDetailViewModel.kt`
297. `presentation/viewmodel/QuickSearchViewModel.kt`
298. `security/BiometricManager.kt`
299. `security/KeystoreRecoveryActivity.kt`
300. `security/LogSanitizer.kt`
301. `security/PinAuthManager.kt`
302. `security/SqlCipherKeyManager.kt`
303. `sync/NetworkConnectivityObserver.kt`
304. `sync/SyncError.kt`
305. `sync/SyncManager.kt`
306. `sync/SyncScheduler.kt`
307. `ui/ConnectionStatusBar.kt`
308. `ui/base/BaseOfflineActivity.kt`
309. `ui/base/BaseOfflineFragment.kt`
310. `ui/coleta/ColetaActivity.kt`
311. `ui/components/OfflineIndicator.kt`
312. `ui/splash/SplashActivity.kt`
313. `utils/Android14CameraHelper.kt`
314. `utils/CameraUtils.kt`
315. `utils/DateUtils.kt`
316. `utils/ErrorMapper.kt`
317. `utils/FeatureFlags.kt`
318. `utils/FileUtils.kt`
319. `utils/FotoReferenciaHelper.kt`
320. `utils/FotoTipo.kt`
321. `utils/LegacyPhotoMigrator.kt`
322. `utils/MetricsHelper.kt`
323. `utils/NavigationHelper.kt`
324. `utils/NetworkChecker.kt`
325. `utils/NetworkLocationManager.kt`
326. `utils/NetworkMonitor.kt`
327. `utils/NetworkUtils.kt`
328. `utils/NotificationUtils.kt`
329. `utils/OfflineNotificationManager.kt`
330. `utils/PermissionHelper.kt`
331. `utils/PhotoHelper.kt`
332. `utils/PreferencesManager.kt`
333. `utils/QRCodeUtils.kt`
334. `utils/ScanMetricsTracker.kt`
335. `utils/ServerConfigManager.kt`
336. `utils/ServerValidator.kt`
337. `utils/SessionManager.kt`
338. `utils/SoundUtils.kt`
339. `utils/ThemeHelper.kt`
340. `utils/TipoPatrimonioIconMapper.kt`
341. `utils/ValidationUtils.kt`
342. `utils/VibrationHelper.kt`
343. `utils/VoiceCommandParser.kt`
344. `utils/VoiceSearchManager.kt`
345. `worker/ColetaSyncWorker.kt`
346. `worker/PhotoSyncWorker.kt`
