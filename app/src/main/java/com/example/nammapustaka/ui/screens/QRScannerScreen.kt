package com.example.nammapustaka.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nammapustaka.ui.components.AppScreen
import com.example.nammapustaka.ui.components.EmptyState
import com.example.nammapustaka.ui.components.PrimaryActionButton
import com.example.nammapustaka.ui.components.StatusPill
import com.example.nammapustaka.viewmodel.BookViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun QRScannerScreen(
    viewModel: BookViewModel = viewModel(),
    userId: String,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val resultMessage by viewModel.resultMessage.observeAsState("")

    var scanned by remember { mutableStateOf(false) }
    var cameraReady by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf("") }
    var manualBookId by remember { mutableStateOf("") }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        viewModel.clearResultMessage()
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    AppScreen(
        title = "Scan QR",
        subtitle = "Issue or return books",
        onBack = onBack,
        trailing = { StatusPill("Scanner", Icons.Outlined.QrCodeScanner) }
    ) {
        when {
            userId.isBlank() -> {
                EmptyState(
                    title = "Session expired",
                    message = "Login again before scanning.",
                    icon = Icons.Outlined.WarningAmber
                )
            }

            !hasPermission -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EmptyState(
                        title = "Camera blocked",
                        message = "Camera permission is required.",
                        icon = Icons.Outlined.WarningAmber
                    )
                    PrimaryActionButton(
                        text = "Allow Camera",
                        icon = Icons.Outlined.QrCodeScanner,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            }

            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build()
                                    val selector = CameraSelector.DEFAULT_BACK_CAMERA
                                    val analyzer = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()

                                    analyzer.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                        if (scanned) {
                                            imageProxy.close()
                                            return@setAnalyzer
                                        }

                                        val mediaImage = imageProxy.image

                                        if (mediaImage == null) {
                                            imageProxy.close()
                                            return@setAnalyzer
                                        }

                                        val image = InputImage.fromMediaImage(
                                            mediaImage,
                                            imageProxy.imageInfo.rotationDegrees
                                        )

                                        BarcodeScanning.getClient()
                                            .process(image)
                                            .addOnSuccessListener { barcodes ->
                                                val bookId = barcodes.firstNotNullOfOrNull { it.rawValue }

                                                if (!bookId.isNullOrBlank()) {
                                                    scanned = true
                                                    viewModel.issueOrReturn(bookId, userId)
                                                    Toast.makeText(
                                                        context,
                                                        "Processing book...",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                            .addOnFailureListener { error ->
                                                cameraError = error.message ?: "Unable to read QR code"
                                            }
                                            .addOnCompleteListener {
                                                imageProxy.close()
                                            }
                                    }

                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        selector,
                                        preview,
                                        analyzer
                                    )
                                    preview.setSurfaceProvider(previewView.surfaceProvider)
                                    cameraReady = true
                                    cameraError = ""
                                } catch (error: Exception) {
                                    cameraReady = false
                                    cameraError = error.message ?: "Unable to start the camera"
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        }
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(240.dp)
                            .border(
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                RoundedCornerShape(8.dp)
                            )
                    )

                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(14.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            StatusPill(
                                text = when {
                                    scanned -> "Processed"
                                    cameraError.isNotBlank() -> "Camera error"
                                    cameraReady -> "Live camera"
                                    else -> "Starting camera"
                                },
                                icon = if (scanned) Icons.Outlined.CheckCircle else Icons.Outlined.QrCodeScanner
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = cameraError.ifBlank {
                                    resultMessage.ifBlank { "Point the camera at a book QR code." }
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = manualBookId,
                                onValueChange = { manualBookId = it.trim() },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Book QR code") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            PrimaryActionButton(
                                text = "Process Code",
                                icon = Icons.Outlined.QrCodeScanner,
                                enabled = manualBookId.isNotBlank()
                            ) {
                                scanned = true
                                viewModel.issueOrReturn(manualBookId, userId)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            PrimaryActionButton(
                                text = "Scan Another",
                                icon = Icons.Outlined.Refresh,
                                enabled = scanned || cameraError.isNotBlank()
                            ) {
                                scanned = false
                                cameraError = ""
                                manualBookId = ""
                            }
                        }
                    }
                }
            }
        }
    }
}
