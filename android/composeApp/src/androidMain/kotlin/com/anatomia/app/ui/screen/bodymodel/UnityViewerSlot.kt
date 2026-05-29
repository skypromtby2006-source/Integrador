package com.anatomia.app.ui.screen.bodymodel

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anatomia.app.ui.model.AnatomySystem
import io.github.sceneview.Scene
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberOnGestureListener

@Composable
actual fun UnityViewerSlot(onOrganSelected: (organId: String) -> Unit) {
    val viewModel: BodyModelViewModel = viewModel { BodyModelViewModel() }
    val activeSystem by viewModel.activeSystem.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val organId = when (activeSystem) {
        AnatomySystem.CARDIOVASCULAR -> "heart"
        AnatomySystem.RESPIRATORY    -> "lungs"
        else                         -> "heart"
    }

    val modelFile = when (organId) {
        "heart"   -> "models/organ_heart.glb"
        "lungs"   -> "models/organ_lungs.glb"
        "kidneys" -> "models/organ_kidneys.glb"
        else      -> "models/organ_heart.glb"
    }

    val engine            = rememberEngine()
    val modelLoader       = rememberModelLoader(engine, context)
    val environment       = rememberEnvironment(engine)
    val cameraManipulator = rememberCameraManipulator()

    // ModelNode se crea solo cuando la instancia está disponible (carga asíncrona)
    var modelNode by remember { mutableStateOf<ModelNode?>(null) }

    LaunchedEffect(modelFile) {
        modelNode = null
        val instance = modelLoader.loadModelInstance(modelFile)
        if (instance != null) {
            modelNode = ModelNode(
                modelInstance = instance,
                autoAnimate   = true,
                scaleToUnits  = 1.0f,
            )
        }
    }

    Scene(
        modifier          = Modifier.fillMaxSize(),
        engine            = engine,
        modelLoader       = modelLoader,
        environment       = environment,
        cameraManipulator = cameraManipulator,
        childNodes        = listOfNotNull(modelNode),
        onGestureListener = rememberOnGestureListener(
            onSingleTapConfirmed = { _, _ -> onOrganSelected(organId) }
        )
    )
}
