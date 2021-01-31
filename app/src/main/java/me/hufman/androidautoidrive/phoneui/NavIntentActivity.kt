package me.hufman.androidautoidrive.phoneui

import android.animation.ObjectAnimator
import android.graphics.Point
import android.os.Bundle
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_navintent.*
import me.hufman.androidautoidrive.R
import me.hufman.androidautoidrive.carapp.navigation.AndroidGeocoderSearcher
import me.hufman.androidautoidrive.carapp.navigation.NavigationParser
import me.hufman.androidautoidrive.carapp.navigation.NavigationTriggerSender
import me.hufman.androidautoidrive.phoneui.controllers.NavigationSearchController
import me.hufman.androidautoidrive.phoneui.viewmodels.NavigationStatusModel

class NavIntentActivity: AppCompatActivity() {
	companion object {
		val TAG = "NavActivity"
	}

	val viewModel by viewModels<NavigationStatusModel> { NavigationStatusModel.Factory(this.applicationContext) }

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()

		val params = window.decorView.layoutParams as WindowManager.LayoutParams
		params.alpha = 1f
		params.dimAmount = 0.2f
		window.attributes = params

		val display = windowManager.defaultDisplay
		val size = Point()
		display.getSize(size)

		window.setLayout((0.7 * size.x).toInt(), (0.5 * size.y).toInt())
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_navintent)

		viewModel.searchStatus.observe(this) {
			val oldText = txtNavLabel.text
			val text = this.run(it)
			txtNavLabel.text = text
			if (oldText.isNotBlank() && text.isBlank()) {
				finish()
			}
		}
		viewModel.isSearching.observe(this) {
			prgNavSpinner.isIndeterminate = it

			// finished searching
			if (!it) {
				prgNavSpinner.progress = 0

				val animation = ObjectAnimator.ofInt(prgNavSpinner, "progress", prgNavSpinner.progress, prgNavSpinner.max)
				animation.duration = 500
				animation.interpolator = DecelerateInterpolator()
				animation.start()
			}
		}
	}

	override fun onResume() {
		super.onResume()
		val url = intent?.data
		if (url != null) {
			val navParser = NavigationParser(AndroidGeocoderSearcher(this.applicationContext))
			val navTrigger = NavigationTriggerSender(this.applicationContext)
			val controller = NavigationSearchController(lifecycleScope, navParser, navTrigger, viewModel)
			controller.startNavigation(url.toString())
		}
	}
}