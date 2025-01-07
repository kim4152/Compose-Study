# Stability

## Compose Compiler Metrics

Compose Compiler의 성능 및 안정성 메트릭과 리포트 볼 수 있다.  
이를 통해 컴파일 과정에서 각 Composable 함수와 관련된 데이터를 어떻게 처리하고 최적화했는지 분석할 수 있다.

```kotlin
// build.gradle.kts (module)
kotlinOptions {
    // ...
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                listOf(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                            layout.buildDirectory.get().asFile.absolutePath + "/compose_metrics",
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                            layout.buildDirectory.get().asFile.absolutePath + "/compose_metrics"
                )
            )
        }
    }
}
```

/build/compose_metrics 디렉터리에 생성된

+ ..module.json : 전체적인 컴포저블 종류 확인 가능한다.(skippable, restartable Composable)
+ ..composable.txt : 컴포저블 함수를 상세히 분석한 것을 볼 수 있다.
+ ..classes.txt : 특정 클래스에 대해 어떻게 안정성 추론을 했는지 볼 수 있다. -stable, usstable, runtime(안정서이 다른 종속성에 의해 좌우됨)
  파일들을 통해 확인이 가능하다.

---

## Stable과 Unstable

컴포즈는 컴포저블 매개변수의 안정성을 사용하여 컴포저블이 리컴포지션 중에 컴포저블을 건너뛸 수 있다.

파라미터의 안정성 유형

1. Stable : 컴포저블 함수가 안정적인 파라미터를 가지고 있고, 그 파라미터 값이 변경되지 않았다면, 컴포즈는 컴포저블을 건너띈다.
   -> 기본형과 문자열, @Stable, @Immutable
2. Unstable : 불안정한 파라미터를 가지고 있다면, 해당 컴포저블은 부모 컴포넌트가 리컴포즈될 때 항상 다시 리컴포즈된다.
   -> 컬렉션과 Any 타입

## 안정성 문제 해결

### 강력한 건너뛰기 사용

### 불변 컬렉션

Compose 컴파일러는 List, Map 및 Set와 같은 컬렉션이 실제로 불변인지 완전히 확신할 수 없으므로 불안정 처리를한다.    
안드로이드
공식문서에서는 [Kotlinx Immutable Collection](https://github.com/Kotlin/kotlinx.collections.immutable)을
사용하길
권장한다.

ImmutableCollection은 코틀린의 Collection을 구현하고 있고,  
ImmutableList는 코틀린 List와 ImmutableCollection을 구현하고 있다.

```kotlin
public interface ImmutableCollection<out E> : Collection<E>
public interface ImmutableList<out E> : List<E>, ImmutableCollection<E>
```

그리고 이 라이브러리에는 또다른 컬렉션 PersistentCollection 존재한다.
PersistentCollection은 ImmutableCollection을 구현하고 있다.

```kotlin
public interface PersistentCollection<out E> : ImmutableCollection<E>
public interface PersistentList<out E> : ImmutableList<E>, PersistentCollection<E>
```

#### ImmutableCollection과 PersistentCollection의 차이는 뭘까

둘다 변경 불가능한 데이터 구조를 다루지만...

##### ImmutableCollection

이 인터페이스는 단지 Collection만을 구현하고 있기 떄문에 어떠한 수정도 하지 못한다.

#### PersistentCollection

PersistentCollection은 ImmutableCollection을 구현하면서 여러 수정가능한 함수들을 포함한다.  
원본 객체에 수정 연산을 적용한 사본을 반환하기 때문에 반환 값이 ImmutableCollection인 것을 확인할 수 있다.

```kotlin
public interface PersistentCollection<out E> : ImmutableCollection<E> {
    public fun add(element: @UnsafeVariance E): PersistentCollection<E>

    public fun addAll(elements: Collection<@UnsafeVariance E>): PersistentCollection<E>

    public fun remove(element: @UnsafeVariance E): PersistentCollection<E>
    // ...
}
```

- mutate 함수
  이 함수를 사용하면 여러 수정 작업을 수행할 때 매 번 새로운 리스트를 생성하는 대신, Builder를 통해 최종 리스트를 반환한다.   
  수정 작업을 여러번 해야할 때 도움이 될 것같다.

```kotlin
public inline fun <T> PersistentList<T>.mutate(mutator: (MutableList<T>) -> Unit): PersistentList<T> =
    builder().apply(mutator).build()
```

### 어노테이션 사용

##### @Immutable

불변 보증. 모든 공개 프로퍼티에 val 키워드를 사용하여 불변성을 보장해줘야한다.  
그리고 컬렉션을 사용하는 경우 절대 수정이 발생하지 않는다는 확신이 서는 경우에 사용해야한다.

##### @Stable

Immutable보다는 느슨하다. 객체의 상태가 변경되더라도 컴포즈가 안전하게 상태 변화를 추적할 수 있음을 의미한다.    
객체 참조가 변경되지 않으면 리컴포지션을 스킵한다.  
내부 상태가 변경 가능하더라도 상태 관리를 효율적으로 해야할 때 사용한다.

```kotlin
@Stable
interface MutableState<T> : State<T> {
    override var value: T  // 변경 가능한 value. 
    operator fun component1(): T
    operator fun component2(): (T) -> Unit
}
```

##### NonRestartableComposable

호출 매개변수의 변경으로 인한 recomposition 프로세스 중에 다시 시작되지 않아야 함을 알린다.
함수의 재구성이 필요 없고, 함수의 역할이 컴포즈 상태나 UI를 직접 변경하지 않을 때 사용하면 된다.

```kotlin
@Composable
@NonRestartableComposable
@OptIn(InternalComposeApi::class)
fun LaunchedEffect(
    key1: Any?,
    block: suspend CoroutineScope.() -> Unit
) {
    val applyContext = currentComposer.applyCoroutineContext
    remember(key1) { LaunchedEffectImpl(applyContext, block) }
}
```

위 함수는 비동기 작업을 실행하기 위한 함수다. 컴포저블의 상태와 독립적으로 동작하도록 설계되어 있다.

1. key1이 변경 되었을 때만 remember(key1) 에 의해 새로 생선된 객체 LaunchedEffectImpl이 초기화된다. 변경되지 않았다면 캐시값 사용.

```kotlin
@Composable
inline fun <T> remember(
    key1: Any?,
    crossinline calculation: @DisallowComposableCalls () -> T
): T {
    return currentComposer.cache(currentComposer.changed(key1), calculation)
    // 변경되었다면 true
}

@ComposeCompilerApi
inline fun <T> Composer.cache(invalid: Boolean, block: @DisallowComposableCalls () -> T): T {
    @Suppress("UNCHECKED_CAST")
    return rememberedValue().let {
        if (invalid || it === Composer.Empty) {
            val value = block()
            updateRememberedValue(value) // 값 저장
            value // block 실행
        } else it // 기존에 저장된 값 실행
    } as T
}
```

2. 수신 객체 CoroutineScope로 지정된 block을 실행한다. 컴포즈의 생명주기와 같다.

```kotlin
internal class LaunchedEffectImpl(
    parentCoroutineContext: CoroutineContext,
    private val task: suspend CoroutineScope.() -> Unit
) : RememberObserver {
    private val scope = CoroutineScope(parentCoroutineContext)
    private var job: Job? = null

    override fun onRemembered() {
        // This should never happen but is left here for safety
        job?.cancel("Old job was still running!")
        job = scope.launch(block = task)
    }

    override fun onForgotten() { //  객체가 더 이상 필요하지 않을 때
        job?.cancel(LeftCompositionCancellationException())
        job = null
    }

    override fun onAbandoned() { // 컴포지션이 강제 중지 되었을 떄
        job?.cancel(LeftCompositionCancellationException())
        job = null
    }
}
```

## Skippable과 Restartable

컴포즈는 함수를 Skippable과 Restartable로 표시할 수 있다.  
둘다 표시되거나 안될 수도 있다.

1. Skippable : 해당 컴포저블의 모든 인자가 이전 값과 동일한 경우 리컴포지션 과정에서 이를 건너띌 수 있다.
2. Restartable : 리컴포지션이 시작될 수 있는 범위 역할을 한다. 즉, 상태가 변경된 이후 컴포즈가 리컴포지션을 시작할 수 있는 진입점이 될 수 있다.  
   (리컴포지션의 시작 지점이 될 수 있는 함수)

## 리컴포지션을 건너뛰는게 무조건 좋을까?

#### 그렇지 않다.

코드가 복잡해지고 유지보수성이 떨어질 수 있다. 또한 오히려 성능이 저하될 수 있다.

#### 언제 필요하지 않을까

1. 리컴포지션이 자주 발생하지 않는 경우
   리컴포지션 자체가 거의 없다면 성능 이점이 별로 없다.

2. 이미 skippable 컴포저블만 호출하는 경우
   상위 컴포저블에서 추가적으로 skippable로 만들 필요가 없다.

3. 파라미터 변경 확인 비용이 리컴포지션 비용보다 클 경우
   파라미터 equals 연산이 복잡해지거나 비용이 크다면, 리컴포지션을 허용하는 것이 더 효율적일 수 있다.

#### Equals 연산

