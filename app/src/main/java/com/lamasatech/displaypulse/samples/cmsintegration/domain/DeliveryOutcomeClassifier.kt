package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ApiResult
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.DeliveryOutcome
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayReceipt
import javax.inject.Inject

/** Decides, from the result of a send attempt, whether a play event is done, refused, or worth retrying. */
class DeliveryOutcomeClassifier @Inject constructor() {

    /**
     * Classifies one send attempt.
     *
     * @param result the result of sending a play event.
     * @return [DeliveryOutcome.ACKNOWLEDGED] on success, [DeliveryOutcome.ALREADY_ENDED] when the
     * device says the play already has a terminal outcome, [DeliveryOutcome.REJECTED] for client
     * errors that retrying cannot fix, and [DeliveryOutcome.RETRYABLE] for everything else.
     */
    fun classify(result: ApiResult<PlayReceipt>): DeliveryOutcome = when (result) {
        is ApiResult.Success -> DeliveryOutcome.ACKNOWLEDGED
        is ApiResult.NetworkError -> DeliveryOutcome.RETRYABLE
        is ApiResult.HttpError ->
            if (result.code == HTTP_CONFLICT && result.error == TERMINAL_CONFLICT) DeliveryOutcome.ALREADY_ENDED
            else if (result.code in CLIENT_ERRORS && result.code !in RETRYABLE_CLIENT_ERRORS) DeliveryOutcome.REJECTED
            else DeliveryOutcome.RETRYABLE
    }

    private companion object {
        const val HTTP_CONFLICT = 409
        const val TERMINAL_CONFLICT = "terminal_conflict"
        val CLIENT_ERRORS = 400..499

        // 401 is fixable by correcting the token; 408 and 429 are transient.
        val RETRYABLE_CLIENT_ERRORS = setOf(401, 408, 429)
    }
}
