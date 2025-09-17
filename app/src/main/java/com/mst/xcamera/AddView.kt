package com.mst.xcamera



import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.ViewGroup
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import coil3.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.util.concurrent.Executor



// Utiliza la anotación @OptIn para las funciones experimentales de permisos de Accompanist.
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddView() {
    // Solicita el permiso de la cámara utilizando rememberPermissionState.
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val context = LocalContext.current
    val cameraController = remember { LifecycleCameraController(context) }
    val lifecycle = LocalLifecycleOwner.current

    // Estado para guardar la URI de la imagen capturada. Si es 'null', la cámara está activa.
    // Si tiene una URI, se muestra la previsualización.
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Lanza la solicitud de permiso la primera vez que se compone la vista.
    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            // Muestra el botón para tomar la foto solo si no hay una imagen en previsualización.
            if (capturedImageUri == null) {
                FloatingActionButton(onClick = {
                    val executor = ContextCompat.getMainExecutor(context)
                    // Llama a la función 'takePicture' y pasa un callback para manejar la URI de la imagen.
                    takePicture(cameraController, executor, context) { uri ->
                        capturedImageUri = uri
                    }
                }) {
                    Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Take photo", modifier = Modifier.size(35.dp))
                }
            }
        }
    ) {
        // Verifica si el permiso de la cámara ha sido concedido.
        if (permissionState.status.isGranted) {
            // Si no hay una imagen capturada, muestra la vista de la cámara.
            if (capturedImageUri == null) {
                CamaraComposable(cameraController, lifecycle, modifier = Modifier.padding(it))
            } else {
                // Si hay una imagen capturada, muestra la previsualización y los botones.
                CapturedImageView(
                    imageUri = capturedImageUri!!,
                    onSave = {
                        // Lógica para guardar la imagen de forma permanente en la galería.
                        saveImageToGallery(context, capturedImageUri!!)
                        // Restablece el estado para volver a la vista de la cámara.
                        capturedImageUri = null
                    },
                    onDiscard = {
                        // Lógica para eliminar el archivo temporal de la imagen.
                        deleteTemporaryImage(context = context, uri = capturedImageUri)
                        // Restablece el estado para volver a la vista de la cámara.
                        capturedImageUri = null
                    },
                    modifier = Modifier.padding(it)
                )
            }
        } else {
            // Muestra un mensaje si el permiso es denegado.
            Text(text = "Permiso Denegado!", modifier = Modifier.padding(it))
        }
    }
}

/**
 * Función para capturar una foto.
 * @param cameraController El controlador de la cámara.
 * @param executor El ejecutor para las tareas de la cámara.
 * @param context El contexto de la aplicación.
 * @param onImageCaptured Callback que se ejecuta con la URI de la imagen temporal.
 */
private fun takePicture(
    cameraController: LifecycleCameraController,
    executor: Executor,
    context: Context,
    onImageCaptured: (Uri) -> Unit
) {
    // Crea un archivo temporal en el caché de la aplicación para la foto.
    val file = File(context.cacheDir, "temp_image.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

    cameraController.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // Si la captura es exitosa, llama al callback con la URI del archivo temporal.
                outputFileResults.savedUri?.let { onImageCaptured(it) }
            }

            override fun onError(exception: ImageCaptureException) {
                // Maneja los errores de la captura de la imagen.
                println("Error al tomar la foto: ${exception.message}")
            }
        },
    )
}

/**
 * Composable que muestra la vista previa de la cámara.
 * @param cameraController El controlador de la cámara.
 * @param lifecycle El propietario del ciclo de vida.
 * @param modifier El modificador para la vista.
 */
@Composable
fun CamaraComposable(
    cameraController: LifecycleCameraController,
    lifecycle: LifecycleOwner,
    modifier: Modifier = Modifier,
) {
    // Vincula el controlador de la cámara al ciclo de vida de la vista.
    cameraController.bindToLifecycle(lifecycle)
    // Utiliza AndroidView para integrar la vista de la cámara de Android en Jetpack Compose.
    AndroidView(modifier = modifier, factory = { context ->
        val previewView = PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        previewView.controller = cameraController
        previewView
    })
}

/**
 * Composable que muestra la imagen capturada y los botones para guardar o descartar.
 * @param imageUri La URI de la imagen a mostrar.
 * @param onSave Callback para la acción de guardar la imagen.
 * @param onDiscard Callback para la acción de descartar la imagen.
 * @param modifier El modificador para la vista.
 */
@Composable
fun CapturedImageView(
    imageUri: Uri,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Muestra la imagen utilizando la librería Coil para cargarla desde la URI.
        Image(
            painter = rememberAsyncImagePainter(model = imageUri),
            contentDescription = "Captured image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Fila con los botones de acción.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onDiscard)
            {
                Text("Descartar")

            }
            Button(onClick = onSave) {
                Text("Guardar")
            }
        }
    }
}

/**
 * Función que guarda un archivo de imagen desde una URI temporal a la galería de fotos.
 * @param context El contexto de la aplicación.
 * @param uri La URI de la imagen temporal.
 */
private fun saveImageToGallery(context: Context, uri: Uri) {
    val resolver = context.contentResolver
    val inputStream = resolver.openInputStream(uri)

    val name = "miFoto-${System.currentTimeMillis()}.jpg"
    val albumName = "MiAppCameraX"

    // Crea un ContentValues para la nueva imagen en la galería.
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/$albumName")
        }
    }

    // Abre un OutputStream para escribir la nueva imagen en la galería.
    val outputStream = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let {
        resolver.openOutputStream(it)
    }

    // Copia el contenido del archivo temporal al archivo de la galería.
    inputStream?.use { input ->
        outputStream?.use { output ->
            input.copyTo(output)
        }
    }
    inputStream?.close()

    deleteTemporaryImage(context, uri)
}

/**
 * Función que elimina un archivo de imagen temporal.
 * @param context El contexto de la aplicación.
 * @param uri La URI de la imagen temporal.
 */
private fun deleteTemporaryImage(context: Context, uri: Uri?) {
    // Obtiene el archivo desde la URI y lo elimina.
    val file = uri?.path?.let { File(it) }
    val contentResolver = context.contentResolver
    println("Ruta borrar $uri")
    try {
        //file?.delete()
        contentResolver.delete(uri!!, null, null)
        println("Pasanaa eh...")
    }catch (e: Exception){
        println("Error al eliminar el archivo ${e.message}")
    }
}
