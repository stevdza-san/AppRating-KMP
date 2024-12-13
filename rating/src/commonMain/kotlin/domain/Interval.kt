package com.stevdza_san.demo.domain

/**
 * Represents the intervals at which the app rating dialog can be shown to the user.
 *
 * @property Monthly - The dialog will be shown once every month.
 * @property Quarterly - The dialog will be shown once every three months (quarter of a year).
 * @property SemiAnnually - The dialog will be shown once every six months (half a year).
 * @property Yearly - The dialog will be shown once every twelve months (a full year).
 */
enum class Interval {
    Monthly,
    Quarterly,
    SemiAnnually,
    Yearly
}