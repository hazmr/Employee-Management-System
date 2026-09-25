# Employee Management System — Spring Core Mini Project

A small plain-Spring (no Spring Boot) application that demonstrates the IoC container,
dependency injection, bean configuration, profiles, scopes and the bean lifecycle.

## Running it

```bash
mvn compile
mvn exec:java -Dexec.args="dev"    # in-memory repository (default)
mvn exec:java -Dexec.args="prod"   # file-backed repository (employees.csv)
```

The full console output of a `dev` run is in `console-output.txt`.

## Package layout

```
org.haz
├── config/      AppConfig
├── model/       Employee
├── repository/  EmployeeRepository, InMemoryEmployeeRepository, FileBackedEmployeeRepository
├── service/     EmployeeService, EmployeeServiceImpl, EmployeeValidator, InvalidEmployeeException
├── notify/      Notifier, EmailNotifier, SmsNotifier, PushNotifier, NotificationManager
├── audit/       AuditLogger
└── Main
```

## Which beans are singleton and which are prototype

Everything is a singleton except `AuditLogger`.

The repository, the service, the validator, the three notifiers and the notification
manager are stateless (or hold state that is meant to be shared, like the employee
store). One shared instance is cheaper and is exactly what we want — every caller must
see the same employee data and the same notification channels.

`AuditLogger` is `@Scope("prototype")` because each audit entry is a separate unit of
work with its own identity and timestamp. The console output prints the instance id, so
three `getBean(AuditLogger.class)` calls visibly produce three different ids while
`getBean(EmployeeService.class)` returns the same object twice.

## Which injection type was used where

- **Constructor injection** — `EmployeeServiceImpl` takes its repository, the
  `NotificationManager` and the `ObjectProvider<AuditLogger>` through the constructor,
  and `NotificationManager` takes its `List<Notifier>` the same way. These dependencies
  are mandatory: without them the bean cannot work at all, so the constructor makes them
  final and makes it impossible to build a half-wired object.
- **Setter injection** — `EmployeeServiceImpl.setValidator(...)` and
  `NotificationManager.setUrgentNotifier(...)`. Used where the dependency can be swapped
  or re-injected after construction, and where a qualifier reads more clearly on a
  setter than as one more constructor parameter.
- **Field injection** — only for the `@Value` properties (`company.name`,
  `company.currency`, `raise.max-percentage`, `notification.retry-count`,
  `storage.file-path`). These are simple configuration values, not collaborators, so the
  usual objection to field injection (untestable, hidden dependencies) does not bite.

`@Primary` is on `EmailNotifier` so it wins whenever a single `Notifier` is injected
without further information. `@Qualifier("smsNotifier")` is used in `NotificationManager`
to pick the SMS channel specifically for urgent messages.

## Collection injection and ordering

`NotificationManager` receives `List<Notifier>` — the container collects every `Notifier`
bean and sorts it by `@Order` (email 1, SMS 2, push 3), then the manager loops over the
list. Adding a fourth channel means adding one `@Component`; no existing class changes.
`EmployeeServiceImpl` calls the manager when an employee is added and again, with a
different message, when a raise is applied.

## How the scoped-bean problem was solved

`EmployeeServiceImpl` is a singleton, so it is created once and its dependencies are
injected once. Injecting `AuditLogger` directly would give the service a single prototype
instance frozen for the lifetime of the application — the prototype scope would be
pointless.

The fix used here is `ObjectProvider<AuditLogger>`: the service holds the provider and
calls `auditLoggerProvider.getObject()` at the moment it needs to log, so the container
creates a fresh instance per call. `ObjectProvider` was preferred over a scoped proxy
(`proxyMode = ScopedProxyMode.TARGET_CLASS`) because the lookup is explicit — anyone
reading the code sees where a new instance is requested — and it needs no CGLIB subclass
of `AuditLogger`, so the class needs no non-final no-arg constructor and there is no
proxy in the stack trace. A scoped proxy would have worked too, and would be the better
choice if the prototype had to be passed around as a plain `AuditLogger` field.

Note that Spring does not call `@PreDestroy` on prototype beans: the container stops
tracking them after creation, so their cleanup is the caller's responsibility.

## Bean lifecycle

Both styles are shown:

- `@PostConstruct` / `@PreDestroy` on the repositories, `EmployeeServiceImpl`,
  `NotificationManager` and `AuditLogger`.
- `InitializingBean` / `DisposableBean` on `EmployeeValidator`.

`Main` ends with `context.close()`, which is what makes the destruction callbacks print.

## dev vs prod profile

Both repository implementations implement `EmployeeRepository`, and each is annotated
with `@Profile`:

- `dev` → `InMemoryEmployeeRepository`, data lives in a `LinkedHashMap` and disappears
  when the context closes.
- `prod` → `FileBackedEmployeeRepository`, data is read from and written to the CSV file
  named by `storage.file-path`.

The profile is selected in `Main` through
`context.getEnvironment().setActiveProfiles(profile)` before `refresh()`. Only the
matching bean is registered, so the service gets exactly one candidate and no code in the
service layer changes. The startup log line prints which implementation was wired.

## Externalised configuration

`src/main/resources/application.properties` is loaded with `@PropertySource` in
`AppConfig`:

| key | used by |
| --- | --- |
| `company.name` | notification text, startup log |
| `company.currency` | raise notification |
| `notification.retry-count` | `NotificationManager` |
| `raise.max-percentage` | rejects a raise above the cap |
| `storage.file-path` | file-backed repository |
