package com.stevdza_san.demo.domain

/**
 * Represents the state of the initial app rating dialog.
 *
 * @property Idle - Indicates that the initial dialog is not yet scheduled or in progress.
 * @property Waiting - Indicates that the initial dialog is scheduled and waiting to be shown to the user.
 * @property Completed - Indicates that the initial dialog has been shown and is no longer pending.
 */
enum class InitialPeriodState {
    Idle,
    Waiting,
    Completed
}