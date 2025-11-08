# Implementation Plan - Analytics Básico

## Task Overview

Este plano implementa o módulo de Analytics Básico em fases incrementais, começando pela infraestrutura backend, seguido pela API REST, interface desktop e finalmente mobile. Cada tarefa é autocontida e testável.

---

## Phase 1: Backend Infrastructure

### - [ ] 1. Create Analytics DTOs
Create data transfer objects for analytics responses
- Create `DashboardMetricsDTO.java` with all dashboard metrics fields
- Create `ColetasPorDiaDTO.java` for time series data
- Create `DistribuicaoSetorDTO.java` for sector distribution
- Create `PerformanceColetorDTO.java` for collector performance
- Create `DivergenciasAnalysisDTO.java` for divergence analysis
- Create `ValorPatrimonialDTO.java` for asset value analysis
- Add Jackson annotations for JSON serialization
- _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1, 11.1_

### - [ ] 2. Create Analytics DAO
Implement data access layer with optimized queries
- Create `AnalyticsDAO.java` class
- Implement `getDashboardMetrics(Integer inventarioId)` with aggregation query
- Implement `getColetasPorDia(Integer inventarioId, LocalDate inicio, LocalDate fim)`
- Implement `getDistribuicaoPorSetor(Integer inventarioId)`
- Implement `getPerformanceColetores(Integer inventarioId)`
- Implement `getDivergenciasAnalysis(Integer inventarioId)`
- Implement `getValorPatrimonial(Integer inventarioId)`
- Add connection pooling and prepared statements
- _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1, 11.1_

### - [ ] 3. Create database optimizations
Add materialized views and indexes for performance

- [ ] 3.1 Create materialized view for dashboard metrics
  - Write SQL migration `V1.3.0__analytics_materialized_views.sql`
  - Create `mv_dashboard_metrics` view with aggregations
  - Add refresh strategy (manual or automatic)
  - _Requirements: 1.1, Technical Requirements (Performance)_

- [ ] 3.2 Create indexes for analytics queries
  - Add index on `tabela_coleta(id_inventario, data_coleta)`
  - Add index on `tabela_coleta(id_coletor, data_coleta)`
  - Add index on `tabela_patrimonio(id_setor, valor)`
  - Add index on `tabela_coleta(id_inventario, divergencia)` for divergences
  - _Requirements: Technical Requirements (Performance)_

### - [ ] 4. Implement Cache Service
Create caching layer for analytics data

- [ ] 4.1 Create CacheService class
  - Implement in-memory cache using Caffeine
  - Add `getOrCompute()` method with TTL support
  - Add `invalidate()` and `invalidatePattern()` methods
  - Configure cache sizes and eviction policies
  - _Requirements: Technical Requirements (Performance)_

- [ ] 4.2 Integrate cache with Analytics DAO
  - Wrap DAO calls with cache layer
  - Define cache keys pattern
  - Set appropriate TTLs for each metric type
  - Add cache warming on application startup
  - _Requirements: Technical Requirements (Performance)_

### - [ ] 5. Implement Analytics Service
Create business logic layer for analytics

- [ ] 5.1 Create AnalyticsService class
  - Implement `getDashboardMetrics(Integer inventarioId)`
  - Implement `getColetasPorDia()` with date range validation
  - Implement `getDistribuicaoPorSetor()` with percentage calculations
  - Implement `getPerformanceColetores()` with averages
  - Implement `getAnalyseDivergencias()` with categorization
  - Implement `getAnalyseValor()` with totals and distributions
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1, 11.1_

- [ ] 5.2 Add comparison logic
  - Implement `compararInventarios(List<Integer> inventarioIds)`
  - Calculate deltas and percentage changes
  - Identify improvements and regressions
  - _Requirements: 10.1, 10.2, 10.3, 10.4_

- [ ] 5.3 Add alert generation logic
  - Implement alert rules (inventory completion, divergences, inactive collectors)
  - Create `AlertDTO` and `AlertService`
  - Store alerts in database or cache
  - _Requirements: 9.1, 9.2, 9.3, 9.4_

---

## Phase 2: REST API

### - [ ] 6. Create Analytics REST Controller
Implement REST endpoints for analytics

- [ ] 6.1 Create AnalyticsController class
  - Add `@RestController` and `@RequestMapping("/api/analytics")`
  - Implement `GET /dashboard/{inventarioId}` endpoint
  - Implement `GET /coletas-evolucao` with query params
  - Implement `GET /distribuicao-setor/{inventarioId}`
  - Implement `GET /performance-coletores/{inventarioId}`
  - Implement `GET /divergencias/{inventarioId}`
  - Implement `GET /valor-patrimonial/{inventarioId}`
  - Add proper error handling and validation
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1, 11.1_

- [ ] 6.2 Add security and authorization
  - Add `@PreAuthorize` annotations for role-based access
  - Verify user has access to requested inventario
  - Add audit logging for sensitive operations
  - _Requirements: Technical Requirements (Security)_

- [ ] 6.3 Add comparison endpoint
  - Implement `POST /comparar-inventarios` endpoint
  - Accept list of inventario IDs in request body
  - Return comparison DTO with deltas
  - _Requirements: 10.1, 10.2_

- [ ] 6.4 Add alerts endpoint
  - Implement `GET /alertas/{inventarioId}` endpoint
  - Return list of active alerts
  - Add endpoint to mark alerts as read
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

### - [ ] 7. Implement Export Service
Create export functionality for reports

- [ ] 7.1 Create ExportService class
  - Implement Excel export using Apache POI
  - Implement PDF export using iText
  - Implement CSV export
  - Implement PNG export for charts
  - _Requirements: 8.1, 8.2, 8.3_

- [ ] 7.2 Create export endpoint
  - Implement `POST /export` endpoint
  - Accept export format and filters in request body
  - Return file as byte array with proper content-type
  - Add progress tracking for large exports
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ] 7.3 Add export templates
  - Create Excel template with multiple sheets
  - Create PDF template with header/footer
  - Add logo and branding to exports
  - _Requirements: 8.2, 8.3_

---

## Phase 3: Desktop Interface

### - [ ] 8. Create Dashboard Frame
Implement main analytics screen for desktop

- [ ] 8.1 Create DashboardFrame class
  - Extend JFrame with proper layout
  - Add menu bar with refresh and export options
  - Create panels for metrics, charts, and filters
  - Implement window sizing and positioning
  - _Requirements: 1.1, 1.2, 1.3_

- [ ] 8.2 Create MetricsPanel
  - Create card-style panels for each metric
  - Display metric value and variation percentage
  - Add color coding (green for positive, red for negative)
  - Make cards clickable for drill-down
  - _Requirements: 1.1, 1.2, 1.3_

- [ ] 8.3 Create FiltersPanel
  - Add date range picker
  - Add setor dropdown filter
  - Add inventario dropdown filter
  - Add "Apply Filters" and "Clear Filters" buttons
  - _Requirements: 7.1, 7.2, 7.3, 7.4_

### - [ ] 9. Create Charts Panel
Implement chart visualizations using JFreeChart

- [ ] 9.1 Create ChartsPanel class
  - Create tabbed pane for different chart types
  - Implement line chart for coletas evolution
  - Implement pie chart for sector distribution
  - Implement bar chart for collector performance
  - _Requirements: 2.1, 3.1, 6.1_

- [ ] 9.2 Add chart interactivity
  - Add tooltips on hover
  - Add click handlers for drill-down
  - Add zoom and pan capabilities
  - Add legend with toggle visibility
  - _Requirements: 2.2, 3.2_

- [ ] 9.3 Add chart export
  - Add "Export Chart" button
  - Support PNG and PDF export
  - Maintain chart quality in exports
  - _Requirements: 2.4, 8.3_

### - [ ] 10. Create Divergencias Frame
Implement divergence analysis screen

- [ ] 10.1 Create DivergenciasFrame class
  - Create summary panel with divergence counts by type
  - Create table with detailed divergence list
  - Add filters for divergence type and status
  - Add "Resolve" button for each divergence
  - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [ ] 10.2 Add divergence resolution dialog
  - Create dialog for resolving divergences
  - Add fields for resolution notes
  - Update divergence status in database
  - Refresh list after resolution
  - _Requirements: 5.4_

### - [ ] 11. Create Performance Frame
Implement collector performance screen

- [ ] 11.1 Create PerformanceFrame class
  - Create table with collector performance metrics
  - Add sorting by column
  - Add color coding for inactive collectors
  - Add "Compare" button for multi-select
  - _Requirements: 6.1, 6.2, 6.3, 6.5_

- [ ] 11.2 Add comparison chart
  - Create dialog with comparison bar chart
  - Show selected collectors side-by-side
  - Highlight best and worst performers
  - _Requirements: 6.5_

### - [ ] 12. Integrate with existing desktop app
Wire analytics into main application

- [ ] 12.1 Add menu item for Analytics
  - Add "Analytics" menu to main menu bar
  - Add submenu items for Dashboard, Divergências, Performance
  - Add keyboard shortcuts
  - _Requirements: 1.1_

- [ ] 12.2 Add analytics button to toolbar
  - Add quick access button to main toolbar
  - Use appropriate icon
  - Open dashboard on click
  - _Requirements: 1.1_

---

## Phase 4: Mobile Interface

### - [ ] 13. Create Analytics Repository (Android)
Implement data layer for mobile analytics

- [ ] 13.1 Create AnalyticsRepository class
  - Implement API calls to analytics endpoints
  - Add error handling and retry logic
  - Implement offline caching
  - _Requirements: 12.1, 12.3_

- [ ] 13.2 Create Analytics DTOs (Kotlin)
  - Create Kotlin data classes matching backend DTOs
  - Add Gson annotations for serialization
  - Add extension functions for formatting
  - _Requirements: 12.1_

### - [ ] 14. Create Analytics ViewModel
Implement business logic for mobile analytics

- [ ] 14.1 Create AnalyticsViewModel class
  - Implement StateFlow for dashboard metrics
  - Implement StateFlow for charts data
  - Implement StateFlow for alerts
  - Add loading and error states
  - _Requirements: 12.1, 12.2_

- [ ] 14.2 Add data refresh logic
  - Implement pull-to-refresh
  - Add automatic refresh every 5 minutes
  - Handle offline mode gracefully
  - _Requirements: 12.2, 12.3_

### - [ ] 15. Create Analytics Activity
Implement main analytics screen for mobile

- [ ] 15.1 Create AnalyticsActivity layout
  - Create XML layout with ScrollView
  - Add cards for metrics
  - Add chart containers
  - Add filters section
  - _Requirements: 12.1_

- [ ] 15.2 Implement AnalyticsActivity class
  - Bind ViewModel to UI
  - Implement pull-to-refresh
  - Handle loading and error states
  - Add navigation to detail screens
  - _Requirements: 12.1, 12.2, 12.3, 12.4_

### - [ ] 16. Create Charts for Mobile
Implement chart visualizations using MPAndroidChart

- [ ] 16.1 Add MPAndroidChart dependency
  - Add library to build.gradle
  - Configure ProGuard rules if needed
  - _Requirements: 12.1_

- [ ] 16.2 Implement LineChart for coletas evolution
  - Create chart view in layout
  - Bind data from ViewModel
  - Add styling and colors
  - Add touch interactions
  - _Requirements: 12.1, 12.4_

- [ ] 16.3 Implement PieChart for sector distribution
  - Create chart view in layout
  - Bind data from ViewModel
  - Add percentage labels
  - Add legend
  - _Requirements: 12.1, 12.4_

### - [ ] 17. Add sharing functionality
Implement share feature for mobile

- [ ] 17.1 Add share button to metrics
  - Create share icon button
  - Generate shareable image
  - Open Android share sheet
  - _Requirements: 12.5_

- [ ] 17.2 Implement image generation
  - Capture metric card as bitmap
  - Add branding and timestamp
  - Optimize image size
  - _Requirements: 12.5_

---

## Phase 5: Testing and Polish

### - [ ] 18. Write unit tests
Test business logic and calculations

- [ ]* 18.1 Test AnalyticsService calculations
  - Test dashboard metrics calculations
  - Test percentage calculations
  - Test date range validations
  - Test comparison logic
  - _Requirements: Technical Requirements (Testing)_

- [ ]* 18.2 Test AnalyticsDAO queries
  - Test query results with sample data
  - Test query performance
  - Test edge cases (empty results, nulls)
  - _Requirements: Technical Requirements (Testing)_

- [ ]* 18.3 Test CacheService
  - Test cache hit/miss scenarios
  - Test TTL expiration
  - Test invalidation
  - _Requirements: Technical Requirements (Testing)_

### - [ ] 19. Write integration tests
Test API endpoints and database integration

- [ ]* 19.1 Test REST endpoints
  - Test all analytics endpoints with MockMvc
  - Test authorization and access control
  - Test error responses
  - Test pagination and filtering
  - _Requirements: Technical Requirements (Testing)_

- [ ]* 19.2 Test export functionality
  - Test Excel export with sample data
  - Test PDF export with sample data
  - Test CSV export
  - Verify file integrity
  - _Requirements: 8.1, 8.2, 8.3_

### - [ ] 20. Performance testing and optimization
Ensure system meets performance requirements

- [ ]* 20.1 Load test analytics endpoints
  - Test with 10,000 patrimônios
  - Test with 50 concurrent users
  - Measure response times
  - Identify bottlenecks
  - _Requirements: Technical Requirements (Performance)_

- [ ]* 20.2 Optimize slow queries
  - Run EXPLAIN ANALYZE on all queries
  - Add missing indexes
  - Optimize JOIN operations
  - Consider query rewriting
  - _Requirements: Technical Requirements (Performance)_

- [ ]* 20.3 Test cache effectiveness
  - Measure cache hit rate
  - Adjust TTLs based on usage patterns
  - Monitor memory usage
  - _Requirements: Technical Requirements (Performance)_

### - [ ] 21. UI/UX polish
Improve user experience and accessibility

- [ ]* 21.1 Add loading indicators
  - Add spinners for async operations
  - Add skeleton screens for charts
  - Add progress bars for exports
  - _Requirements: 1.4, 8.4_

- [ ]* 21.2 Improve error messages
  - Add user-friendly error messages
  - Add retry buttons
  - Add help text
  - _Requirements: Error Handling_

- [ ]* 21.3 Test accessibility
  - Test keyboard navigation
  - Test screen reader compatibility
  - Verify color contrast ratios
  - Add ARIA labels where needed
  - _Requirements: Technical Requirements (Accessibility)_

### - [ ] 22. Documentation
Create user and developer documentation

- [ ]* 22.1 Write user documentation
  - Create user guide for analytics features
  - Add screenshots and examples
  - Document export formats
  - Create FAQ section
  - _Requirements: All_

- [ ]* 22.2 Write API documentation
  - Document all REST endpoints with Swagger
  - Add request/response examples
  - Document error codes
  - Add authentication requirements
  - _Requirements: All API endpoints_

- [ ]* 22.3 Write developer documentation
  - Document architecture and design decisions
  - Add code examples for extending analytics
  - Document database schema changes
  - Add troubleshooting guide
  - _Requirements: All_

---

## Notes

- Tasks marked with `*` are optional but recommended
- Each task should be completed and tested before moving to the next
- Integration tests should be run after completing each phase
- Performance testing should be done before Phase 5
- Mobile implementation (Phase 4) can be done in parallel with Desktop (Phase 3)
