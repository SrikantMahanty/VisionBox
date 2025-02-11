package com.srikant.visionbox.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.PowerManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import android.view.Surface
import androidx.fragment.app.viewModels
import com.srikant.visionbox.R
import com.srikant.visionbox.databinding.FragmentMainBinding
import com.srikant.visionbox.ml.AutoModel1
import com.srikant.visionbox.model.RecognizedObjects
import com.srikant.visionbox.viewmodel.MainViewModel

class AnaFragment : Fragment() {

    private lateinit var labels: List<String>
    private var colorsList = listOf(Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK, Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED)
    private val paint = Paint()
    private lateinit var imageProcessor: ImageProcessor
    private lateinit var model: AutoModel1
    private lateinit var handler: Handler
    private lateinit var cameraManager: CameraManager
    private var cameraDevice: CameraDevice? = null
    private lateinit var coroutineScope: CoroutineScope
    private val recognizedObjects = mutableListOf<RecognizedObjects>()
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private val vm: MainViewModel by viewModels()
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private var camera = false
    private var scanning = false



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        labels = FileUtil.loadLabels(requireContext(), "labels.txt")

// Print a log message
        Log.d("LabelsLoad", "Loaded labels: $labels")

        cameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager

        coroutineScope = lifecycleScope

        coroutineScope.launch {
            uyanikTutmaKilidi()

            val handlerThread = HandlerThread("videoThread")
            handlerThread.start()
            handler = Handler(handlerThread.looper)

            imageProcessor = ImageProcessor.Builder().add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)).build()
            model = AutoModel1.newInstance(requireContext())

            toolbarMenuTiklamaIslemleri()
            liveDatalariGozlemle()
        }
    }

    private fun liveDatalariGozlemle () {
        vm.recognizedObjectsListesiLiveData.observe(viewLifecycleOwner) {
            val stringBuilder = StringBuilder()

            recognizedObjects.forEach {
                stringBuilder.append("${it.label}: %${it.probability.toInt()}\n")
            }
            binding.multiLineTextView.text = stringBuilder.toString()
        }
    }

    private fun toolbarMenuTiklamaIslemleri (){
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {

                R.id.kamerayiAc_menuitem -> {

                    camera = camera == false
                    kameraOperasyonlari(camera)
                    return@setOnMenuItemClickListener true
                }

                R.id.tara_menuitem -> {
                    if (camera) {
                        scanning = scanning == false
                        taramaOperasyonlari(scanning)
                    } else
                        Snackbar.make(requireView(),"Önce kamera açılmalı",Snackbar.LENGTH_SHORT).show()

                    return@setOnMenuItemClickListener true
                }

                else -> return@setOnMenuItemClickListener false
            }
        }
    }

    fun kameraOperasyonlari (kamera : Boolean) {
        if(kamera) {
            if (kameraIzniVarMi()) {
                kamerayiAc()
                binding.txtKameraDurumu.setText(R.string.acik)
                binding.txtKameraDurumu.setTextColor(Color.GREEN)
            } else kameraIzniIste()
        }
        else {
            scanning = false
            taramaOperasyonlari(false)
            kamerayiKapat()
            vm.recognizedObjectsListesiLiveData.value = arrayListOf()
            binding.txtKameraDurumu.setText(R.string.kapali)
            binding.txtKameraDurumu.setTextColor(Color.RED)
        }
    }

    fun taramaOperasyonlari (tarama : Boolean) {
        if(tarama && camera ) {
            binding.txtObjeTanimaDurumu.setText(R.string.acik)
            binding.txtObjeTanimaDurumu.setTextColor(Color.GREEN)
            textureIslemleri()
        }
        else {
            binding.txtObjeTanimaDurumu.setText(R.string.kapali)
            binding.txtObjeTanimaDurumu.setTextColor(Color.RED)
        }
    }


    fun kameraIzniVarMi () = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED


    fun kameraIzniIste () {

        @Suppress("DEPRECATION")
        requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 255)
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult (requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            255 -> {
                if(kameraIzniVarMi()){
                    kamerayiAc()
                    binding.txtKameraDurumu.setText(R.string.acik)
                    binding.txtKameraDurumu.setTextColor(Color.GREEN)
                }
                else Snackbar.make(requireView(), "İzin alınamadı", Snackbar.LENGTH_SHORT).show()
            }
        }
    }


    private fun uyanikTutmaKilidi() {
        powerManager = requireActivity().getSystemService(Context.POWER_SERVICE) as PowerManager

        @Suppress("DEPRECATION")
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "ObjetAnima::UyanikTutmaKilidimTag")

        wakeLock.acquire(10*60*1000L /*10 dakika*/)
    }


    fun textureIslemleri () {
        binding.textureView.surfaceTextureListener = object: TextureView.SurfaceTextureListener {

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture) = false

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                coroutineScope.launch {
                    if (camera) kamerayiAc()
                }
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

                coroutineScope.launch {
                    val bitmap = binding.textureView.bitmap!!

                    var image = TensorImage.fromBitmap(bitmap)
                    image = imageProcessor.process(image)

                    val outputs = model.process(image)
                    val konumlar = outputs.locationsAsTensorBuffer.floatArray
                    val siniflar = outputs.classesAsTensorBuffer.floatArray
                    val skorlar = outputs.scoresAsTensorBuffer.floatArray
                    val mutable =  bitmap.copy(Bitmap.Config.ARGB_8888,true)
                    val tuval = Canvas(mutable)

                    val h = mutable.height
                    val w = mutable.width
                    paint.textSize = h/15f
                    paint.strokeWidth = h/85f

                    var x = 0

                    skorlar.forEachIndexed { index, fl ->
                        x = index
                        x *= 4
                        if (fl > 0.5 && scanning) {
                            val etiket = labels.get(siniflar.get(index).toInt())
                            val olasilik = fl * 100
                            val renk = colorsList.get(index)

                            recognizedObjects.add(RecognizedObjects(etiket, olasilik, renk))

                            paint.setColor(renk)
                            paint.style = Paint.Style.STROKE
                            tuval.drawRect(
                                RectF(
                                    konumlar.get(x + 1) * w,
                                    konumlar.get(x) * h,
                                    konumlar.get(x + 3) * w,
                                    konumlar.get(x + 2) * h
                                ), paint
                            )
                            paint.style = Paint.Style.FILL
                            tuval.drawText(
                                "${etiket}",
                                konumlar.get(x + 1) * w,
                                konumlar.get(x) * h,
                                paint
                            )
                        }
                    }
                    binding.imageView.setImageBitmap(mutable)
                    vm.recognizedObjectsListesiLiveData.value = recognizedObjects
                    recognizedObjects.clear()
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    fun kamerayiAc () {
        coroutineScope.launch {

            binding.textureView.visibility = View.VISIBLE
            binding.imageView.visibility = View.VISIBLE
            cameraManager.openCamera(cameraManager.cameraIdList[0], object : CameraDevice.StateCallback() {

                override fun onDisconnected(camera: CameraDevice) {}
                override fun onError(camera: CameraDevice, error: Int) {}

                override fun onOpened(camera: CameraDevice) {

                    cameraDevice = camera

                    val surface = Surface(binding.textureView.surfaceTexture)
                    val yakalamaIstegi = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    yakalamaIstegi.addTarget(surface)

                    @Suppress("DEPRECATION")
                    camera.createCaptureSession(listOf(surface), object: CameraCaptureSession.StateCallback(){

                        override fun onConfigureFailed(session: CameraCaptureSession) {}

                        override fun onConfigured(session: CameraCaptureSession) {
                            session.setRepeatingRequest(yakalamaIstegi.build(), null , null)
                        }
                    }, handler)
                }
            } , handler)
        }
    }


    fun kamerayiKapat() {
        cameraDevice?.close()
        cameraDevice = null
        binding.textureView.visibility = View.INVISIBLE
        binding.imageView.visibility = View.INVISIBLE
    }


    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.launch {
            model.close()
            if (wakeLock.isHeld) wakeLock.release()
        }
    }
}