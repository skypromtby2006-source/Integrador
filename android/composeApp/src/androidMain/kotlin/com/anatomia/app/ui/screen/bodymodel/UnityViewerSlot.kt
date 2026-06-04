package com.anatomia.app.ui.screen.bodymodel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anatomia.app.ui.model.AnatomySystem
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.Scene
import io.github.sceneview.node.CameraNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberOnGestureListener
import kotlinx.coroutines.delay

@Composable
actual fun UnityViewerSlot(
    onOrganSelected: (organId: String) -> Unit,
    onPoiSelected  : (poiName: String, organId: String) -> Unit,
) {
    val viewModel: BodyModelViewModel = viewModel { BodyModelViewModel() }
    val activeSystem by viewModel.activeSystem.collectAsStateWithLifecycle()

    val organId = when (activeSystem) {
        AnatomySystem.CARDIOVASCULAR -> "heart"
        AnatomySystem.RESPIRATORY    -> "lungs"
        AnatomySystem.URINARY        -> "kidneys"
        else                         -> null
    }

    if (organId != null) {
        SceneViewerContent(
            organId         = organId,
            onOrganSelected = onOrganSelected,
            onPoiSelected   = onPoiSelected,
        )
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Modelo 3D no disponible para este sistema",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SceneViewerContent(
    organId        : String,
    onOrganSelected: (String) -> Unit,
    onPoiSelected  : (poiName: String, organId: String) -> Unit,
) {
    val context = LocalContext.current

    val modelFile = when (organId) {
        "heart"   -> "models/organ_heart.glb"
        "lungs"   -> "models/organ_lungs.glb"
        "kidneys" -> "models/organ_kidneys.glb"
        else      -> "models/organ_heart.glb"
    }

    val engine            = rememberEngine()
    val modelLoader       = rememberModelLoader(engine, context)
    val environment       = rememberEnvironment(engine)
    val cameraNode        = rememberCameraNode(engine)
    val cameraManipulator = rememberCameraManipulator()

    var modelNode    by remember { mutableStateOf<ModelNode?>(null) }
    var tapIndicator by remember { mutableStateOf<Offset?>(null) }
    var viewSize     by remember { mutableStateOf(IntSize.Zero) }

    // BUG 1 fix: scale bigger + center origin via method call
    LaunchedEffect(modelFile) {
        modelNode = null
        val instance = modelLoader.loadModelInstance(modelFile)
        if (instance != null) {
            modelNode = ModelNode(
                modelInstance = instance,
                autoAnimate   = true,
                scaleToUnits  = 2.0f,
            ).also { node ->
                node.centerOrigin(Float3(0f, 0f, 0f))
            }
        }
    }

    // Auto-clear tap indicator after 1.5 s
    LaunchedEffect(tapIndicator) {
        if (tapIndicator != null) {
            delay(1500L)
            tapIndicator = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { viewSize = it }
    ) {
        Scene(
            modifier          = Modifier.fillMaxSize(),
            engine            = engine,
            modelLoader       = modelLoader,
            environment       = environment,
            cameraNode        = cameraNode,
            cameraManipulator = cameraManipulator,
            childNodes        = listOfNotNull(modelNode),
            // BUG 2 fix: detect POI by screen-space proximity, not 3D world center
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { motionEvent, _ ->
                    val mn = modelNode
                    if (mn != null && viewSize.width > 0) {
                        val nearest = findNearestPoiByScreen(
                            tapX       = motionEvent.x,
                            tapY       = motionEvent.y,
                            viewWidth  = viewSize.width,
                            viewHeight = viewSize.height,
                            cameraNode = cameraNode,
                            modelNode  = mn,
                        )
                        if (nearest != null) {
                            tapIndicator = Offset(motionEvent.x, motionEvent.y)
                            onPoiSelected(nearest, organId)
                        } else {
                            onOrganSelected(organId)
                        }
                    } else {
                        onOrganSelected(organId)
                    }
                }
            )
        )

        // Indicador visual en la posición exacta del tap
        AnimatedVisibility(
            visible  = tapIndicator != null,
            modifier = Modifier.fillMaxSize(),
            enter    = fadeIn(),
            exit     = fadeOut(),
        ) {
            tapIndicator?.let { offset ->
                Box(Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .absoluteOffset {
                                IntOffset(
                                    x = (offset.x - 20.dp.toPx()).toInt(),
                                    y = (offset.y - 20.dp.toPx()).toInt(),
                                )
                            }
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                                shape = CircleShape,
                            )
                    )
                }
            }
        }
    }
}

// BUG 2: proyecta cada POI a espacio de pantalla y compara con el tap
private fun findNearestPoiByScreen(
    tapX      : Float,
    tapY      : Float,
    viewWidth : Int,
    viewHeight: Int,
    cameraNode: CameraNode,
    modelNode : ModelNode,
): String? = modelNode.emptyNodes
    .filter { it.name?.startsWith("poi_") == true }
    .minByOrNull { node ->
        val ndc = cameraNode.worldToView(node.worldPosition)
        // NDC [-1,1] → píxeles Android (y=0 arriba)
        val sx  = (ndc.x + 1f) / 2f * viewWidth
        val sy  = (1f - ndc.y) / 2f * viewHeight
        val dx  = sx - tapX
        val dy  = sy - tapY
        dx * dx + dy * dy
    }
    ?.name
