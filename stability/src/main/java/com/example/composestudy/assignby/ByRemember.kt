package com.example.composestudy.assignby

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


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
