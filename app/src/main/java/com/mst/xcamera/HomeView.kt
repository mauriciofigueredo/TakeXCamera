package com.mst.xcamera

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cortes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        content = {
            HomeContent(it, navController)
        }
    )
}

@Composable
fun HomeContent(values : PaddingValues, navController: NavController){

    var selectedOption by remember{ mutableStateOf("") }
    val options = listOf<String>("Corte", "Recorte", "Otro")
    var lectura by remember { mutableStateOf("") }
    var cuenta by remember { mutableStateOf("") }

    Column(
        //verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(values)
            .fillMaxSize())
    {
        Spacer(modifier = Modifier.padding(10.dp))

        MainTitle(title = "Cargar trabajo", color = Color.Gray, size = 25.sp)
        Spacer(modifier = Modifier.padding(15.dp))

        //Radio buton, seleccionar tipo de trabajo
        Row {
            options.forEach { text ->
                Row(
                    Modifier
                        .clickable(onClick = { selectedOption = text })
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (text == selectedOption),
                        onClick = { selectedOption = text }
                    )
                    Text(
                        text = text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding( end = 20.dp)
                    )
                }
            }
        }//fin Radio button
        Spacer(modifier = Modifier.padding(20.dp))

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 30.dp)) {
            MainTextField(
                value = cuenta,
                onValueChange = { cuenta = it},
                label = "Cuenta",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.padding(bottom = 10.dp))
            MainTextField(
                value = lectura,
                onValueChange = { lectura = it },
                label = "Lectura",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.padding(10.dp))

            if(cuenta.isNotEmpty()){
                FilledTonalButton(
                    onClick = { navController.navigate("Agregar")},
                    border = BorderStroke(1.dp, Color.Cyan),
                    shape = CircleShape,
                ) { Text("Agregar fotos") }
            }
        }
    }
}





