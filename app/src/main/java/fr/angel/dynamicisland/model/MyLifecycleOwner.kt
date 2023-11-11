package fr.angel.dynamicisland.model

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

internal class MyLifecycleOwner : SavedStateRegistryOwner {
	private var mLifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
	private var mSavedStateRegistryController: SavedStateRegistryController = SavedStateRegistryController.create(this)

	/**
	 * @return True if the Lifecycle has been initialized.
	 */
	val isInitialized: Boolean
		get() = true

	fun setCurrentState(state: Lifecycle.State) {
		mLifecycleRegistry.currentState = state
	}

	fun handleLifecycleEvent(event: Lifecycle.Event) {
		mLifecycleRegistry.handleLifecycleEvent(event)
	}

	override val lifecycle: Lifecycle
		get() = mLifecycleRegistry

	override val savedStateRegistry: SavedStateRegistry
		get() = mSavedStateRegistryController.savedStateRegistry

	fun performRestore(savedState: Bundle?) {
		mSavedStateRegistryController.performRestore(savedState)
	}

	fun performSave(outBundle: Bundle) {
		mSavedStateRegistryController.performSave(outBundle)
	}
}