package com.localeventhub.app.repository

import com.localeventhub.app.room.AppDao
import javax.inject.Inject

class DatabaseRepository @Inject constructor(private val appDao: AppDao) {
}