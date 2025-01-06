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
2. Unstable : 불안정한 파라미터를 가지고 있다면, 해당 컴포저블은 부모 컴포넌트가 리컴포즈될 때 항상 다시 리컴포즈된다.


### 안정성 문제 해결
#### 강력한 건너뛰기 사용

#### 클래스를 변경할 수 없게 만들기


#### 불변 컬렉션


#### 어노테이션 사용


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

내부적으로 이미 skippable 컴포저블만 호출하는 컴포저블 ??
