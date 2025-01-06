# by remember VS = remember

### by remember 코드
```kotlin
@Composable
fun ByParent(text: String, number: Int) {
    Column {
        Text(text = "Child 1: $text")
        Text(text = "Child 2: $number")
    }
}

@Composable
fun ByExample() {
    var text by remember { mutableStateOf("Hello") }
    var number by remember { mutableStateOf(0) }

    Column {
        Button(onClick = { text += "!" }) {
            Text("Change Text")
        }
        Button(onClick = { number += 1 }) {
            Text("Increment Number")
        }

        ByParent(text = text, number = number)
    }
}
```
by를 사용하기 때문에 매개변수로 값 자체를 넘겨주고있다.  
하나의 버튼이 눌렸을 때 변경된 매개변수를 갖는 컴포저블 함수는 리컴포지션이 되지만 다른 컴포저블 함수느 스킵된걸 확인 할 수 있다.


### = remember
```kotlin
@Composable
fun ParentComposable(text: MutableState<String>, number: MutableState<Int>) {
    Column {
        Text(text = "Child 1: $text")
        Text(text = "Child 2: $number")
    }
}

@Composable
fun AssignExample() {
    val text = remember { mutableStateOf("Hello") }
    val number = remember { mutableStateOf(0) }

    Column {
        Button(onClick = { text.value += "!" }) {
            Text("Change Text")
        }
        Button(onClick = { number.value += 1 }) {
            Text("Increment Number")
        }

        ParentComposable(text = text, number = number)
    }
}
```
이번에는 매개변수로 값 자체를 넘겨주는게 아니라 상태를 넘겨주고 있다.  
이는 하위 컴포저블에서 구독을 하는 형태가 된다. 그래서 값이 바뀌지 않으면 재구성 검사 자체를 안하게 된다.  
따라서 아래 사진처럼 하나의 버튼이 눌렸을 때 변경되지 않는 컴포저블 함수는 스킵 횟수를 띄우지조차 않는다.


### 정리
#### by remember
1. 값 자체를 넘겨줌 -> 코드 간결
2. 스킵이 되더라도 검사는 한다

#### = remember
1. 상태를 구독하는 컴포저블만 값 변경에 따라 리컴포지션 된다 -> 성능 최적화
2. 상태를 넘기기 때문에 코드가 복잡해진다
