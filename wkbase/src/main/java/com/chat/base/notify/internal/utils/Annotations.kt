/*
 * MIT License
 *
 * Copyright (c) 2018 Karn Saheb
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.chat.base.notify.internal.utils

import androidx.annotation.IntDef
import com.chat.base.notify.Notify

/**
 * Denotes features which are considered experimental and are subject to change without notice.
 */
annotation class Experimental

@DslMarker
annotation class NotifyScopeMarker

@Retention(AnnotationRetention.SOURCE)
@IntDef(Notify.IMPORTANCE_MIN,
        Notify.IMPORTANCE_LOW,
        Notify.IMPORTANCE_NORMAL,
        Notify.IMPORTANCE_HIGH,
        Notify.IMPORTANCE_MAX)
annotation class NotifyImportance
