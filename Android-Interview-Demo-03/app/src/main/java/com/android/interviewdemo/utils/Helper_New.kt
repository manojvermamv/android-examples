//import android.app.Activity
//import android.content.Context
//import android.os.Build
//import android.text.Html
//
//
//object AnimationHelper {
//    fun fadeView(view: View, duration: Long = 300L, toAlpha: Float = 1f) {
//        view.animate()
//            .alpha(toAlpha)
//            .setDuration(duration)
//            .setInterpolator(AccelerateDecelerateInterpolator())
//            .start()
//    }
//
//    fun slideView(view: View, translationY: Float = 0f) {
//        view.animate()
//            .translationY(translationY)
//            .setDuration(300L)
//            .start()
//    }
//
//    fun rotateView(view: View, degrees: Float, duration: Long = 300L) {
//        view.animate()
//            .rotation(degrees)
//            .setDuration(duration)
//            .start()
//    }
//}
//
//
///**
// * Advanced View extensions with animation and click handling capabilities
// *
// * Usage:
// * ```
// * view.showWithAnimation()
// * view.hideWithAnimation { /* Completion callback */ }
// * view.setDebouncedClickListener { /* Handle click */ }
// * ```
// */
//object ViewExtensions {
//
//    @JvmStatic
//    fun View.showWithAnimation(duration: Long = 300L, onComplete: () -> Unit = {}) {
//        if (visibility == View.VISIBLE) return
//        alpha = 0f
//        visibility = View.VISIBLE
//        animate()
//            .alpha(1f)
//            .setDuration(duration)
//            .withEndAction(onComplete)
//            .start()
//    }
//
//    @JvmStatic
//    fun View.hideWithAnimation(duration: Long = 300L, onComplete: () -> Unit = {}) {
//        if (visibility != View.VISIBLE) return
//        animate()
//            .alpha(0f)
//            .setDuration(duration)
//            .withEndAction {
//                visibility = View.GONE
//                onComplete()
//            }
//            .start()
//    }
//
//    @JvmStatic
//    fun View.setDebouncedClickListener(delay: Long = 500L, action: (View) -> Unit) {
//        var lastClickTime = 0L
//        setOnClickListener { view ->
//            if (SystemClock.elapsedRealtime() - lastClickTime >= delay) {
//                lastClickTime = SystemClock.elapsedRealtime()
//                action(view)
//            }
//        }
//    }
//}
//
///**
// * Reactive extensions for LiveData/Flow with advanced stream manipulation
// *
// * Usage:
// * ```
// * flow.throttleFirst(1000L).collect { /* Handle values */ }
// * liveData.observeOnce(lifecycleOwner) { /* Single observation */ }
// * resourceFlow.toStateFlow(viewModelScope)
// * ```
// */
//object ReactiveExtensions {
//
//    fun <T> Flow<T>.throttleFirst(duration: Long): Flow<T> = flow {
//        var lastEmissionTime = 0L
//        collect { value ->
//            val currentTime = System.currentTimeMillis()
//            if (currentTime - lastEmissionTime > duration) {
//                lastEmissionTime = currentTime
//                emit(value)
//            }
//        }
//    }
//
//    fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
//        observe(owner, object : Observer<T> {
//            override fun onChanged(value: T) {
//                observer(value)
//                removeObserver(this)
//            }
//        })
//    }
//
//    fun <T> Flow<Resource<T>>.toStateFlow(
//        scope: CoroutineScope,
//        initialValue: Resource<T> = Resource.Loading()
//    ): StateFlow<Resource<T>> = stateIn(
//        scope = scope,
//        started = SharingStarted.WhileSubscribed(5000),
//        initialValue = initialValue
//    )
//}
//
///**
// * Type-safe navigation helpers for Fragment/Activity transitions
// *
// * Usage:
// * ```
// * fragment.navigateWithArgs<DetailFragment>(R.id.detail, item)
// * val args = fragment.getNavigationArgs<DetailArgs>()
// * context.startActivity<ProfileActivity>()
// * ```
// */
//object NavigationExtensions {
//
//    inline fun <reified T : Any> Fragment.navigateWithArgs(
//        @IdRes resId: Int,
//        args: T? = null,
//        navOptions: NavOptions? = null
//    ) where T : Parcelable {
//        findNavController().navigate(
//            resId,
//            args?.let { bundleOf(T::class.java.name to it) },
//            navOptions
//        )
//    }
//
//    inline fun <reified T : Parcelable> Fragment.getNavigationArgs(): T? {
//        return arguments?.getParcelable(T::class.java.name)
//    }
//
//    inline fun <reified T : Activity> Context.startActivity(
//        flags: Int? = null,
//        options: Bundle? = null,
//        crossinline intentConfig: Intent.() -> Unit = {}
//    ) {
//        val intent = Intent(this, T::class.java).apply {
//            flags?.let { addFlags(it) }
//            intentConfig()
//        }
//        if (this is Activity) {
//            startActivity(intent, options)
//        } else {
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//        }
//    }
//}
//
///**
// * Secure SharedPreferences wrapper with type-safe operations
// *
// * Usage:
// * ```
// * securePrefs.put("user_token", "abc123")
// * val token = securePrefs.get<String>("user_token", "")
// * ```
// */
//@RequiresApi(Build.VERSION_CODES.M)
//class SecurePreferences(context: Context) {
//    private val masterKey = MasterKey.Builder(context)
//        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
//        .build()
//
//    private val sharedPreferences = EncryptedSharedPreferences.create(
//        context,
//        "secure_prefs",
//        masterKey,
//        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
//        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
//    )
//
//    /**
//     * Get stored preference value
//     * @param key Preference key
//     * @param defaultValue Default value if key not found
//     */
//    inline fun <reified T> get(key: String, defaultValue: T): T {
//        return when (T::class) {
//            String::class -> sharedPreferences.getString(key, defaultValue as? String) as T
//            Int::class -> sharedPreferences.getInt(key, defaultValue as? Int ?: 0) as T
//            Boolean::class -> sharedPreferences.getBoolean(key, defaultValue as? Boolean ?: false) as T
//            else -> throw IllegalArgumentException("Unsupported type")
//        }
//    }
//
//    /**
//     * Store preference value
//     * @param key Preference key
//     * @param value Value to store
//     */
//    inline fun <reified T> put(key: String, value: T) {
//        sharedPreferences.edit().apply {
//            when (T::class) {
//                String::class -> putString(key, value as String)
//                Int::class -> putInt(key, value as Int)
//                Boolean::class -> putBoolean(key, value as Boolean)
//                else -> throw IllegalArgumentException("Unsupported type")
//            }
//        }.apply()
//    }
//}
//
//
///**
// * **1. Smart View Extensions**
// */
//inline fun View.showWithAnimation(
//    duration: Long = 300L,
//    crossinline onComplete: () -> Unit = {}
//) {
//    if (visibility == View.VISIBLE) return
//    alpha = 0f
//    visibility = View.VISIBLE
//    animate()
//        .alpha(1f)
//        .setDuration(duration)
//        .withEndAction { onComplete() }
//        .start()
//}
//
//inline fun View.hideWithAnimation(
//    duration: Long = 300L,
//    crossinline onComplete: () -> Unit = {}
//) {
//    if (visibility != View.VISIBLE) return
//    animate()
//        .alpha(0f)
//        .setDuration(duration)
//        .withEndAction {
//            visibility = View.GONE
//            onComplete()
//        }
//        .start()
//}
//
//inline fun View.setDebouncedClickListener(
//    delay: Long = 500L,
//    crossinline action: (View) -> Unit
//) {
//    var lastClickTime = 0L
//    setOnClickListener { view ->
//        if (SystemClock.elapsedRealtime() - lastClickTime >= delay) {
//            lastClickTime = SystemClock.elapsedRealtime()
//            action(view)
//        }
//    }
//}
//```
//
//
///**
// * **2. Advanced LiveData/Flow Utilities**
// * */
//fun <T> Flow<T>.throttleFirst(duration: Long): Flow<T> = flow {
//    var lastEmissionTime = 0L
//    collect { value ->
//        val currentTime = System.currentTimeMillis()
//        if (currentTime - lastEmissionTime > duration) {
//            lastEmissionTime = currentTime
//            emit(value)
//        }
//    }
//}
//
//fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
//    observe(owner, object : Observer<T> {
//        override fun onChanged(value: T) {
//            observer(value)
//            removeObserver(this)
//        }
//    })
//}
//
//fun <T> Flow<Resource<T>>.toStateFlow(
//    scope: CoroutineScope,
//    initialValue: Resource<T> = Resource.Loading()
//): StateFlow<Resource<T>> = stateIn(
//    scope = scope,
//    started = SharingStarted.WhileSubscribed(5000),
//    initialValue = initialValue
//)
//
///**
// * **3. Fragment/Activity Navigation Helpers**
// * */
//inline fun <reified T : Any> Fragment.navigateWithArgs(
//    @IdRes resId: Int,
//    args: T? = null,
//    navOptions: NavOptions? = null
//) where T : Parcelable {
//    findNavController().navigate(
//        resId,
//        args?.let { bundleOf(T::class.java.name to it) },
//        navOptions
//    )
//}
//
//inline fun <reified T : Parcelable> Fragment.getNavigationArgs(): T? {
//    return arguments?.getParcelable(T::class.java.name)
//}
//
//inline fun <reified T : Activity> Context.startActivity(
//    flags: Int? = null,
//    options: Bundle? = null,
//    crossinline intentConfig: Intent.() -> Unit = {}
//) {
//    val intent = Intent(this, T::class.java).apply {
//        flags?.let { addFlags(it) }
//        intentConfig()
//    }
//    if (this is Activity) {
//        startActivity(intent, options)
//    } else {
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(intent)
//    }
//}
//
//
///**
// * **4. Advanced Resource Management**
// */
//fun Context.getQuantityString(
//    @PluralsRes resId: Int,
//    quantity: Int,
//    vararg formatArgs: Any
//): String = resources.getQuantityString(resId, quantity, *formatArgs)
//
//inline fun <reified T> Context.typedArrayResource(
//    @ArrayRes resId: Int,
//    crossinline block: (TypedArray) -> T
//): T {
//    return resources.obtainTypedArray(resId).use { ta ->
//        block(ta)
//    }
//}
//
//
///**
// * **5. Advanced Coroutine Workers**
// * */
//class CoroutineWorkManager(
//    private val workManager: WorkManager,
//    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
//) {
//    suspend fun <T : ListenableWorker> awaitWorkResult(
//        workRequest: WorkRequest
//    ): Result = withContext(dispatcher) {
//        suspendCancellableCoroutine { continuation ->
//            workManager.enqueue(workRequest).result.addListener(
//                {
//                    val workInfo = workManager.getWorkInfoById(workRequest.id).get()
//                    when (workInfo.state) {
//                        WorkInfo.State.SUCCEEDED -> continuation.resume(Result.success())
//                        WorkInfo.State.FAILED -> continuation.resume(Result.failure())
//                        WorkInfo.State.CANCELLED -> continuation.cancel()
//                        else -> { /* Do nothing */ }
//                    }
//                },
//                MoreExecutors.directExecutor()
//            )
//        }
//    }
//}
//
//
///**
// * **6. Advanced Input Validation**
// */
//class InputValidator {
//    private val validations = mutableMapOf<Int, (String) -> Boolean>()
//
//    fun addValidation(viewId: Int, validation: (String) -> Boolean) {
//        validations[viewId] = validation
//    }
//
//    fun validateAll(views: List<EditText>): Boolean {
//        return views.all { editText ->
//            validations[editText.id]?.invoke(editText.text.toString()) ?: true
//        }
//    }
//
//    fun EditText.validateOnTextChange(validation: (String) -> Boolean) {
//        addValidation(id) { validation(it) }
//        doAfterTextChanged { validateAll(listOf(this)) }
//    }
//}
//
///**
// * Advanced input validation system with reactive updates
// *
// * Usage:
// * ```
// * validator.apply {
// *     editText.validateOnTextChange { it.isValidEmail() }
// *     validateAll().collect { valid -> button.isEnabled = valid }
// * }
// * ```
// */
//class InputValidator {
//    private val validations = mutableMapOf<Int, (String) -> Boolean>()
//    private val _validationState = MutableStateFlow(false)
//    val validationState: StateFlow<Boolean> = _validationState
//
//    /**
//     * Add validation rule for a view
//     * @param viewId Resource ID of the view
//     * @param validation Validation lambda
//     */
//    fun addValidation(viewId: Int, validation: (String) -> Boolean) {
//        validations[viewId] = validation
//    }
//
//    /**
//     * Validate all registered inputs
//     */
//    fun validateAll(views: List<EditText>): Boolean {
//        return views.all { editText ->
//            validations[editText.id]?.invoke(editText.text.toString()) ?: true
//        }.also { isValid ->
//            _validationState.value = isValid
//        }
//    }
//
//    /**
//     * Setup text change validation for EditText
//     */
//    fun EditText.validateOnTextChange(validation: (String) -> Boolean) {
//        addValidation(id) { validation(it) }
//        doAfterTextChanged { validateAll(listOf(this)) }
//    }
//}
//
//
//
//
///**
// * **7. Advanced File Operations**
// */
//fun File.copyToWithProgress(
//    dest: File,
//    context: Context,
//    callback: (progress: Float) -> Unit
//): Boolean {
//    return try {
//        inputStream().use { input ->
//            dest.outputStream().use { output ->
//                val buffer = ByteArray(1024)
//                var bytesRead: Int
//                var totalBytes = 0L
//                val totalSize = this.length()
//
//                while (input.read(buffer).also { bytesRead = it } != -1) {
//                    output.write(buffer, 0, bytesRead)
//                    totalBytes += bytesRead
//                    callback(totalBytes.toFloat() / totalSize)
//                }
//            }
//        }
//        true
//    } catch (e: Exception) {
//        false
//    }
//}
//
//
///**
// * **8. Advanced Error Handling**
// * */
//inline fun <T> Result<T>.onSuccessWithContext(
//    context: CoroutineContext = Dispatchers.Main,
//    crossinline action: (T) -> Unit
//): Result<T> {
//    return also {
//        if (isSuccess) {
//            CoroutineScope(context).launch { action(getOrNull()!! }
//        }
//    }
//}
//
//inline fun <T> Result<T>.onFailureWithContext(
//    context: CoroutineContext = Dispatchers.Main,
//    crossinline action: (Throwable) -> Unit
//): Result<T> {
//    return also {
//        if (isFailure) {
//            CoroutineScope(context).launch { action(exceptionOrNull()!!) }
//        }
//    }
//}
//
//
///**
// * **9. Advanced Sensor Management**
// */
//class SensorEventManager(
//    context: Context,
//    private val sensorType: Int,
//    private val samplingPeriod: Int = SensorManager.SENSOR_DELAY_NORMAL
//) : LifecycleObserver {
//
//    private val sensorManager = context.getSystemService<SensorManager>()
//    private var sensorEventListener: SensorEventListener? = null
//
//    private val _sensorEvents = MutableSharedFlow<SensorEvent>()
//    val sensorEvents: SharedFlow<SensorEvent> = _sensorEvents
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_START)
//    fun startListening() {
//        sensorManager?.getDefaultSensor(sensorType)?.let { sensor ->
//            sensorEventListener = object : SensorEventListener {
//                override fun onSensorChanged(event: SensorEvent) {
//                    _sensorEvents.tryEmit(event)
//                }
//
//                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//            }
//            sensorManager.registerListener(
//                sensorEventListener,
//                sensor,
//                samplingPeriod
//            )
//        }
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
//    fun stopListening() {
//        sensorEventListener?.let {
//            sensorManager?.unregisterListener(it)
//        }
//    }
//}
//
///*
//These extensions demonstrate advanced Kotlin patterns including:
//1. **Reified Generics** for type-safe operations
//2. **Coroutine Integration** for async operations
//3. **Flow/LiveData Transformations** for reactive programming
//4. **Lifecycle Awareness** for resource management
//5. **Extension Functions** for Android component augmentation
//6. **Functional Programming** patterns for clean code
//7. **Null Safety** with proper error handling
//8. **Performance Optimization** through debouncing and throttling
//9. **Modern Architecture** patterns (MVVM, MVI)
//10. **Cross-component Communication** with type safety
//
//Each extension is designed to solve common Android development challenges while maintaining:
//- **Testability** through clear input/output separation
//- **Maintainability** via modular design
//- **Performance** through efficient resource management
//- **Safety** with proper error handling
//- **Modern Practices** following Google's recommended guidelines
//
//They can be easily integrated into existing projects and extended for specific use cases while maintaining type safety and code clarity.
//*/
