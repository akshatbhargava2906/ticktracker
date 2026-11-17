package com.ticktracker.data.db

import androidx.room.migration.Migration

/**
 * How the database on the phone is upgraded when its structure changes (D28).
 *
 * The data on the phone is the only copy, so a structure change must never
 * delete it. Room refuses to open a database it has no upgrade path for, and
 * this app never enables Room's "destructive" fallback that would wipe it instead.
 *
 * To change a table (add a column, add a table, and so on):
 *
 * 1. Change the entity in Entities.kt.
 * 2. Raise `version` in AppDatabase by one.
 * 3. Tell Room how to get from the old version to the new one, either:
 *    - add `AutoMigration(from = N, to = N + 1)` to `autoMigrations` in AppDatabase,
 *      which covers simple changes such as a new table or a new column with a default; or
 *    - write a [Migration] with the SQL by hand and add it to [ALL_MIGRATIONS],
 *      for changes Room cannot work out by itself (renames, splitting a table, moving data).
 * 4. Build. A new file appears in app/schemas/ for the new version. Commit it:
 *    Room compares these files to generate and check upgrades.
 *
 * Never edit or delete an existing file in app/schemas/, and never change an
 * entity without raising the version.
 */
val ALL_MIGRATIONS: Array<Migration> = arrayOf(
    // Hand-written migrations go here, oldest first. None are needed yet: version 1 is the first.
)
