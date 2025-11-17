# SOLID Principles Check Report
**SC2002 Internship Placement Management System**

---

## Summary
This report evaluates the codebase against the five SOLID principles: Single Responsibility Principle (SRP), Open/Closed Principle (OCP), Liskov Substitution Principle (LSP), Interface Segregation Principle (ISP), and Dependency Inversion Principle (DIP).

**Overall Assessment: MIXED - Several strong areas with notable violations**

---

## 1. Single Responsibility Principle (SRP)
*A class should have only one reason to change.*

### ✅ Strengths

**DatabaseManager.java** - Well decomposed:
- `EntityRepository` handles entity CRUD logic
- `FileOperations` interface isolates file I/O concerns
- `EntityFactoryRegistry` manages entity creation
- `DatabaseManager` acts as a facade

**Entity system** - Clear separation:
- `Entity` base class: stores and serializes data
- Each entity type (`StudentEntity`, `StaffEntity`, etc.) has focused responsibility
- Factories handle entity instantiation

**Authentication.java** - Good separation:
- Handles authentication routing
- Display logic delegated to `AuthenticationDisplay`

### ⚠️ Violations

**StaffHomePageController.java** - MAJOR VIOLATION (35+ methods):
```java
public class StaffHomePageController extends StaffController {
    // Responsibilities:
    // 1. Handle menu navigation
    // 2. Manage review filters (7+ filter methods)
    // 3. Validate date ranges
    // 4. Format output
    // 5. Normalize user input
    
    private void editFilters() { /* complex logic */ }
    private void setStatusFilter() { }
    private void setMajorFilter() { }
    private void setLevelFilter() { }
    private void setCompanyFilter() { }
    private void setPlacementStatusFilter() { }
    private void setMinimumApplications() { }
    private void setDateRangeFilters() { }
    private StaffReviewFilters.DateRange requestDateRange(String label) { }
    private LocalDate promptDate(String prompt) { }
    // ... plus multiple utility methods
}
```
**Issue**: This class violates SRP by managing:
- Navigation (menu handling)
- Filter management and validation
- Date parsing and validation
- String formatting and normalization

**Recommendation**: Extract into:
- `StaffFilterManager` - Handle filter operations
- `DateRangeValidator` - Validate and parse dates
- `FilterFormatter` - Format filter summaries

---

**ApplyInternshipController.java** - MODERATE VIOLATION:
```java
public class ApplyInternshipController extends StudentController {
    // Responsibilities:
    // 1. Load internship listings
    // 2. Validate student eligibility
    // 3. Check deadlines
    // 4. Parse dates
    // 5. Create applications
    // 6. Display internships
    
    private static boolean isVisible(InternshipEntity internship) { }
    private static String optionalTrim(String value) { }
    
    private static class YearChecker {
        static boolean checkYear(String studentsAge, String internshipLevel) { }
    }
}
```
**Issue**: Mixed concerns of business logic (eligibility, deadlines) and presentation

**Recommendation**: Extract:
- `ApplicationEligibilityChecker` - Verify student eligibility
- `DeadlineValidator` - Check application deadlines
- `InternshipVisibilityFilter` - Determine visibility

---

**StudentHomePageController.java** - MINOR VIOLATION:
```java
private void handleMenu() {
    while (true) {
        // Complex switch with 9 cases
        // Each case instantiates different controllers
    }
}
```
**Issue**: Menu routing mixed with controller navigation

**Recommendation**: Extract to `StudentMenuRouter` or similar

---

### SRP Score: 6/10
**Issue**: Multiple controllers have too many responsibilities, especially `StaffHomePageController`

---

## 2. Open/Closed Principle (OCP)
*Classes should be open for extension but closed for modification.*

### ✅ Strengths

**DatabaseManager.java** - Excellent OCP design:
```java
interface EntityFactory {
    Entity createEntity(String csvLine);
    boolean canHandle(String entityType);
}

class EntityFactoryRegistry {
    public static void registerFactory(EntityFactory factory) {
        factories.add(factory); // New types can be added without modifying existing code
    }
}
```
**Benefit**: New entity types can be added by implementing `EntityFactory` without changing `DatabaseManager`

**Router.java** - Extension through inheritance:
- `Controller` is base class for all controllers
- New controllers extend `Controller` without modifying Router
- Stack-based navigation supports any controller type

**FileOperations interface** - Strategy pattern:
```java
interface FileOperations {
    List<String> readLines(String filePath);
    void writeLines(String filePath, List<String> lines, boolean append);
}
```
Can swap implementations without affecting `EntityRepository`

### ⚠️ Violations

**StudentHomePageController.java** - Hardcoded menu routing:
```java
switch (choice) {
    case "1" -> new ViewInternshipController(router, scanner, entityStore, studentID);
    case "2" -> new UpdateInternshipFiltersController(router, scanner, entityStore, studentID);
    case "3" -> handleClearFilters();
    case "4" -> new ApplyInternshipController(router, scanner, entityStore, studentID);
    // ... more cases
}
```
**Issue**: Adding new student features requires modifying `StudentHomePageController`

**Recommendation**: Use a menu registry pattern:
```java
interface MenuAction {
    void execute();
}

class MenuRegistry {
    private Map<String, MenuAction> actions = new HashMap<>();
    
    public void register(String key, MenuAction action) {
        actions.put(key, action);
    }
}
```

---

**StaffHomePageController.java** - Hardcoded filter logic:
```java
private void editFilters() {
    switch (choice) {
        case "1" -> setStatusFilter();
        case "2" -> setMajorFilter();
        case "3" -> setLevelFilter();
        // Can't add new filter types without modifying this method
    }
}
```

---

**Entity.java** - Array-based storage:
```java
protected String[] values;

public String getArrayValueByIndex(int i) { }
public String setArrayValueByIndex(int i, String value) { }
```
**Issue**: Schema changes require modifying the base class

---

### OCP Score: 7/10
**Issue**: Entity creation is well-designed, but navigation/menu logic is tightly coupled

---

## 3. Liskov Substitution Principle (LSP)
*Subtypes must be substitutable for their base types.*

### ✅ Strengths

**Controller hierarchy**:
```
Controller (abstract)
├── UserController
│   ├── StudentController
│   │   ├── ViewInternshipController
│   │   ├── ApplyInternshipController
│   │   └── ...
│   ├── StaffController
│   │   ├── ReviewRegistrationController
│   │   └── ...
│   └── CRController
└── Authentication
```
All subclasses properly implement `initialize()` without violating parent contracts.

**Entity hierarchy**:
```
Entity (abstract)
├── StudentEntity
├── StaffEntity
├── CREntity
├── InternshipEntity
├── ApplicationEntity
└── UserEntity
```
All subclasses can be used interchangeably as `Entity`.

**FileOperations implementations**:
- `StandardFileOperations` implements contract correctly
- Can be swapped with other implementations transparently

### ⚠️ Violations

**No major violations detected** - The class hierarchy respects LSP well.

**Minor concern**: `Router` assumptions about Controller state:
```java
public void replace(Controller controller) {
    controllerStack.pop();  // Assumes at least one controller exists
    this.push(controller);
}
```
**Issue**: If `controllerStack` is empty, this throws `EmptyStackException`

**Recommendation**: Add null/empty checks:
```java
public void replace(Controller controller) {
    if (!controllerStack.isEmpty()) {
        controllerStack.pop();
    }
    this.push(controller);
}
```

---

### LSP Score: 9/10
**Issue**: Minor defensive programming concerns, but generally LSP-compliant

---

## 4. Interface Segregation Principle (ISP)
*Clients should not depend on interfaces they don't use.*

### ✅ Strengths

**EntityStore interface** - Focused and segregated:
```java
public interface EntityStore {
    List<Entity> loadAll(String filePath, String entityType);
    Entity findById(String filePath, String id, String entityType);
    void append(String filePath, Entity entity);
    void update(String filePath, String id, Entity entity, String entityType);
    void delete(String filePath, String id, String entityType);
}
```
Only exposes persistence operations needed by controllers.

**EntityFactory interface** - Minimal and cohesive:
```java
interface EntityFactory {
    Entity createEntity(String csvLine);
    boolean canHandle(String entityType);
}
```

**FileOperations interface** - Clean separation:
```java
interface FileOperations {
    List<String> readLines(String filePath);
    void writeLines(String filePath, List<String> lines, boolean append);
}
```

### ⚠️ Violations

**Controller base class has too many dependencies**:
```java
public abstract class Controller {
    public final Scanner scanner;        // UI input
    public final Router router;          // Navigation
    protected final EntityStore entityStore;  // Persistence
}
```
**Issue**: Not all controllers need all three:
- Authentication doesn't need `entityStore` (uses it minimally)
- Some controllers might only need `Router`

**Recommendation**: Use constructor injection or interface segregation:
```java
public interface HasRouter { Router getRouter(); }
public interface HasScanner { Scanner getScanner(); }
public interface HasEntityStore { EntityStore getEntityStore(); }

public abstract class Controller implements HasRouter, HasScanner, HasEntityStore {
    // ...
}
```

---

**Display hierarchy not segregated**:
```java
public abstract class Display {
    protected final Controller owner;
    protected final Scanner scanner;
}
```
All displays have owner reference and scanner, but not all may need both.

---

### ISP Score: 7/10
**Issue**: Interfaces are good, but base classes expose more than needed

---

## 5. Dependency Inversion Principle (DIP)
*High-level modules should not depend on low-level modules. Both should depend on abstractions.*

### ✅ Strengths

**DatabaseManager - Perfect DIP implementation**:
```java
public class DatabaseManager implements EntityStore {
    private final EntityRepository repository;
    
    public DatabaseManager(EntityRepository repository) {
        this.repository = repository;  // Dependency injection
    }
}

class EntityRepository {
    private final FileOperations fileOps;  // Depends on abstraction
    
    public EntityRepository(FileOperations fileOps) {
        this.fileOps = fileOps;
    }
}
```
**Benefit**: Can swap `FileOperations` implementation without changing `EntityRepository`

**Controllers depend on EntityStore interface**:
```java
public abstract class Controller {
    protected final EntityStore entityStore;  // Abstraction, not concrete DatabaseManager
}
```

**Router - Abstraction over concrete controllers**:
```java
public class Router {
    private final Stack<Controller> controllerStack;  // Abstract type
}
```

### ⚠️ Violations

**Scanner injection as concrete type**:
```java
public abstract class Controller {
    public final Scanner scanner;  // Concrete type, not abstraction
}

// In Main.java:
Scanner scanner = new Scanner(System.in);
Authentication authentication = new Authentication(router, scanner, entityStore);
```
**Issue**: Scanner is tightly coupled; hard to test or swap

**Recommendation**: Create abstraction:
```java
public interface InputProvider {
    String readLine();
}

public class ScannerInputProvider implements InputProvider {
    private final Scanner scanner;
    // ...
}

public abstract class Controller {
    protected final InputProvider input;  // Abstraction
}
```

---

**StaffReviewFilters passed directly**:
```java
public class ReviewRegistrationController {
    public ReviewRegistrationController(
        Router router, 
        Scanner scanner, 
        EntityStore entityStore, 
        String staffID, 
        StaffReviewFilters filters  // Concrete type
    ) { }
}
```
**Issue**: Depends on concrete `StaffReviewFilters` instead of interface

**Recommendation**: Create `FilterProvider` interface:
```java
public interface FilterProvider {
    Set<String> getStatuses();
    Set<String> getMajors();
    // ...
}
```

---

**Main.java creates concrete implementations**:
```java
Router router = new Router();
Scanner scanner = new Scanner(System.in);
EntityStore entityStore = new DatabaseManager();  // Concrete class, not interface
```
**Issue**: Main knows too much about concrete implementations

**Recommendation**: Use factory or service locator pattern

---

### DIP Score: 7/10
**Issue**: Good with persistence layer, but Scanner and filters need abstraction

---

## Detailed Violation Summary

| Principle | Score | Severity | Main Issues |
|-----------|-------|----------|------------|
| **SRP** | 6/10 | HIGH | `StaffHomePageController` has 35+ methods; `ApplyInternshipController` mixes business logic |
| **OCP** | 7/10 | MEDIUM | Hardcoded menu switches; can't extend without modification |
| **LSP** | 9/10 | LOW | Minor defensive programming issues in `Router` |
| **ISP** | 7/10 | MEDIUM | `Controller` base class exposes unneeded dependencies |
| **DIP** | 7/10 | MEDIUM | `Scanner` as concrete type; `StaffReviewFilters` tightly coupled |

---

## Critical Issues (High Priority)

### 1. **StaffHomePageController - Violates SRP Severely**
**Lines of code**: ~200+
**Methods**: 35+
**Responsible for**:
- Navigation menu handling
- 7 different filter types
- Date range validation and parsing
- String formatting and normalization

**Impact**: Extremely difficult to test, modify, or extend

**Fix Priority**: 🔴 CRITICAL
**Estimated effort**: 2-3 hours
**Refactoring steps**:
1. Extract `StaffFilterManager` class
2. Extract `DateRangeValidator` class
3. Extract `FilterFormatter` class
4. Reduce controller to just menu orchestration

---

### 2. **ApplyInternshipController - Mixed Concerns**
**Issues**:
- Eligibility checking (business logic) mixed with UI
- Date parsing mixed with application submission
- Visibility filtering mixed with data retrieval

**Fix Priority**: 🟡 HIGH
**Estimated effort**: 1-2 hours

---

### 3. **Scanner Passed as Concrete Type - Violates DIP**
**Issue**: Cannot mock `Scanner` in tests
**Affects**: All controllers, all display classes
**Fix Priority**: 🟡 HIGH
**Estimated effort**: 1-2 hours

---

## Medium Priority Issues

### 4. **Hardcoded Menu Routing - Violates OCP**
**Issue**: Adding new features requires modifying controller classes
**Affects**: `StudentHomePageController`, `StaffHomePageController`, `Authentication`
**Fix Priority**: 🟠 MEDIUM
**Estimated effort**: 1-2 hours

---

### 5. **Base Controller Class Over-Injected - Violates ISP**
**Issue**: Not all controllers need `EntityStore`
**Fix Priority**: 🟠 MEDIUM
**Estimated effort**: 0.5-1 hour

---

## Positive Observations

1. ✅ **DatabaseManager** is a textbook example of SRP, OCP, and DIP
2. ✅ **Entity hierarchy** follows LSP correctly
3. ✅ **Factory pattern** for entity creation is well-implemented
4. ✅ **Router abstraction** allows adding new controller types without modification
5. ✅ **Interface-based persistence** (`EntityStore`) is excellent

---

## Recommended Refactoring Roadmap

### Phase 1 (CRITICAL) - 3-4 hours
1. Extract `StaffFilterManager` from `StaffHomePageController`
2. Create `InputProvider` interface to replace concrete `Scanner`
3. Extract filter-related methods from `ApplyInternshipController`

### Phase 2 (HIGH) - 2-3 hours
4. Implement menu registry pattern for student/staff/CR menus
5. Create `DateValidator` utility class
6. Add defensive checks in `Router`

### Phase 3 (MEDIUM) - 2-3 hours
7. Segregate `Controller` base class dependencies
8. Extract `FilterProvider` interface from `StaffReviewFilters`
9. Add comprehensive unit tests

---

## Testing Recommendations

Current testability is **LOW** (3/10) due to:
- Concrete `Scanner` injection
- Tight coupling between classes
- Large methods with multiple responsibilities

After refactoring:
- Testability should reach **GOOD** (7/10)
- Mock `InputProvider` instead of `Scanner`
- Test business logic separately from UI

---

## Conclusion

The codebase demonstrates **good understanding of SOLID principles** in the persistence layer (DatabaseManager, EntityStore interface) but violates them significantly in the **controller/UI layer** (SRP violations in menu handling, OCP violations in hardcoded routing, DIP violations with concrete Scanner).

**Overall SOLID Score: 7.2/10**

The refactoring roadmap above, if implemented, could bring the score to **8.5+/10**.

