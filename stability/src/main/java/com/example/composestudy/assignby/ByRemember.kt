package com.example.composestudy.assignby

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ByChild1(text: String) {
    Text(text = "Child 1: $text")
}

@Composable
fun ByChild2(number: List<Int>) {
    Text(text = "Child 2: ${number}")
}

@Composable
fun ByParent(text: String, number: List<Int>) {
    Column {
        ByChild1(text = text)
        key(number) {
            ByChild2(number = number)
        }
    }
}

@Composable
fun ByExample() {
    var text by remember { mutableStateOf("Hello") }
    var number by remember { mutableStateOf(listOf(1,2,3)) }

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
