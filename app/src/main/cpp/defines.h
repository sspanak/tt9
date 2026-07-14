/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#pragma once

#if 1 //defined(FLAG_DO_PROFILE) || defined(FLAG_DBG)

    #if defined(__ANDROID__)
        #include <android/log.h>
    #endif

    #ifndef LOG_TAG
        #define LOG_TAG "VoiceInput: "
    #endif

    #if defined(HOST_TOOL)
        #include <stdio.h>
        #define AKLOGE(fmt, ...) printf(fmt "\n", ##__VA_ARGS__)
        #define AKLOGI(fmt, ...) printf(fmt "\n", ##__VA_ARGS__)
    #else
        #define AKLOGE(fmt, ...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##__VA_ARGS__)
        #define AKLOGI(fmt, ...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##__VA_ARGS__)
    #endif

    #ifndef __ANDROID__
        #include <cassert>
        #include <execinfo.h>
        #include <stdlib.h>

        #define DO_ASSERT_TEST
        #define ASSERT(success) do { if (!(success)) { showStackTrace(); assert(success);} } while (0)
        #define SHOW_STACK_TRACE do { showStackTrace(); } while (0)

        static inline void showStackTrace() {
            void *callstack[128];
            int i, frames = backtrace(callstack, 128);
            char **strs = backtrace_symbols(callstack, frames);
            for (i = 0; i < frames; ++i) {
                if (i == 0) {
                    AKLOGI("=== Trace ===");
                    continue;
                }
                AKLOGI("%s", strs[i]);
            }
            free(strs);
        }
    #else
        #ifdef __cplusplus
            #include <cassert>
        #else
            #include <assert.h>
        #endif

        #define DO_ASSERT_TEST
        #define ASSERT(success) assert(success)
        #define SHOW_STACK_TRACE
    #endif

    #define TIME_START(name)  const int64_t start_##name = ggml_time_us();
    #define TIME_END(name)    const int64_t end_##name = ggml_time_us(); \
                              const int64_t time_taken_##name = (end_##name - start_##name) / 1000L; \
                              AKLOGI("%s:     Time taken by %s: %d ms\n", __func__, #name, (int)time_taken_##name);

#else
    #define AKLOGE(fmt, ...)
    #define AKLOGI(fmt, ...)
    #undef DO_ASSERT_TEST
    #define ASSERT(success)
    #define SHOW_STACK_TRACE
    #define INTS_TO_CHARS(input, length, output)

    #define TIME_START(name)
    #define TIME_END(name)
#endif

#ifdef __cplusplus
// TODO: Use size_t instead of int.
// Disclaimer: You will see a compile error if you use this macro against a variable-length array.
// Sorry for the inconvenience. It isn't supported.
template <typename T, int N>
char (&ArraySizeHelper(T (&array)[N]))[N];
#define NELEMS(x) (sizeof(ArraySizeHelper(x)))
#endif