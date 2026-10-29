package com.lamasatech.displaypulse.samples.cmsintegration.ui.audience

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lamasatech.displaypulse.samples.cmsintegration.R
import com.lamasatech.displaypulse.samples.cmsintegration.databinding.FragmentAudienceBinding
import com.lamasatech.displaypulse.samples.cmsintegration.databinding.ViewKpiTileBinding
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEventType
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * The app's main (and currently only) screen.
 *
 * It shows the connection form, the live measurements, the campaign and play controls and an
 * activity log. All logic lives in [AudienceViewModel]; this class only binds views to state and
 * forwards clicks.
 */
@AndroidEntryPoint
class AudienceFragment : Fragment() {

    private val viewModel: AudienceViewModel by viewModels()

    private var _binding: FragmentAudienceBinding? = null
    private val binding get() = checkNotNull(_binding)

    private var kpiTiles: List<ViewKpiTileBinding> = emptyList()

    // Saved values are copied into the inputs once per screen. After that the inputs keep what the
    // user typed, which Android restores itself on rotation.
    private var inputsPrefilled = false
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ROOT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inputsPrefilled = savedInstanceState?.getBoolean(KEY_INPUTS_PREFILLED) ?: false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_INPUTS_PREFILLED, inputsPrefilled)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAudienceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindActions()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::render)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onScreenVisible(true)
    }

    override fun onStop() {
        viewModel.onScreenVisible(false)
        super.onStop()
    }

    override fun onDestroyView() {
        kpiTiles = emptyList()
        _binding = null
        super.onDestroyView()
    }

    private fun bindActions() = with(binding) {
        btnSave.setOnClickListener {
            viewModel.saveConnection(etHost.text.toString(), etPort.text.toString(), etToken.text.toString())
        }
        btnHealth.setOnClickListener { viewModel.checkHealth() }
        switchPolling.setOnCheckedChangeListener { _, checked -> viewModel.setPolling(checked) }
        btnNewCampaign.setOnClickListener { viewModel.createCampaign(etCampaignName.text.toString()) }
        btnStart.setOnClickListener { viewModel.startPlay(etContentDuration.text.toString()) }
        btnComplete.setOnClickListener { viewModel.endPlay(PlayEventType.COMPLETED, null) }
        btnFail.setOnClickListener { viewModel.endPlay(PlayEventType.FAILED, REASON_FAILED) }
        btnInterrupt.setOnClickListener { viewModel.endPlay(PlayEventType.INTERRUPTED, REASON_INTERRUPTED) }
        btnRetry.setOnClickListener { viewModel.retryPending() }
        btnDiscard.setOnClickListener { viewModel.discardPending() }
    }

    private fun render(state: AudienceUiState) = with(binding) {
        prefillInputsOnce(state)
        renderConnection(state.connection)
        // Setting isChecked fires the listener only on a real change, so the loop stays quiet.
        if (switchPolling.isChecked != state.polling) switchPolling.isChecked = state.polling

        val view = AudiencePresenter.present(state.audience, state.playback.campaign, state.audienceError)
        tvAudienceStatus.text = view.status
        renderKpis(view.kpis)
        tvDemographics.text = view.demographics
        tvCampaignLink.text = view.campaignLink

        renderPlayback(state)
        tvLog.text = state.log.joinToString("\n") { entry ->
            "${timeFormat.format(Date(entry.timeMs))}  ${if (entry.isError) "✖ " else ""}${entry.message}"
        }
    }

    private fun prefillInputsOnce(state: AudienceUiState) = with(binding) {
        if (inputsPrefilled) return@with
        inputsPrefilled = true
        etHost.setText(state.settings.host)
        // A port is an identifier, not a quantity: keep ASCII digits whatever the locale.
        etPort.setText(String.format(Locale.ROOT, "%d", state.settings.port))
        etToken.setText(state.settings.token)
        etCampaignName.setText(state.playback.campaign?.name.orEmpty())
    }

    private fun renderConnection(status: ConnectionStatus) = with(binding.tvConnection) {
        val (text, colorRes) = when (status) {
            ConnectionStatus.Unknown -> getString(R.string.status_unknown) to R.color.text_dim
            is ConnectionStatus.Online -> getString(R.string.status_online) to R.color.status_ok
            is ConnectionStatus.Offline -> getString(R.string.status_offline, status.reason) to R.color.status_error
        }
        this.text = text
        setTextColor(ContextCompat.getColor(context, colorRes))
    }

    private fun renderKpis(tiles: List<KpiTile>) {
        if (kpiTiles.size != tiles.size) {
            binding.kpiGrid.removeAllViews()
            val colors = resources.getIntArray(R.array.kpi_colors)
            kpiTiles = tiles.mapIndexed { index, tile ->
                ViewKpiTileBinding.inflate(layoutInflater, binding.kpiGrid, true).also {
                    it.tvKpiLabel.text = tile.label
                    it.tvKpiValue.setTextColor(colors[index % colors.size])
                }
            }
        }
        kpiTiles.zip(tiles).forEach { (binding, tile) -> binding.tvKpiValue.text = tile.value }
    }

    private fun renderPlayback(state: AudienceUiState) = with(binding) {
        val playback = state.playback
        val campaign = playback.campaign
        tvCampaignId.text = campaign?.let { getString(R.string.campaign_id_format, it.id) }
            ?: getString(R.string.campaign_none)
        tvContentId.text = campaign?.let { getString(R.string.content_id_format, it.contentId) }.orEmpty()
        tvPlayState.text = playback.activePlay?.let { getString(R.string.play_active_format, it.playId) }
            ?: getString(R.string.play_idle)

        btnNewCampaign.isEnabled = playback.canChangeCampaign && !state.busy
        btnStart.isEnabled = playback.canStart && !state.busy
        val canEnd = playback.canEnd && !state.busy
        btnComplete.isEnabled = canEnd
        btnFail.isEnabled = canEnd
        btnInterrupt.isEnabled = canEnd

        val pending = playback.pending
        layoutPending.isVisible = pending != null
        tvPending.text = pending?.let { getString(R.string.pending_format, it.type.wireName, it.eventId) }.orEmpty()
        btnRetry.isEnabled = !state.busy
        btnDiscard.isEnabled = !state.busy
    }

    private companion object {
        const val KEY_INPUTS_PREFILLED = "inputs_prefilled"
        const val REASON_FAILED = "player_error"
        const val REASON_INTERRUPTED = "user_skipped"
    }
}
